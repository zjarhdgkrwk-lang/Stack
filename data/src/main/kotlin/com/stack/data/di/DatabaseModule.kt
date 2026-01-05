package com.stack.data.di

import android.content.Context
import androidx.room.Room
import com.stack.data.local.db.StackDatabase
import com.stack.data.local.db.dao.LyricsDao
import com.stack.data.local.db.dao.PlayHistoryDao
import com.stack.data.local.db.dao.PlaylistDao
import com.stack.data.local.db.dao.SourceFolderDao
import com.stack.data.local.db.dao.TagDao
import com.stack.data.local.db.dao.TrackDao
import com.stack.data.local.db.dao.TrackFtsDao
import com.stack.data.local.prefs.PreferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideStackDatabase(
        @ApplicationContext context: Context
    ): StackDatabase {
        return Room.databaseBuilder(
            context,
            StackDatabase::class.java,
            "stack_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideTrackDao(database: StackDatabase): TrackDao {
        return database.trackDao()
    }

    @Provides
    @Singleton
    fun provideTrackFtsDao(database: StackDatabase): TrackFtsDao {
        return database.trackFtsDao()
    }

    @Provides
    @Singleton
    fun provideTagDao(database: StackDatabase): TagDao {
        return database.tagDao()
    }

    @Provides
    @Singleton
    fun providePlaylistDao(database: StackDatabase): PlaylistDao {
        return database.playlistDao()
    }

    @Provides
    @Singleton
    fun provideLyricsDao(database: StackDatabase): LyricsDao {
        return database.lyricsDao()
    }

    @Provides
    @Singleton
    fun providePlayHistoryDao(database: StackDatabase): PlayHistoryDao {
        return database.playHistoryDao()
    }

    @Provides
    @Singleton
    fun provideSourceFolderDao(database: StackDatabase): SourceFolderDao {
        return database.sourceFolderDao()
    }

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context
    ): PreferencesDataStore {
        return PreferencesDataStore(context)
    }
}
