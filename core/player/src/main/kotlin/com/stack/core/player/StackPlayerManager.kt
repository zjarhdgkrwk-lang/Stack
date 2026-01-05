package com.stack.core.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.stack.core.logging.Logger
import com.stack.core.player.model.PlayerError
import com.stack.core.player.model.PlayerState
import com.stack.core.util.DispatcherProvider
import com.stack.domain.model.Track
import com.stack.domain.repository.RepeatMode
import com.stack.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Core playback manager implementing Dual ExoPlayer architecture.
 *
 * SSOT Reference: Section 8.1 (Dual Player Architecture)
 * - Player A: Active playback
 * - Player B: Preload next track at 80% completion
 * - Gapless transition via swap
 *
 * ⚠️ THREAD SAFETY: All ExoPlayer interactions MUST happen on Main thread.
 * Use DispatcherProvider.main for all player operations.
 */
@Singleton
class StackPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playbackQueue: PlaybackQueue,
    private val audioFocusHandler: AudioFocusHandler,
    private val settingsRepository: SettingsRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val logger: Logger
) : AudioFocusHandler.AudioFocusListener {

    private val scope = CoroutineScope(SupervisorJob() + dispatcherProvider.main)

    // Dual Player instances
    private var activePlayer: ExoPlayer? = null
    private var warmPlayer: ExoPlayer? = null

    // Position update job
    private var positionUpdateJob: Job? = null

    // Preload trigger threshold (80% of track duration)
    private val PRELOAD_THRESHOLD = 0.80f
    private var preloadTriggered = false

    // Internal state
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    // Volume for ducking
    private var normalVolume = 1.0f
    private var isDucking = false

    init {
        audioFocusHandler.setListener(this)
        observeSettingsChanges()
    }

    private fun observeSettingsChanges() {
        scope.launch {
            settingsRepository.observeShuffleEnabled().collectLatest { shuffleEnabled ->
                playbackQueue.setShuffle(shuffleEnabled)
                _playerState.update { it.copy(shuffleEnabled = shuffleEnabled) }
            }
        }

        scope.launch {
            settingsRepository.observeRepeatMode().collectLatest { repeatMode ->
                _playerState.update { it.copy(repeatMode = repeatMode) }
            }
        }
    }

    /**
     * Initialize the player manager.
     * Called once when service starts.
     */
    @OptIn(UnstableApi::class)
    suspend fun initialize() = withContext(dispatcherProvider.main) {
        if (activePlayer != null) return@withContext

        logger.d(TAG, "Initializing StackPlayerManager")
        activePlayer = createExoPlayer()
    }

    @OptIn(UnstableApi::class)
    private fun createExoPlayer(): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply {
                addListener(createPlayerListener())
            }
    }

    private fun createPlayerListener() = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    _playerState.update {
                        it.copy(
                            isLoading = false,
                            duration = activePlayer?.duration ?: 0L
                        )
                    }
                    logger.d(TAG, "Player ready, duration: ${activePlayer?.duration}")
                }
                Player.STATE_BUFFERING -> {
                    _playerState.update { it.copy(isLoading = true) }
                }
                Player.STATE_ENDED -> {
                    logger.d(TAG, "Track ended")
                    handleTrackEnded()
                }
                Player.STATE_IDLE -> {
                    _playerState.update { it.copy(isLoading = false) }
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playerState.update { it.copy(isPlaying = isPlaying) }

            if (isPlaying) {
                startPositionUpdates()
            } else {
                stopPositionUpdates()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            logger.e(TAG, "Player error: ${error.message}", error)
            val playerError = when (error.errorCode) {
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> PlayerError.NetworkError
                PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
                PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED ->
                    PlayerError.DecoderError(error.message ?: "Decoder error")
                else -> PlayerError.SourceError(error.message ?: "Unknown error")
            }
            _playerState.update { it.copy(error = playerError, isPlaying = false) }
        }
    }

    /**
     * Play a specific track with context queue.
     * Main entry point from UI.
     */
    suspend fun play(track: Track, queue: List<Track> = listOf(track)) {
        withContext(dispatcherProvider.main) {
            logger.d(TAG, "Playing track: ${track.title}")

            // Request audio focus
            if (!audioFocusHandler.requestFocus()) {
                logger.w(TAG, "Failed to acquire audio focus")
                return@withContext
            }

            // Set up queue
            playbackQueue.setQueue(queue, track)

            // Clear any previous error
            _playerState.update {
                it.copy(
                    currentTrack = track,
                    error = null,
                    isLoading = true
                )
            }

            // Prepare and play
            prepareAndPlay(track)

            // Reset preload state for new track
            preloadTriggered = false
        }
    }

    private fun prepareAndPlay(track: Track) {
        val player = activePlayer ?: return

        val mediaItem = MediaItem.Builder()
            .setUri(track.contentUri)
            .setMediaId(track.id.toString())
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        logger.d(TAG, "Prepared and playing: ${track.contentUri}")
    }

    /**
     * Toggle play/pause.
     */
    suspend fun togglePlayPause() = withContext(dispatcherProvider.main) {
        val player = activePlayer ?: return@withContext

        if (player.isPlaying) {
            player.pause()
            logger.d(TAG, "Paused")
        } else {
            if (audioFocusHandler.requestFocus()) {
                player.play()
                logger.d(TAG, "Resumed")
            }
        }
    }

    /**
     * Pause playback.
     */
    suspend fun pause() = withContext(dispatcherProvider.main) {
        activePlayer?.pause()
    }

    /**
     * Resume playback.
     */
    suspend fun resume() = withContext(dispatcherProvider.main) {
        if (audioFocusHandler.requestFocus()) {
            activePlayer?.play()
        }
    }

    /**
     * Skip to next track.
     */
    suspend fun skipToNext() = withContext(dispatcherProvider.main) {
        val repeatMode = _playerState.value.repeatMode

        if (playbackQueue.moveToNext()) {
            playbackQueue.currentTrack?.let { track ->
                prepareAndPlay(track)
                _playerState.update { it.copy(currentTrack = track) }
            }
        } else if (repeatMode == RepeatMode.ALL) {
            // At end of queue with repeat all, go back to start
            playbackQueue.skipToIndex(0)
            playbackQueue.currentTrack?.let { track ->
                prepareAndPlay(track)
                _playerState.update { it.copy(currentTrack = track) }
            }
        }

        preloadTriggered = false
    }

    /**
     * Skip to previous track or restart current.
     * SSOT: ≤3s = previous track, >3s = restart current
     */
    suspend fun skipToPrevious() = withContext(dispatcherProvider.main) {
        val currentPosition = activePlayer?.currentPosition ?: 0

        if (currentPosition > 3000) {
            // Restart current track
            activePlayer?.seekTo(0)
            logger.d(TAG, "Restarting current track")
        } else if (playbackQueue.moveToPrevious()) {
            playbackQueue.currentTrack?.let { track ->
                prepareAndPlay(track)
                _playerState.update { it.copy(currentTrack = track) }
            }
        } else {
            // At start of queue, just restart
            activePlayer?.seekTo(0)
        }

        preloadTriggered = false
    }

    /**
     * Seek to position.
     */
    suspend fun seekTo(positionMs: Long) = withContext(dispatcherProvider.main) {
        activePlayer?.seekTo(positionMs)
        _playerState.update { it.copy(position = positionMs) }
    }

    /**
     * Set repeat mode.
     */
    suspend fun setRepeatMode(mode: RepeatMode) {
        settingsRepository.setRepeatMode(mode)
        _playerState.update { it.copy(repeatMode = mode) }
    }

    /**
     * Toggle shuffle.
     */
    suspend fun toggleShuffle() {
        val newShuffle = !_playerState.value.shuffleEnabled
        settingsRepository.setShuffleEnabled(newShuffle)
        playbackQueue.setShuffle(newShuffle)
        _playerState.update { it.copy(shuffleEnabled = newShuffle) }
    }

    /**
     * Handle track ended event.
     */
    private fun handleTrackEnded() {
        scope.launch(dispatcherProvider.main) {
            val repeatMode = _playerState.value.repeatMode

            when (repeatMode) {
                RepeatMode.ONE -> {
                    // Repeat current track
                    activePlayer?.seekTo(0)
                    activePlayer?.play()
                }
                RepeatMode.ALL, RepeatMode.OFF -> {
                    skipToNext()
                }
            }
        }
    }

    /**
     * Start position update loop.
     */
    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionUpdateJob = scope.launch(dispatcherProvider.main) {
            while (true) {
                activePlayer?.let { player ->
                    val position = player.currentPosition
                    val duration = player.duration
                    val buffered = player.bufferedPosition

                    _playerState.update {
                        it.copy(
                            position = position,
                            duration = if (duration > 0) duration else it.duration,
                            bufferedPosition = buffered
                        )
                    }

                    // Check for preload trigger
                    checkPreloadTrigger(position, duration)
                }
                delay(250) // Update 4 times per second
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    /**
     * Check if we should preload the next track.
     * Triggered at 80% completion (SSOT Section 8.3).
     */
    @OptIn(UnstableApi::class)
    private fun checkPreloadTrigger(position: Long, duration: Long) {
        if (preloadTriggered || duration <= 0) return

        val progress = position.toFloat() / duration
        if (progress >= PRELOAD_THRESHOLD) {
            preloadTriggered = true
            preloadNextTrack()
        }
    }

    @OptIn(UnstableApi::class)
    private fun preloadNextTrack() {
        val nextTrack = playbackQueue.peekNext() ?: return

        logger.d(TAG, "Preloading next track: ${nextTrack.title}")

        // Create warm player if needed
        if (warmPlayer == null) {
            warmPlayer = createExoPlayer()
        }

        val mediaItem = MediaItem.Builder()
            .setUri(nextTrack.contentUri)
            .setMediaId(nextTrack.id.toString())
            .build()

        warmPlayer?.setMediaItem(mediaItem)
        warmPlayer?.prepare()
    }

    /**
     * Stop playback and release resources.
     */
    suspend fun stop() = withContext(dispatcherProvider.main) {
        activePlayer?.stop()
        activePlayer?.clearMediaItems()

        warmPlayer?.release()
        warmPlayer = null

        audioFocusHandler.abandonFocus()
        stopPositionUpdates()

        _playerState.update { PlayerState() }

        logger.d(TAG, "Playback stopped")
    }

    /**
     * Release all resources.
     */
    suspend fun release() = withContext(dispatcherProvider.main) {
        stop()

        activePlayer?.release()
        activePlayer = null

        logger.d(TAG, "Player released")
    }

    // AudioFocusListener implementation

    override fun onFocusGained() {
        scope.launch(dispatcherProvider.main) {
            if (isDucking) {
                activePlayer?.volume = normalVolume
                isDucking = false
            }
        }
    }

    override fun onFocusLostTransient() {
        scope.launch(dispatcherProvider.main) {
            activePlayer?.pause()
        }
    }

    override fun onFocusLostTransientCanDuck() {
        scope.launch(dispatcherProvider.main) {
            if (!isDucking) {
                normalVolume = activePlayer?.volume ?: 1.0f
                activePlayer?.volume = 0.2f // Duck to 20%
                isDucking = true
            }
        }
    }

    override fun onFocusLostPermanent() {
        scope.launch {
            stop()
        }
    }

    /**
     * Get the active ExoPlayer instance for MediaSession binding.
     */
    fun getPlayer(): ExoPlayer? = activePlayer

    companion object {
        private const val TAG = "StackPlayerManager"
    }
}
