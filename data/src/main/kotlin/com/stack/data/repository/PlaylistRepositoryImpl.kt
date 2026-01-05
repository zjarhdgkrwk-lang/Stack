package com.stack.data.repository

import com.stack.data.local.db.dao.PlaylistDao
import com.stack.data.local.db.entity.PlaylistEntity
import com.stack.data.local.db.entity.PlaylistTrackCrossRef
import com.stack.data.mapper.PlaylistMapper
import com.stack.data.mapper.TrackMapper
import com.stack.domain.model.Playlist
import com.stack.domain.model.PlaylistSortOrder
import com.stack.domain.model.Track
import com.stack.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao
) : PlaylistRepository {

    override fun observePlaylists(sortOrder: PlaylistSortOrder): Flow<List<Playlist>> {
        return playlistDao.observeAllPlaylists().map { entities ->
            val playlists = PlaylistMapper.toDomainList(entities)
            applySorting(playlists, sortOrder)
        }
    }

    override fun observePlaylist(playlistId: Long): Flow<Playlist?> {
        return combine(
            playlistDao.observePlaylist(playlistId),
            playlistDao.observeTracksForPlaylist(playlistId)
        ) { entity, tracks ->
            entity?.let {
                PlaylistMapper.toDomain(it).copy(
                    trackCount = tracks.size,
                    totalDuration = tracks.sumOf { track -> track.duration }
                )
            }
        }
    }

    override fun observePlaylistTracks(playlistId: Long): Flow<List<Track>> {
        return playlistDao.observeTracksForPlaylist(playlistId).map { entities ->
            TrackMapper.toDomainList(entities)
        }
    }

    override suspend fun getPlaylistById(id: Long): Playlist? {
        val entity = playlistDao.getById(id) ?: return null
        val trackCount = playlistDao.getTrackCount(id)
        val totalDuration = playlistDao.getTotalDuration(id)
        return PlaylistMapper.toDomain(entity).copy(
            trackCount = trackCount,
            totalDuration = totalDuration
        )
    }

    override suspend fun createPlaylist(name: String, description: String?): Long {
        val now = System.currentTimeMillis()
        val entity = PlaylistEntity(
            name = name,
            description = description,
            coverArtUri = null,
            createdAt = now,
            updatedAt = now
        )
        return playlistDao.insert(entity)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        val entity = PlaylistMapper.toEntity(playlist).copy(
            updatedAt = System.currentTimeMillis()
        )
        playlistDao.update(entity)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deleteById(playlistId)
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        val position = playlistDao.getNextPosition(playlistId)
        val crossRef = PlaylistTrackCrossRef(
            playlistId = playlistId,
            trackId = trackId,
            position = position,
            addedAt = System.currentTimeMillis()
        )
        playlistDao.insertCrossRef(crossRef)
        updatePlaylistTimestamp(playlistId)
    }

    override suspend fun addTracksToPlaylist(playlistId: Long, trackIds: List<Long>) {
        var position = playlistDao.getNextPosition(playlistId)
        val now = System.currentTimeMillis()

        trackIds.forEach { trackId ->
            val crossRef = PlaylistTrackCrossRef(
                playlistId = playlistId,
                trackId = trackId,
                position = position++,
                addedAt = now
            )
            playlistDao.insertCrossRef(crossRef)
        }
        updatePlaylistTimestamp(playlistId)
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        playlistDao.deleteCrossRef(playlistId, trackId)

        // Reorder remaining tracks to fill the gap
        val crossRefs = playlistDao.getCrossRefs(playlistId)
        crossRefs.forEachIndexed { index, crossRef ->
            if (crossRef.position != index) {
                playlistDao.updatePosition(playlistId, crossRef.trackId, index)
            }
        }

        updatePlaylistTimestamp(playlistId)
    }

    override suspend fun reorderPlaylistTracks(playlistId: Long, fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) return

        val crossRefs = playlistDao.getCrossRefs(playlistId).toMutableList()

        if (fromPosition < 0 || fromPosition >= crossRefs.size ||
            toPosition < 0 || toPosition >= crossRefs.size) {
            return
        }

        // Reorder the list
        val item = crossRefs.removeAt(fromPosition)
        crossRefs.add(toPosition, item)

        // Update positions in database
        crossRefs.forEachIndexed { index, crossRef ->
            playlistDao.updatePosition(playlistId, crossRef.trackId, index)
        }

        updatePlaylistTimestamp(playlistId)
    }

    override suspend fun getPlaylistCount(): Int {
        return playlistDao.getPlaylistCount()
    }

    /**
     * Update playlist's updatedAt timestamp.
     */
    private suspend fun updatePlaylistTimestamp(playlistId: Long) {
        val playlist = playlistDao.getById(playlistId) ?: return
        playlistDao.update(playlist.copy(updatedAt = System.currentTimeMillis()))
    }

    /**
     * Apply sorting to a list of playlists.
     */
    private fun applySorting(playlists: List<Playlist>, sortOrder: PlaylistSortOrder): List<Playlist> {
        return when (sortOrder) {
            PlaylistSortOrder.NAME_ASC -> playlists.sortedBy { it.name }
            PlaylistSortOrder.NAME_DESC -> playlists.sortedByDescending { it.name }
            PlaylistSortOrder.CREATED_ASC -> playlists.sortedBy { it.createdAt }
            PlaylistSortOrder.CREATED_DESC -> playlists.sortedByDescending { it.createdAt }
            PlaylistSortOrder.UPDATED_ASC -> playlists.sortedBy { it.updatedAt }
            PlaylistSortOrder.UPDATED_DESC -> playlists.sortedByDescending { it.updatedAt }
        }
    }
}
