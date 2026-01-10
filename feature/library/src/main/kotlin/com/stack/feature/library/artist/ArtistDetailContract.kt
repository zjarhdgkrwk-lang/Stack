package com.stack.feature.library.artist

import com.stack.domain.model.Album
import com.stack.domain.model.Track

/**
 * Contract for Artist Detail screen (Phase 5.1)
 */

data class ArtistDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val artistId: Long = 0L,
    val artistName: String = "",
    val artistArtUri: String? = null,
    val albumCount: Int = 0,
    val trackCount: Int = 0,
    val albums: List<Album> = emptyList(),
    val tracks: List<Track> = emptyList()
)

sealed interface ArtistDetailIntent {
    data class Load(val artistId: Long) : ArtistDetailIntent
    data object PlayAll : ArtistDetailIntent
    data object ShufflePlay : ArtistDetailIntent
    data class PlayTrack(val index: Int) : ArtistDetailIntent
    data class OpenAlbum(val albumId: Long) : ArtistDetailIntent
    data object NavigateBack : ArtistDetailIntent
}

sealed interface ArtistDetailEffect {
    data class NavigateToAlbum(val albumId: Long) : ArtistDetailEffect
    data object NavigateBack : ArtistDetailEffect
    data class ShowError(val message: String) : ArtistDetailEffect
}
