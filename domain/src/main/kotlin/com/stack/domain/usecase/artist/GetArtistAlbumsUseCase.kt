package com.stack.domain.usecase.artist

import com.stack.domain.model.Album
import com.stack.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get all albums by an artist for artist detail screen.
 * Phase 5.1
 */
class GetArtistAlbumsUseCase @Inject constructor(
    private val trackRepository: TrackRepository
) {
    operator fun invoke(artistId: Long): Flow<List<Album>> {
        return trackRepository.observeAlbumsByArtist(artistId)
    }
}
