package com.stack.domain.repository

import com.stack.domain.model.Lyrics
import com.stack.domain.model.LyricsSource
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Lyrics operations.
 */
interface LyricsRepository {

    /**
     * Observe lyrics for a track.
     */
    fun observeLyrics(trackId: Long): Flow<Lyrics?>

    /**
     * Get lyrics for a track.
     */
    suspend fun getLyrics(trackId: Long): Lyrics?

    /**
     * Save or update lyrics.
     */
    suspend fun saveLyrics(
        trackId: Long,
        content: String,
        isSynced: Boolean,
        lrcData: String?,
        source: LyricsSource
    ): Long

    /**
     * Delete lyrics for a track.
     */
    suspend fun deleteLyrics(trackId: Long)

    /**
     * Search lyrics content (uses FTS).
     */
    fun searchLyrics(query: String): Flow<List<Lyrics>>

    /**
     * Check if track has lyrics.
     */
    suspend fun hasLyrics(trackId: Long): Boolean
}
