package com.stack.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.stack.data.local.db.entity.LyricsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LyricsDao {

    @Query("SELECT * FROM lyrics WHERE track_id = :trackId")
    fun observeByTrackId(trackId: Long): Flow<LyricsEntity?>

    @Query("SELECT * FROM lyrics WHERE track_id = :trackId")
    suspend fun getByTrackId(trackId: Long): LyricsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(lyrics: LyricsEntity): Long

    @Query("DELETE FROM lyrics WHERE track_id = :trackId")
    suspend fun deleteByTrackId(trackId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM lyrics WHERE track_id = :trackId)")
    suspend fun hasLyrics(trackId: Long): Boolean
}
