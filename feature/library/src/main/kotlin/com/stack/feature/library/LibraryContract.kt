package com.stack.feature.library

import com.stack.domain.model.Track
import com.stack.domain.model.TrackSortOrder
import com.stack.domain.model.AlbumSortOrder

/**
 * Library screen MVI contract.
 * Follows SSOT Section 7.2 specification.
 */
object LibraryContract {

    /**
     * Library tabs following SSOT Section 7.2.
     */
    enum class LibraryTab {
        TRACKS,
        ALBUMS,
        ARTISTS
    }

    /**
     * View mode for grid-capable lists.
     */
    enum class ViewMode {
        LIST,
        GRID
    }

    /**
     * Immutable state for Library screen.
     */
    data class State(
        val currentTab: LibraryTab = LibraryTab.TRACKS,

        // Track tab state
        val tracks: List<Track> = emptyList(),
        val trackSortOrder: TrackSortOrder = TrackSortOrder.DATE_ADDED_DESC,

        // Album tab state
        val albums: List<AlbumUiModel> = emptyList(),
        val albumSortOrder: AlbumSortOrder = AlbumSortOrder.NAME_ASC,
        val albumViewMode: ViewMode = ViewMode.GRID,

        // Artist tab state
        val artists: List<ArtistUiModel> = emptyList(),

        // Common state
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val error: LibraryError? = null,
        val showSortMenu: Boolean = false
    ) {
        val isEmpty: Boolean
            get() = when (currentTab) {
                LibraryTab.TRACKS -> tracks.isEmpty()
                LibraryTab.ALBUMS -> albums.isEmpty()
                LibraryTab.ARTISTS -> artists.isEmpty()
            }

        val currentItemCount: Int
            get() = when (currentTab) {
                LibraryTab.TRACKS -> tracks.size
                LibraryTab.ALBUMS -> albums.size
                LibraryTab.ARTISTS -> artists.size
            }
    }

    /**
     * Album UI model aggregated from tracks.
     */
    data class AlbumUiModel(
        val albumId: Long,
        val name: String,
        val artist: String,
        val artworkUri: String?,
        val trackCount: Int,
        val totalDuration: Long,  // ms
        val year: Int?
    )

    /**
     * Artist UI model aggregated from tracks.
     */
    data class ArtistUiModel(
        val artistId: Long,
        val name: String,
        val artworkUri: String?,   // First album art or null
        val albumCount: Int,
        val trackCount: Int
    )

    /**
     * Library-specific errors.
     */
    sealed class LibraryError {
        data object LoadFailed : LibraryError()
        data object RefreshFailed : LibraryError()
        data class Unknown(val message: String) : LibraryError()
    }

    /**
     * User intents (actions).
     */
    sealed class Intent {
        data object LoadLibrary : Intent()
        data class ChangeTab(val tab: LibraryTab) : Intent()
        data class ChangeTrackSortOrder(val order: TrackSortOrder) : Intent()
        data class ChangeAlbumSortOrder(val order: AlbumSortOrder) : Intent()
        data object ToggleAlbumViewMode : Intent()
        data object RefreshLibrary : Intent()
        data object ShowSortMenu : Intent()
        data object HideSortMenu : Intent()
        data object DismissError : Intent()

        // Item click intents (NO PLAYBACK - Phase 4.3)
        data class OnTrackClick(val track: Track) : Intent()
        data class OnAlbumClick(val album: AlbumUiModel) : Intent()
        data class OnArtistClick(val artist: ArtistUiModel) : Intent()
    }

    /**
     * One-time side effects.
     */
    sealed class Effect {
        data class ShowToast(val message: String) : Effect()
        data class ShowError(val error: LibraryError) : Effect()
        data class NavigateToAlbum(val albumId: Long) : Effect()
        data class NavigateToArtist(val artistId: Long) : Effect()
    }
}
