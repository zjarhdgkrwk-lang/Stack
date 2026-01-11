package com.stack.feature.nowplaying

sealed interface NowPlayingIntent {
    data object TogglePlayPause : NowPlayingIntent
    data object PlayNext : NowPlayingIntent
    data object PlayPrevious : NowPlayingIntent
    data object ToggleShuffle : NowPlayingIntent
    data object CycleRepeatMode : NowPlayingIntent

    // Seek intents
    data class StartSeek(val position: Long) : NowPlayingIntent
    data class UpdateSeekPosition(val position: Long) : NowPlayingIntent
    data class FinishSeek(val position: Long) : NowPlayingIntent

    // Queue intents
    data object ShowQueueSheet : NowPlayingIntent
    data object HideQueueSheet : NowPlayingIntent
    data class PlayFromQueue(val index: Int) : NowPlayingIntent
}
