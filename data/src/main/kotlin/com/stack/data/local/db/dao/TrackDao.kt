package com.stack.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.stack.data.local.db.entity.TrackEntity
import com.stack.domain.model.TrackStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    // ===== Observe =====

    @Query("SELECT * FROM tracks WHERE status = 'ACTIVE' ORDER BY date_added DESC")
    fun observeAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE status = 'ACTIVE' ORDER BY title ASC")
    fun observeTracksByTitleAsc(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE status = 'ACTIVE' ORDER BY title DESC")
    fun observeTracksByTitleDesc(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE status = 'ACTIVE' ORDER BY artist ASC")
    fun observeTracksByArtistAsc(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE status = 'ACTIVE' ORDER BY artist DESC")
    fun observeTracksByArtistDesc(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE status = 'ACTIVE' ORDER BY date_added ASC")
    fun observeTracksByDateAddedAsc(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE status = 'ACTIVE' ORDER BY date_added DESC")
    fun observeTracksByDateAddedDesc(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE folder_path = :folderPath AND status = 'ACTIVE'")
    fun observeTracksByFolder(folderPath: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE album_id = :albumId AND status = 'ACTIVE' ORDER BY track_number ASC")
    fun observeTracksByAlbum(albumId: Long): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE artist_id = :artistId AND status = 'ACTIVE'")
    fun observeTracksByArtist(artistId: Long): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE status = 'GHOST'")
    fun observeGhostTracks(): Flow<List<TrackEntity>>

    // ===== Single Fetch =====

    @Query("SELECT * FROM tracks WHERE id = :id")
    suspend fun getById(id: Long): TrackEntity?

    @Query("SELECT * FROM tracks WHERE content_uri = :contentUri")
    suspend fun getByContentUri(contentUri: String): TrackEntity?

    // ===== Insert/Update =====

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(track: TrackEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tracks: List<TrackEntity>)

    @Update
    suspend fun update(track: TrackEntity)

    @Query("UPDATE tracks SET status = :status WHERE id = :trackId")
    suspend fun updateStatus(trackId: Long, status: TrackStatus)

    @Query("UPDATE tracks SET last_scanned = :timestamp WHERE id = :trackId")
    suspend fun updateLastScanned(trackId: Long, timestamp: Long)

    // ===== Delete =====

    @Query("DELETE FROM tracks WHERE id = :trackId")
    suspend fun deleteById(trackId: Long)

    @Query("DELETE FROM tracks WHERE id IN (:trackIds)")
    suspend fun deleteByIds(trackIds: List<Long>)

    @Query("DELETE FROM tracks WHERE status = 'GHOST'")
    suspend fun deleteGhostTracks()

    // ===== Count =====

    @Query("SELECT COUNT(*) FROM tracks")
    suspend fun getTrackCount(): Int

    @Query("SELECT COUNT(*) FROM tracks WHERE status = :status")
    suspend fun getTrackCountByStatus(status: TrackStatus): Int

    // ===== Album/Artist/Folder aggregation =====

    @Query("""
        SELECT album_id, album, artist, album_art_uri,
               COUNT(*) as track_count, SUM(duration) as total_duration
        FROM tracks
        WHERE status = 'ACTIVE' AND album IS NOT NULL
        GROUP BY album_id, album
        ORDER BY album ASC
    """)
    fun observeAlbums(): Flow<List<AlbumQueryResult>>

    @Query("""
        SELECT artist_id, artist, COUNT(DISTINCT album_id) as album_count, COUNT(*) as track_count
        FROM tracks
        WHERE status = 'ACTIVE' AND artist IS NOT NULL
        GROUP BY artist_id, artist
        ORDER BY artist ASC
    """)
    fun observeArtists(): Flow<List<ArtistQueryResult>>

    @Query("""
        SELECT folder_path, display_path, COUNT(*) as track_count
        FROM tracks
        WHERE status = 'ACTIVE'
        GROUP BY folder_path
        ORDER BY display_path ASC
    """)
    fun observeFolders(): Flow<List<FolderQueryResult>>
}

/**
 * Query result for album aggregation.
 */
data class AlbumQueryResult(
    val album_id: Long?,
    val album: String?,
    val artist: String?,
    val album_art_uri: String?,
    val track_count: Int,
    val total_duration: Long
)

/**
 * Query result for artist aggregation.
 */
data class ArtistQueryResult(
    val artist_id: Long?,
    val artist: String?,
    val album_count: Int,
    val track_count: Int
)

/**
 * Query result for folder aggregation.
 */
data class FolderQueryResult(
    val folder_path: String,
    val display_path: String,
    val track_count: Int
)
