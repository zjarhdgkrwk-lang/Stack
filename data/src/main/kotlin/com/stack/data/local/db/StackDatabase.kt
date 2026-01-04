package com.stack.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.stack.data.local.db.converter.Converters
import com.stack.data.local.db.dao.LyricsDao
import com.stack.data.local.db.dao.PlayHistoryDao
import com.stack.data.local.db.dao.PlaylistDao
import com.stack.data.local.db.dao.SourceFolderDao
import com.stack.data.local.db.dao.TagDao
import com.stack.data.local.db.dao.TrackDao
import com.stack.data.local.db.dao.TrackFtsDao
import com.stack.data.local.db.entity.LyricsEntity
import com.stack.data.local.db.entity.LyricsFtsEntity
import com.stack.data.local.db.entity.PlayHistoryEntity
import com.stack.data.local.db.entity.PlaylistEntity
import com.stack.data.local.db.entity.PlaylistTrackCrossRef
import com.stack.data.local.db.entity.SourceFolderEntity
import com.stack.data.local.db.entity.TagEntity
import com.stack.data.local.db.entity.TrackEntity
import com.stack.data.local.db.entity.TrackFtsEntity
import com.stack.data.local.db.entity.TrackTagCrossRef

/**
 * Room Database for Stack app.
 *
 * SSOT 6.1:
 * - DB name: stack_database
 * - Schema version: 1
 * - FTS tokenizer: unicode61 (Room default for FTS4)
 */
@Database(
    entities = [
        // Core entities
        TrackEntity::class,
        TrackFtsEntity::class,
        TagEntity::class,
        TrackTagCrossRef::class,
        PlaylistEntity::class,
        PlaylistTrackCrossRef::class,
        LyricsEntity::class,
        LyricsFtsEntity::class,
        PlayHistoryEntity::class,
        SourceFolderEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class StackDatabase : RoomDatabase() {

    abstract fun trackDao(): TrackDao
    abstract fun trackFtsDao(): TrackFtsDao
    abstract fun tagDao(): TagDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun lyricsDao(): LyricsDao
    abstract fun playHistoryDao(): PlayHistoryDao
    abstract fun sourceFolderDao(): SourceFolderDao

    companion object {
        const val DATABASE_NAME = "stack_database"
    }
}
