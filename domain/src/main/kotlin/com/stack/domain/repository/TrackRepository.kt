package com.stack.domain.repository

import com.stack.domain.model.Track
import com.stack.domain.model.TrackSortOrder
import com.stack.domain.model.TrackStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Track operations.
 * Implementations handle Room database operations.
 */
interface TrackRepository {

    /**
     * Observe all active tracks with sorting.
     */
    fun observeTracks(sortOrder: TrackSortOrder = TrackSortOrder.DATE_ADDED_DESC): Flow<List<Track>>

    /**
     * Observe tracks filtered by folder path.
     */
    fun observeTracksByFolder(folderPath: String, sortOrder: TrackSortOrder): Flow<List<Track>>

    /**
     * Observe tracks filtered by album.
     */
    fun observeTracksByAlbum(albumId: Long, sortOrder: TrackSortOrder): Flow<List<Track>>

    /**
     * Observe tracks filtered by artist.
     */
    fun observeTracksByArtist(artistId: Long, sortOrder: TrackSortOrder): Flow<List<Track>>

    /**
     * Observe ghost tracks (for cleanup UI).
     */
    fun observeGhostTracks(): Flow<List<Track>>

    /**
     * Get track by ID.
     */
    suspend fun getTrackById(id: Long): Track?

    /**
     * Get track by content URI.
     */
    suspend fun getTrackByContentUri(contentUri: String): Track?

    /**
     * Insert or update a track (upsert by contentUri).
     */
    suspend fun upsertTrack(track: Track): Long

    /**
     * Insert or update multiple tracks.
     */
    suspend fun upsertTracks(tracks: List<Track>)

    /**
     * Update track status (e.g., mark as GHOST).
     */
    suspend fun updateTrackStatus(trackId: Long, status: TrackStatus)

    /**
     * Delete track by ID.
     */
    suspend fun deleteTrack(trackId: Long)

    /**
     * Delete multiple tracks.
     */
    suspend fun deleteTracks(trackIds: List<Long>)

    /**
     * Delete all ghost tracks.
     */
    suspend fun deleteGhostTracks()

    /**
     * Get total track count.
     */
    suspend fun getTrackCount(): Int

    /**
     * Get track count by status.
     */
    suspend fun getTrackCountByStatus(status: TrackStatus): Int

    /**
     * Search tracks by query (uses FTS).
     */
    fun searchTracks(query: String): Flow<List<Track>>

    /**
     * Get all unique albums.
     */
    fun observeAlbums(): Flow<List<AlbumInfo>>

    /**
     * Get all unique artists.
     */
    fun observeArtists(): Flow<List<ArtistInfo>>

    /**
     * Get all unique folder paths.
     */
    fun observeFolders(): Flow<List<FolderInfo>>
}

/**
 * Album info for album list display.
 */
data class AlbumInfo(
    val albumId: Long?,
    val album: String,
    val artist: String?,
    val albumArtUri: String?,
    val trackCount: Int,
    val totalDuration: Long
)

/**
 * Artist info for artist list display.
 */
data class ArtistInfo(
    val artistId: Long?,
    val artist: String,
    val albumCount: Int,
    val trackCount: Int
)

/**
 * Folder info for folder browser.
 */
data class FolderInfo(
    val folderPath: String,
    val displayPath: String,
    val trackCount: Int
)
