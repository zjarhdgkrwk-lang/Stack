package com.stack.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.stack.data.local.db.entity.PlaylistEntity
import com.stack.data.local.db.entity.PlaylistTrackCrossRef
import com.stack.data.local.db.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    // ===== Playlists =====

    @Query("SELECT * FROM playlists ORDER BY updated_at DESC")
    fun observeAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    fun observePlaylist(id: Long): Flow<PlaylistEntity?>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getById(id: Long): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playlist: PlaylistEntity): Long

    @Update
    suspend fun update(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deleteById(playlistId: Long)

    @Query("SELECT COUNT(*) FROM playlists")
    suspend fun getPlaylistCount(): Int

    // ===== Playlist-Track Relations =====

    @Query("""
        SELECT tracks.* FROM tracks
        INNER JOIN playlist_track_cross_ref ON tracks.id = playlist_track_cross_ref.track_id
        WHERE playlist_track_cross_ref.playlist_id = :playlistId
        ORDER BY playlist_track_cross_ref.position ASC
    """)
    fun observeTracksForPlaylist(playlistId: Long): Flow<List<TrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: PlaylistTrackCrossRef)

    @Query("DELETE FROM playlist_track_cross_ref WHERE playlist_id = :playlistId AND track_id = :trackId")
    suspend fun deleteCrossRef(playlistId: Long, trackId: Long)

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM playlist_track_cross_ref WHERE playlist_id = :playlistId")
    suspend fun getNextPosition(playlistId: Long): Int

    @Query("SELECT COUNT(*) FROM playlist_track_cross_ref WHERE playlist_id = :playlistId")
    suspend fun getTrackCount(playlistId: Long): Int

    @Query("""
        SELECT COALESCE(SUM(tracks.duration), 0) FROM tracks
        INNER JOIN playlist_track_cross_ref ON tracks.id = playlist_track_cross_ref.track_id
        WHERE playlist_track_cross_ref.playlist_id = :playlistId
    """)
    suspend fun getTotalDuration(playlistId: Long): Long

    @Query("UPDATE playlist_track_cross_ref SET position = :newPosition WHERE playlist_id = :playlistId AND track_id = :trackId")
    suspend fun updatePosition(playlistId: Long, trackId: Long, newPosition: Int)

    @Query("SELECT * FROM playlist_track_cross_ref WHERE playlist_id = :playlistId ORDER BY position ASC")
    suspend fun getCrossRefs(playlistId: Long): List<PlaylistTrackCrossRef>
}
