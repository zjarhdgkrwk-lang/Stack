package com.stack.feature.library

import androidx.lifecycle.viewModelScope
import com.stack.core.base.BaseViewModel
import com.stack.core.util.Result
import com.stack.data.scanner.ScanState
import com.stack.domain.model.Track
import com.stack.domain.model.TrackSortOrder
import com.stack.domain.model.AlbumSortOrder
import com.stack.domain.repository.TrackRepository
import com.stack.domain.usecase.scan.ScanLibraryUseCase
import com.stack.feature.library.LibraryContract.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Library screen.
 *
 * Manages tab state, sorting, and data loading from TrackRepository.
 *
 * IMPORTANT: This ViewModel does NOT handle playback.
 * All item clicks result in Toast messages only (Phase 4.3 scope).
 *
 * SSOT Reference: Section 7.2-7.5
 */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val scanLibraryUseCase: ScanLibraryUseCase,
    private val playerManager: com.stack.core.player.StackPlayerManager
) : BaseViewModel<State, Intent, Effect>(State()) {

    init {
        handleIntent(Intent.LoadLibrary)
        observeScanState()
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadLibrary -> loadLibrary()
            is Intent.ChangeTab -> changeTab(intent.tab)
            is Intent.ChangeTrackSortOrder -> changeTrackSortOrder(intent.order)
            is Intent.ChangeAlbumSortOrder -> changeAlbumSortOrder(intent.order)
            is Intent.ToggleAlbumViewMode -> toggleAlbumViewMode()
            is Intent.RefreshLibrary -> refreshLibrary()
            is Intent.ShowSortMenu -> updateState { copy(showSortMenu = true) }
            is Intent.HideSortMenu -> updateState { copy(showSortMenu = false) }
            is Intent.DismissError -> updateState { copy(error = null) }
            is Intent.OnTrackClick -> onTrackClick(intent.track)
            is Intent.OnAlbumClick -> onAlbumClick(intent.album)
            is Intent.OnArtistClick -> onArtistClick(intent.artist)
        }
    }

    private fun loadLibrary() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }

            try {
                // Observe tracks based on current sort order
                trackRepository.observeTracks(state.value.trackSortOrder)
                    .catch { e ->
                        updateState {
                            copy(
                                isLoading = false,
                                error = LibraryError.LoadFailed
                            )
                        }
                    }
                    .collect { tracks ->
                        val albums = aggregateAlbums(tracks)
                        val artists = aggregateArtists(tracks)

                        updateState {
                            copy(
                                tracks = tracks,
                                albums = albums,
                                artists = artists,
                                isLoading = false
                            )
                        }
                    }
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        error = LibraryError.LoadFailed
                    )
                }
            }
        }
    }

    private fun observeScanState() {
        viewModelScope.launch {
            scanLibraryUseCase.scanState.collect { scanState ->
                when (scanState) {
                    is ScanState.Scanning -> {
                        // Keep isRefreshing true while scanning
                        updateState { copy(isRefreshing = true) }
                    }
                    is ScanState.Completed -> {
                        updateState { copy(isRefreshing = false) }
                    }
                    is ScanState.Error -> {
                        updateState {
                            copy(
                                isRefreshing = false,
                                error = LibraryError.RefreshFailed
                            )
                        }
                    }
                    is ScanState.Idle -> {
                        updateState { copy(isRefreshing = false) }
                    }
                }
            }
        }
    }

    private fun changeTab(tab: LibraryTab) {
        updateState { copy(currentTab = tab) }
    }

    private fun changeTrackSortOrder(order: TrackSortOrder) {
        updateState {
            copy(
                trackSortOrder = order,
                showSortMenu = false
            )
        }

        // Re-observe with new sort order
        viewModelScope.launch {
            trackRepository.observeTracks(order)
                .catch { }
                .collect { tracks ->
                    updateState { copy(tracks = tracks) }
                }
        }
    }

    private fun changeAlbumSortOrder(order: AlbumSortOrder) {
        val currentAlbums = state.value.albums
        val sortedAlbums = sortAlbums(currentAlbums, order)

        updateState {
            copy(
                albums = sortedAlbums,
                albumSortOrder = order,
                showSortMenu = false
            )
        }
    }

    private fun toggleAlbumViewMode() {
        val newMode = when (state.value.albumViewMode) {
            ViewMode.LIST -> ViewMode.GRID
            ViewMode.GRID -> ViewMode.LIST
        }
        updateState { copy(albumViewMode = newMode) }
    }

    private fun refreshLibrary() {
        viewModelScope.launch {
            updateState { copy(isRefreshing = true) }

            try {
                // Trigger rescan
                when (val result = scanLibraryUseCase.performIncrementalScan()) {
                    is Result.Success -> {
                        // ScanState observer will handle isRefreshing = false
                    }
                    is Result.Error -> {
                        updateState {
                            copy(
                                isRefreshing = false,
                                error = LibraryError.RefreshFailed
                            )
                        }
                    }
                    is Result.Loading -> { /* Progress handled by scanState observer */ }
                }
            } catch (e: Exception) {
                updateState {
                    copy(
                        isRefreshing = false,
                        error = LibraryError.RefreshFailed
                    )
                }
            }
        }
    }

    /**
     * Play track with current tracks as queue context.
     * Phase 4.3: Now plays actual audio.
     */
    private fun onTrackClick(track: Track) {
        viewModelScope.launch {
            // Get current visible tracks as queue context
            val currentTracks = state.value.tracks
            playerManager.play(track, currentTracks)
        }
    }

    /**
     * Navigate to album detail screen (Phase 5.1).
     */
    private fun onAlbumClick(album: AlbumUiModel) {
        emitEffect(Effect.NavigateToAlbum(album.albumId))
    }

    /**
     * Navigate to artist detail screen (Phase 5.1).
     */
    private fun onArtistClick(artist: ArtistUiModel) {
        emitEffect(Effect.NavigateToArtist(artist.artistId))
    }

    /**
     * Aggregate tracks into album UI models.
     */
    private fun aggregateAlbums(tracks: List<Track>): List<AlbumUiModel> {
        return tracks
            .filter { it.albumId != null && it.album != null }
            .groupBy { it.albumId!! }
            .map { (albumId, albumTracks) ->
                val firstTrack = albumTracks.first()
                AlbumUiModel(
                    albumId = albumId,
                    name = firstTrack.displayAlbum,
                    artist = firstTrack.albumArtist ?: firstTrack.displayArtist,
                    artworkUri = firstTrack.albumArtUri,
                    trackCount = albumTracks.size,
                    totalDuration = albumTracks.sumOf { it.duration },
                    year = firstTrack.year
                )
            }
            .let { sortAlbums(it, state.value.albumSortOrder) }
    }

    /**
     * Aggregate tracks into artist UI models.
     */
    private fun aggregateArtists(tracks: List<Track>): List<ArtistUiModel> {
        return tracks
            .filter { it.artistId != null }
            .groupBy { it.artistId!! }
            .map { (artistId, artistTracks) ->
                val firstTrack = artistTracks.first()
                val albumCount = artistTracks.mapNotNull { it.albumId }.distinct().size
                ArtistUiModel(
                    artistId = artistId,
                    name = firstTrack.displayArtist,
                    artworkUri = artistTracks.firstOrNull { it.albumArtUri != null }?.albumArtUri,
                    albumCount = albumCount,
                    trackCount = artistTracks.size
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    /**
     * Sort albums by given order.
     */
    private fun sortAlbums(albums: List<AlbumUiModel>, order: AlbumSortOrder): List<AlbumUiModel> {
        return when (order) {
            AlbumSortOrder.NAME_ASC -> albums.sortedBy { it.name.lowercase() }
            AlbumSortOrder.NAME_DESC -> albums.sortedByDescending { it.name.lowercase() }
            AlbumSortOrder.ARTIST_ASC -> albums.sortedBy { it.artist.lowercase() }
            AlbumSortOrder.ARTIST_DESC -> albums.sortedByDescending { it.artist.lowercase() }
            AlbumSortOrder.YEAR_ASC -> albums.sortedBy { it.year ?: Int.MAX_VALUE }
            AlbumSortOrder.YEAR_DESC -> albums.sortedByDescending { it.year ?: Int.MIN_VALUE }
            AlbumSortOrder.TRACK_COUNT_ASC -> albums.sortedBy { it.trackCount }
            AlbumSortOrder.TRACK_COUNT_DESC -> albums.sortedByDescending { it.trackCount }
        }
    }
}
