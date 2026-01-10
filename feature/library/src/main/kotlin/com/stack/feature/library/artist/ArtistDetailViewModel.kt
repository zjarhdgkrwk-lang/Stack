package com.stack.feature.library.artist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stack.core.player.StackPlayerManager
import com.stack.domain.usecase.artist.GetArtistAlbumsUseCase
import com.stack.domain.usecase.artist.GetArtistTracksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Artist Detail screen (Phase 5.1)
 */
@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    private val getArtistAlbumsUseCase: GetArtistAlbumsUseCase,
    private val getArtistTracksUseCase: GetArtistTracksUseCase,
    private val playerManager: StackPlayerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArtistDetailUiState())
    val uiState: StateFlow<ArtistDetailUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ArtistDetailEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: ArtistDetailIntent) {
        when (intent) {
            is ArtistDetailIntent.Load -> loadArtist(intent.artistId)
            is ArtistDetailIntent.PlayAll -> playAll(shuffle = false)
            is ArtistDetailIntent.ShufflePlay -> playAll(shuffle = true)
            is ArtistDetailIntent.PlayTrack -> playTrack(intent.index)
            is ArtistDetailIntent.OpenAlbum -> openAlbum(intent.albumId)
            is ArtistDetailIntent.NavigateBack -> navigateBack()
        }
    }

    private fun loadArtist(artistId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, artistId = artistId) }

            // Load albums and tracks in parallel
            combine(
                getArtistAlbumsUseCase(artistId),
                getArtistTracksUseCase(artistId)
            ) { albums, tracks ->
                Pair(albums, tracks)
            }
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to load artist"
                        )
                    }
                }
                .collect { (albums, tracks) ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            artistName = tracks.firstOrNull()?.artist ?: "Unknown Artist",
                            albumCount = albums.size,
                            trackCount = tracks.size,
                            albums = albums,
                            tracks = tracks
                        )
                    }
                }
        }
    }

    private fun playAll(shuffle: Boolean) {
        val tracks = _uiState.value.tracks
        if (tracks.isEmpty()) return

        viewModelScope.launch {
            val queue = if (shuffle) tracks.shuffled() else tracks
            playerManager.play(queue.first(), queue)
        }
    }

    private fun playTrack(index: Int) {
        val tracks = _uiState.value.tracks
        if (index !in tracks.indices) return

        viewModelScope.launch {
            playerManager.play(tracks[index], tracks)
        }
    }

    private fun openAlbum(albumId: Long) {
        viewModelScope.launch {
            _effect.send(ArtistDetailEffect.NavigateToAlbum(albumId))
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _effect.send(ArtistDetailEffect.NavigateBack)
        }
    }
}
