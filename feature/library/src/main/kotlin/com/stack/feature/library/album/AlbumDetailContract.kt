package com.stack.feature.library.album

import com.stack.domain.model.Track

/**
 * Contract for Album Detail screen (Phase 5.1)
 */

data class AlbumDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val albumId: Long = 0L,
    val albumName: String = "",
    val artistName: String = "",
    val artistId: Long = 0L,
    val albumArtUri: String? = null,
    val year: Int? = null,
    val trackCount: Int = 0,
    val totalDuration: Long = 0L,  // in milliseconds
    val tracks: List<Track> = emptyList()
)

sealed interface AlbumDetailIntent {
    data class Load(val albumId: Long) : AlbumDetailIntent
    data object PlayAll : AlbumDetailIntent
    data object ShufflePlay : AlbumDetailIntent
    data class PlayTrack(val index: Int) : AlbumDetailIntent
    data object NavigateToArtist : AlbumDetailIntent
    data object NavigateBack : AlbumDetailIntent
}

sealed interface AlbumDetailEffect {
    data class NavigateToArtist(val artistId: Long) : AlbumDetailEffect
    data object NavigateBack : AlbumDetailEffect
    data class ShowError(val message: String) : AlbumDetailEffect
}
