package com.stack.data.repository

import com.stack.data.local.db.dao.TrackDao
import com.stack.data.local.db.dao.TrackFtsDao
import com.stack.data.mapper.TrackMapper
import com.stack.domain.model.Album
import com.stack.domain.model.Track
import com.stack.domain.model.TrackSortOrder
import com.stack.domain.model.TrackStatus
import com.stack.domain.repository.AlbumInfo
import com.stack.domain.repository.ArtistInfo
import com.stack.domain.repository.FolderInfo
import com.stack.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackRepositoryImpl @Inject constructor(
    private val trackDao: TrackDao,
    private val trackFtsDao: TrackFtsDao
) : TrackRepository {

    override fun observeTracks(sortOrder: TrackSortOrder): Flow<List<Track>> {
        return when (sortOrder) {
            TrackSortOrder.TITLE_ASC -> trackDao.observeTracksByTitleAsc()
            TrackSortOrder.TITLE_DESC -> trackDao.observeTracksByTitleDesc()
            TrackSortOrder.ARTIST_ASC -> trackDao.observeTracksByArtistAsc()
            TrackSortOrder.ARTIST_DESC -> trackDao.observeTracksByArtistDesc()
            TrackSortOrder.DATE_ADDED_ASC -> trackDao.observeTracksByDateAddedAsc()
            TrackSortOrder.DATE_ADDED_DESC -> trackDao.observeTracksByDateAddedDesc()
        }.map { entities ->
            TrackMapper.toDomainList(entities)
        }
    }

    override fun observeTracksByFolder(folderPath: String, sortOrder: TrackSortOrder): Flow<List<Track>> {
        return trackDao.observeTracksByFolder(folderPath).map { entities ->
            val tracks = TrackMapper.toDomainList(entities)
            applySorting(tracks, sortOrder)
        }
    }

    override fun observeTracksByAlbum(albumId: Long, sortOrder: TrackSortOrder): Flow<List<Track>> {
        return trackDao.observeTracksByAlbum(albumId).map { entities ->
            val tracks = TrackMapper.toDomainList(entities)
            applySorting(tracks, sortOrder)
        }
    }

    override fun observeTracksByArtist(artistId: Long, sortOrder: TrackSortOrder): Flow<List<Track>> {
        return trackDao.observeTracksByArtist(artistId).map { entities ->
            val tracks = TrackMapper.toDomainList(entities)
            applySorting(tracks, sortOrder)
        }
    }

    override fun observeGhostTracks(): Flow<List<Track>> {
        return trackDao.observeGhostTracks().map { entities ->
            TrackMapper.toDomainList(entities)
        }
    }

    override suspend fun getTrackById(id: Long): Track? {
        return trackDao.getById(id)?.let { entity ->
            TrackMapper.toDomain(entity)
        }
    }

    override suspend fun getTrackByContentUri(contentUri: String): Track? {
        return trackDao.getByContentUri(contentUri)?.let { entity ->
            TrackMapper.toDomain(entity)
        }
    }

    override suspend fun upsertTrack(track: Track): Long {
        val entity = TrackMapper.toEntity(track)
        return trackDao.insert(entity)
    }

    override suspend fun upsertTracks(tracks: List<Track>) {
        val entities = TrackMapper.toEntityList(tracks)
        trackDao.insertAll(entities)
    }

    override suspend fun updateTrackStatus(trackId: Long, status: TrackStatus) {
        trackDao.updateStatus(trackId, status)
    }

    override suspend fun deleteTrack(trackId: Long) {
        trackDao.deleteById(trackId)
    }

    override suspend fun deleteTracks(trackIds: List<Long>) {
        trackDao.deleteByIds(trackIds)
    }

    override suspend fun deleteGhostTracks() {
        trackDao.deleteGhostTracks()
    }

    override suspend fun getTrackCount(): Int {
        return trackDao.getTrackCount()
    }

    override suspend fun getTrackCountByStatus(status: TrackStatus): Int {
        return trackDao.getTrackCountByStatus(status)
    }

    override fun searchTracks(query: String): Flow<List<Track>> {
        return trackFtsDao.search(query).map { entities ->
            TrackMapper.toDomainList(entities)
        }
    }

    override fun observeAlbums(): Flow<List<AlbumInfo>> {
        return trackDao.observeAlbums().map { results ->
            results.map { result ->
                AlbumInfo(
                    albumId = result.album_id,
                    album = result.album ?: "",
                    artist = result.artist,
                    albumArtUri = result.album_art_uri,
                    trackCount = result.track_count,
                    totalDuration = result.total_duration
                )
            }
        }
    }

    override fun observeArtists(): Flow<List<ArtistInfo>> {
        return trackDao.observeArtists().map { results ->
            results.map { result ->
                ArtistInfo(
                    artistId = result.artist_id,
                    artist = result.artist ?: "",
                    albumCount = result.album_count,
                    trackCount = result.track_count
                )
            }
        }
    }

    override fun observeFolders(): Flow<List<FolderInfo>> {
        return trackDao.observeFolders().map { results ->
            results.map { result ->
                FolderInfo(
                    folderPath = result.folder_path,
                    displayPath = result.display_path,
                    trackCount = result.track_count
                )
            }
        }
    }

    // ===== Phase 5.1: Detail views =====

    override fun observeAlbumWithTracks(albumId: Long): Flow<Pair<Album, List<Track>>> {
        return trackDao.observeTracksByAlbum(albumId).map { entities ->
            val tracks = TrackMapper.toDomainList(entities)
            val firstTrack = tracks.firstOrNull()

            val album = Album(
                id = albumId,
                name = firstTrack?.album ?: "",
                artistId = firstTrack?.artistId ?: 0L,
                artistName = firstTrack?.artist ?: "Unknown Artist",
                artworkUri = firstTrack?.albumArtUri,
                year = firstTrack?.year,
                trackCount = tracks.size
            )

            Pair(album, tracks)
        }
    }

    override fun observeAlbumsByArtist(artistId: Long): Flow<List<Album>> {
        return trackDao.observeAlbumsByArtist(artistId).map { results ->
            results.mapNotNull { result ->
                val id = result.album_id ?: return@mapNotNull null
                Album(
                    id = id,
                    name = result.album ?: "",
                    artistId = result.artist_id ?: artistId,
                    artistName = result.artist ?: "",
                    artworkUri = result.album_art_uri,
                    year = result.year,
                    trackCount = result.track_count
                )
            }
        }
    }

    /**
     * Apply sorting to a list of tracks in memory.
     */
    private fun applySorting(tracks: List<Track>, sortOrder: TrackSortOrder): List<Track> {
        return when (sortOrder) {
            TrackSortOrder.TITLE_ASC -> tracks.sortedBy { it.title }
            TrackSortOrder.TITLE_DESC -> tracks.sortedByDescending { it.title }
            TrackSortOrder.ARTIST_ASC -> tracks.sortedBy { it.artist }
            TrackSortOrder.ARTIST_DESC -> tracks.sortedByDescending { it.artist }
            TrackSortOrder.DATE_ADDED_ASC -> tracks.sortedBy { it.dateAdded }
            TrackSortOrder.DATE_ADDED_DESC -> tracks.sortedByDescending { it.dateAdded }
        }
    }
}
