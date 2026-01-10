package com.stack.feature.library.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stack.core.player.StackPlayerManager
import com.stack.domain.usecase.album.GetAlbumWithTracksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Album Detail screen (Phase 5.1)
 */
@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    private val getAlbumWithTracksUseCase: GetAlbumWithTracksUseCase,
    private val playerManager: StackPlayerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlbumDetailUiState())
    val uiState: StateFlow<AlbumDetailUiState> = _uiState.asStateFlow()

    private val _effect = Channel<AlbumDetailEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: AlbumDetailIntent) {
        when (intent) {
            is AlbumDetailIntent.Load -> loadAlbum(intent.albumId)
            is AlbumDetailIntent.PlayAll -> playAll(shuffle = false)
            is AlbumDetailIntent.ShufflePlay -> playAll(shuffle = true)
            is AlbumDetailIntent.PlayTrack -> playTrack(intent.index)
            is AlbumDetailIntent.NavigateToArtist -> navigateToArtist()
            is AlbumDetailIntent.NavigateBack -> navigateBack()
        }
    }

    private fun loadAlbum(albumId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, albumId = albumId) }

            getAlbumWithTracksUseCase(albumId)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to load album"
                        )
                    }
                }
                .collect { (album, tracks) ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            albumId = album.id,
                            albumName = album.name,
                            artistName = album.artistName,
                            artistId = album.artistId,
                            albumArtUri = album.artworkUri,
                            year = album.year,
                            trackCount = tracks.size,
                            totalDuration = tracks.sumOf { track -> track.duration },
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
            // Play the track at the given index with the full album as the queue
            playerManager.play(tracks[index], tracks)
        }
    }

    private fun navigateToArtist() {
        val artistId = _uiState.value.artistId
        if (artistId > 0) {
            viewModelScope.launch {
                _effect.send(AlbumDetailEffect.NavigateToArtist(artistId))
            }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _effect.send(AlbumDetailEffect.NavigateBack)
        }
    }
}
