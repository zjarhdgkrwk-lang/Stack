package com.stack.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stack.core.player.PlaybackQueue
import com.stack.core.player.StackPlayerManager
import com.stack.core.player.model.PlayerState
import com.stack.domain.model.Track
import com.stack.domain.repository.RepeatMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for player-related UI (MiniPlayer, NowPlaying).
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerManager: StackPlayerManager,
    private val playbackQueue: PlaybackQueue
) : ViewModel() {

    val playerState: StateFlow<PlayerState> = playerManager.playerState
    val currentQueue: StateFlow<List<Track>> = playbackQueue.currentQueue

    private val _events = Channel<PlayerEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onPlayPauseClick() {
        viewModelScope.launch {
            playerManager.togglePlayPause()
        }
    }

    fun onNextClick() {
        viewModelScope.launch {
            playerManager.skipToNext()
        }
    }

    fun onPreviousClick() {
        viewModelScope.launch {
            playerManager.skipToPrevious()
        }
    }

    fun onSeekTo(positionMs: Long) {
        viewModelScope.launch {
            playerManager.seekTo(positionMs)
        }
    }

    fun onRepeatModeChange() {
        viewModelScope.launch {
            val currentMode = playerState.value.repeatMode
            val nextMode = when (currentMode) {
                RepeatMode.OFF -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.OFF
            }
            playerManager.setRepeatMode(nextMode)
        }
    }

    fun onShuffleToggle() {
        viewModelScope.launch {
            playerManager.toggleShuffle()
        }
    }

    /**
     * Called when MiniPlayer is clicked.
     * Phase 4.3: Just show toast (NowPlaying screen is Phase 4.4 scope)
     */
    fun onMiniPlayerClick() {
        viewModelScope.launch {
            _events.send(PlayerEvent.ShowToast("Full player coming in Phase 4.4"))
        }
    }

    /**
     * Play a track with queue context.
     * Called from Library when track is clicked.
     */
    fun playTrack(track: Track, queue: List<Track>) {
        viewModelScope.launch {
            playerManager.play(track, queue)
        }
    }
}

sealed class PlayerEvent {
    data class ShowToast(val message: String) : PlayerEvent()
    data object NavigateToNowPlaying : PlayerEvent()
}
