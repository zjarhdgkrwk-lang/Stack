package com.stack.core.player.model

import com.stack.domain.model.Track
import com.stack.domain.repository.RepeatMode

/**
 * Internal player state representation.
 * This is the single source of truth for playback status.
 */
data class PlayerState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val position: Long = 0L,           // Current position in ms
    val duration: Long = 0L,           // Track duration in ms
    val bufferedPosition: Long = 0L,   // Buffered position in ms
    val playbackSpeed: Float = 1.0f,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val error: PlayerError? = null
) {
    val progress: Float
        get() = if (duration > 0) position.toFloat() / duration else 0f
}

sealed class PlayerError {
    data class SourceError(val message: String) : PlayerError()
    data class DecoderError(val message: String) : PlayerError()
    data object NetworkError : PlayerError()
    data object UnknownError : PlayerError()
}
