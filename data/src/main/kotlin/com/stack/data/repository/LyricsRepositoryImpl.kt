package com.stack.data.repository

import com.stack.data.local.db.dao.LyricsDao
import com.stack.data.local.db.entity.LyricsEntity
import com.stack.data.mapper.LyricsMapper
import com.stack.domain.model.Lyrics
import com.stack.domain.model.LyricsSource
import com.stack.domain.repository.LyricsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsRepositoryImpl @Inject constructor(
    private val lyricsDao: LyricsDao
) : LyricsRepository {

    override fun observeLyrics(trackId: Long): Flow<Lyrics?> {
        return lyricsDao.observeByTrackId(trackId).map { entity ->
            entity?.let { LyricsMapper.toDomain(it) }
        }
    }

    override suspend fun getLyrics(trackId: Long): Lyrics? {
        return lyricsDao.getByTrackId(trackId)?.let { entity ->
            LyricsMapper.toDomain(entity)
        }
    }

    override suspend fun saveLyrics(
        trackId: Long,
        content: String,
        isSynced: Boolean,
        lrcData: String?,
        source: LyricsSource
    ): Long {
        val now = System.currentTimeMillis()
        val entity = LyricsEntity(
            trackId = trackId,
            content = content,
            isSynced = isSynced,
            lrcData = lrcData,
            source = source,
            createdAt = now,
            updatedAt = now
        )
        return lyricsDao.insert(entity)
    }

    override suspend fun deleteLyrics(trackId: Long) {
        lyricsDao.deleteByTrackId(trackId)
    }

    override fun searchLyrics(query: String): Flow<List<Lyrics>> {
        // TODO: Implement FTS search with LyricsFtsDao
        // For now, returning empty flow as LyricsFtsDao is not yet implemented
        return flowOf(emptyList())
    }

    override suspend fun hasLyrics(trackId: Long): Boolean {
        return lyricsDao.hasLyrics(trackId)
    }
}
