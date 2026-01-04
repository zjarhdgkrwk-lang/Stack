package com.stack.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.stack.data.local.db.entity.PlayHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: PlayHistoryEntity): Long

    @Query("""
        SELECT * FROM play_history
        WHERE played_at >= :sinceTimestamp
        ORDER BY played_at DESC
    """)
    fun observeRecentHistory(sinceTimestamp: Long): Flow<List<PlayHistoryEntity>>

    @Query("""
        SELECT DISTINCT track_id FROM play_history
        WHERE played_at >= :sinceTimestamp
        ORDER BY played_at DESC
    """)
    fun observeRecentlyPlayedTrackIds(sinceTimestamp: Long): Flow<List<Long>>

    @Query("""
        SELECT track_id FROM play_history
        GROUP BY track_id
        ORDER BY COUNT(*) DESC
        LIMIT :limit
    """)
    fun observeMostPlayedTrackIds(limit: Int): Flow<List<Long>>

    @Query("""
        SELECT track_id, week_key, COUNT(*) as play_count, SUM(played_duration) as total_duration
        FROM play_history
        WHERE week_key >= :startWeek AND week_key <= :endWeek
        GROUP BY track_id, week_key
    """)
    fun getWeeklyStats(startWeek: String, endWeek: String): Flow<List<WeeklyStatResult>>

    @Query("""
        SELECT COUNT(*) as total_play_count,
               SUM(played_duration) as total_duration,
               MAX(played_at) as last_played,
               MIN(played_at) as first_played
        FROM play_history
        WHERE track_id = :trackId
    """)
    suspend fun getTrackStats(trackId: Long): TrackStatsResult?

    @Query("DELETE FROM play_history WHERE played_at < :beforeTimestamp")
    suspend fun deleteOldRecords(beforeTimestamp: Long)

    @Query("SELECT COUNT(*) FROM play_history")
    suspend fun getTotalPlayCount(): Int
}

data class WeeklyStatResult(
    val track_id: Long,
    val week_key: String,
    val play_count: Int,
    val total_duration: Long
)

data class TrackStatsResult(
    val total_play_count: Int,
    val total_duration: Long,
    val last_played: Long?,
    val first_played: Long?
)
