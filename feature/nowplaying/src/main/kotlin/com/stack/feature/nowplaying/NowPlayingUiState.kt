package com.stack.feature.nowplaying

import com.stack.domain.model.Track
import com.stack.domain.repository.RepeatMode

data class NowPlayingUiState(
    val currentTrack: Track? = null,
    val queue: List<Track> = emptyList(),
    val currentQueueIndex: Int = 0,
    val isPlaying: Boolean = false,
    val position: Long = 0L,
    val duration: Long = 0L,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleEnabled: Boolean = false,
    val isQueueSheetVisible: Boolean = false,
    val isSeeking: Boolean = false,
    val seekPosition: Long = 0L  // Temporary position while dragging
)
