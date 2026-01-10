package com.stack.domain.usecase.artist

import com.stack.domain.model.Track
import com.stack.domain.model.TrackSortOrder
import com.stack.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get all tracks by an artist for artist detail screen.
 * Phase 5.1
 */
class GetArtistTracksUseCase @Inject constructor(
    private val trackRepository: TrackRepository
) {
    operator fun invoke(artistId: Long): Flow<List<Track>> {
        // Use existing observeTracksByArtist with default sort order
        // The DAO already orders by year DESC, album ASC, disc/track ASC
        return trackRepository.observeTracksByArtist(artistId, TrackSortOrder.DATE_ADDED_DESC)
    }
}
