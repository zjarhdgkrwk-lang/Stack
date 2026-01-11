package com.stack.data.repository

import com.stack.data.local.db.dao.PlayHistoryDao
import com.stack.data.local.db.entity.PlayHistoryEntity
import com.stack.data.mapper.PlayHistoryMapper
import com.stack.domain.model.PlayHistory
import com.stack.domain.model.TrackPlayStats
import com.stack.domain.model.WeeklyStat
import com.stack.domain.repository.PlayHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayHistoryRepositoryImpl @Inject constructor(
    private val playHistoryDao: PlayHistoryDao
) : PlayHistoryRepository {

    override suspend fun recordPlay(
        trackId: Long,
        playedDuration: Long,
        completed: Boolean
    ) {
        val now = System.currentTimeMillis()
        val weekKey = getWeekKeyFromTimestamp(now)

        val entity = PlayHistoryEntity(
            trackId = trackId,
            playedAt = now,
            playedDuration = playedDuration,
            completed = completed,
            weekKey = weekKey
        )

        playHistoryDao.insert(entity)
    }

    override fun getDetailedHistory(days: Int): Flow<List<PlayHistory>> {
        val sinceTimestamp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong())
        return playHistoryDao.observeRecentHistory(sinceTimestamp).map { entities ->
            PlayHistoryMapper.toDomainList(entities)
        }
    }

    override fun getWeeklyStats(startDate: LocalDate, endDate: LocalDate): Flow<List<WeeklyStat>> {
        val startWeek = getWeekKeyFromLocalDate(startDate)
        val endWeek = getWeekKeyFromLocalDate(endDate)

        return playHistoryDao.getWeeklyStats(startWeek, endWeek).map { results ->
            results.map { result ->
                WeeklyStat(
                    weekKey = result.week_key,
                    trackId = result.track_id,
                    playCount = result.play_count,
                    totalDuration = result.total_duration
                )
            }
        }
    }

    override suspend fun getTrackStats(trackId: Long): TrackPlayStats? {
        val result = playHistoryDao.getTrackStats(trackId) ?: return null

        return TrackPlayStats(
            trackId = trackId,
            totalPlayCount = result.total_play_count,
            totalPlayDuration = result.total_duration,
            lastPlayedAt = result.last_played,
            firstPlayedAt = result.first_played
        )
    }

    override fun observeRecentlyPlayed(days: Int): Flow<List<Long>> {
        val sinceTimestamp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong())
        return playHistoryDao.observeRecentlyPlayedTrackIds(sinceTimestamp)
    }

    override fun observeMostPlayed(limit: Int): Flow<List<Long>> {
        return playHistoryDao.observeMostPlayedTrackIds(limit)
    }

    override suspend fun cleanupOldRecords() {
        val retentionDays = 365L
        val beforeTimestamp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(retentionDays)
        playHistoryDao.deleteOldRecords(beforeTimestamp)
    }

    override suspend fun getTotalPlayCount(): Int {
        return playHistoryDao.getTotalPlayCount()
    }

    /**
     * Convert LocalDate to week key format (YYYY-Www).
     */
    private fun getWeekKeyFromLocalDate(date: LocalDate): String {
        val weekFields = WeekFields.of(Locale.getDefault())
        val year = date.get(weekFields.weekBasedYear())
        val week = date.get(weekFields.weekOfWeekBasedYear())
        return String.format("%04d-W%02d", year, week)
    }

    /**
     * Convert timestamp to week key format (YYYY-Www).
     */
    private fun getWeekKeyFromTimestamp(timestamp: Long): String {
        val date = LocalDate.ofEpochDay(TimeUnit.MILLISECONDS.toDays(timestamp))
        return getWeekKeyFromLocalDate(date)
    }
}
