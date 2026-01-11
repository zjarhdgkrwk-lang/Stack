package com.stack.domain.usecase.album

import com.stack.domain.model.Album
import com.stack.domain.model.Track
import com.stack.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get an album with all its tracks for detail screen.
 * Phase 5.1
 */
class GetAlbumWithTracksUseCase @Inject constructor(
    private val trackRepository: TrackRepository
) {
    operator fun invoke(albumId: Long): Flow<Pair<Album, List<Track>>> {
        return trackRepository.observeAlbumWithTracks(albumId)
    }
}
