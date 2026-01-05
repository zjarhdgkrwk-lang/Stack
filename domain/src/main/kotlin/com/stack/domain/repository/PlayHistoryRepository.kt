package com.stack.domain.repository

import com.stack.domain.model.PlayHistory
import com.stack.domain.model.TrackPlayStats
import com.stack.domain.model.WeeklyStat
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository interface for Play History operations (SSOT 6.4).
 */
interface PlayHistoryRepository {

    /**
     * Record a play event.
     */
    suspend fun recordPlay(
        trackId: Long,
        playedDuration: Long,
        completed: Boolean
    )

    /**
     * Get detailed history for recent days (default 30).
     */
    fun getDetailedHistory(days: Int = 30): Flow<List<PlayHistory>>

    /**
     * Get weekly aggregated stats for a date range.
     */
    fun getWeeklyStats(startDate: LocalDate, endDate: LocalDate): Flow<List<WeeklyStat>>

    /**
     * Get play stats for a specific track.
     */
    suspend fun getTrackStats(trackId: Long): TrackPlayStats?

    /**
     * Get recently played tracks (for system tag).
     */
    fun observeRecentlyPlayed(days: Int = 30): Flow<List<Long>>

    /**
     * Get most played track IDs (for system tag).
     */
    fun observeMostPlayed(limit: Int = 100): Flow<List<Long>>

    /**
     * Clean up old records (SSOT 6.4: 365 days retention).
     */
    suspend fun cleanupOldRecords()

    /**
     * Get total play count for all tracks.
     */
    suspend fun getTotalPlayCount(): Int
}
