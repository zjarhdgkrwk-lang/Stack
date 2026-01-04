package com.stack.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.stack.data.local.db.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackFtsDao {

    /**
     * Full-text search on tracks (title, artist, album, album_artist).
     * Uses FTS4 MATCH syntax.
     */
    @Query("""
        SELECT tracks.* FROM tracks
        JOIN tracks_fts ON tracks.rowid = tracks_fts.rowid
        WHERE tracks_fts MATCH :query AND tracks.status = 'ACTIVE'
        ORDER BY tracks.title ASC
    """)
    fun search(query: String): Flow<List<TrackEntity>>

    /**
     * Search with prefix matching (for autocomplete).
     */
    @Query("""
        SELECT tracks.* FROM tracks
        JOIN tracks_fts ON tracks.rowid = tracks_fts.rowid
        WHERE tracks_fts MATCH :query || '*' AND tracks.status = 'ACTIVE'
        ORDER BY tracks.title ASC
        LIMIT :limit
    """)
    fun searchWithPrefix(query: String, limit: Int = 20): Flow<List<TrackEntity>>
}
