package com.stack.feature.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stack.core.player.PlaybackQueue
import com.stack.core.player.StackPlayerManager
import com.stack.domain.repository.RepeatMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val playerManager: StackPlayerManager,
    private val playbackQueue: PlaybackQueue
) : ViewModel() {

    private val _uiState = MutableStateFlow(NowPlayingUiState())
    val uiState: StateFlow<NowPlayingUiState> = _uiState.asStateFlow()

    init {
        observePlayerState()
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            // Combine player state and queue state
            combine(
                playerManager.playerState,
                playbackQueue.currentQueue,
                playbackQueue.currentIndex
            ) { playerState, queue, queueIndex ->
                // During seeking, preserve seek position
                if (_uiState.value.isSeeking) {
                    _uiState.value.copy(
                        currentTrack = playerState.currentTrack,
                        queue = queue,
                        currentQueueIndex = queueIndex,
                        isPlaying = playerState.isPlaying,
                        duration = playerState.duration,
                        repeatMode = playerState.repeatMode,
                        shuffleEnabled = playerState.shuffleEnabled
                    )
                } else {
                    NowPlayingUiState(
                        currentTrack = playerState.currentTrack,
                        queue = queue,
                        currentQueueIndex = queueIndex,
                        isPlaying = playerState.isPlaying,
                        position = playerState.position,
                        duration = playerState.duration,
                        repeatMode = playerState.repeatMode,
                        shuffleEnabled = playerState.shuffleEnabled,
                        isQueueSheetVisible = _uiState.value.isQueueSheetVisible,
                        isSeeking = _uiState.value.isSeeking,
                        seekPosition = _uiState.value.seekPosition
                    )
                }
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun onIntent(intent: NowPlayingIntent) {
        viewModelScope.launch {
            when (intent) {
                NowPlayingIntent.TogglePlayPause -> {
                    playerManager.togglePlayPause()
                }

                NowPlayingIntent.PlayNext -> {
                    playerManager.skipToNext()
                }

                NowPlayingIntent.PlayPrevious -> {
                    playerManager.skipToPrevious()
                }

                NowPlayingIntent.ToggleShuffle -> {
                    playerManager.toggleShuffle()
                }

                NowPlayingIntent.CycleRepeatMode -> {
                    val currentMode = _uiState.value.repeatMode
                    val nextMode = when (currentMode) {
                        RepeatMode.OFF -> RepeatMode.ALL
                        RepeatMode.ALL -> RepeatMode.ONE
                        RepeatMode.ONE -> RepeatMode.OFF
                    }
                    playerManager.setRepeatMode(nextMode)
                }

                is NowPlayingIntent.StartSeek -> {
                    _uiState.update { it.copy(isSeeking = true, seekPosition = intent.position) }
                }

                is NowPlayingIntent.UpdateSeekPosition -> {
                    _uiState.update { it.copy(seekPosition = intent.position) }
                }

                is NowPlayingIntent.FinishSeek -> {
                    playerManager.seekTo(intent.position)
                    _uiState.update { it.copy(isSeeking = false) }
                }

                NowPlayingIntent.ShowQueueSheet -> {
                    _uiState.update { it.copy(isQueueSheetVisible = true) }
                }

                NowPlayingIntent.HideQueueSheet -> {
                    _uiState.update { it.copy(isQueueSheetVisible = false) }
                }

                is NowPlayingIntent.PlayFromQueue -> {
                    // Get the track from queue and play it
                    val track = _uiState.value.queue.getOrNull(intent.index)
                    if (track != null) {
                        playerManager.play(track, _uiState.value.queue)
                    }
                }
            }
        }
    }
}
