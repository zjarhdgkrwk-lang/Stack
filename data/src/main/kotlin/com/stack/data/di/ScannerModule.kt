package com.stack.data.di

import android.content.ContentResolver
import android.content.Context
import com.stack.core.logging.Logger
import com.stack.core.util.DispatcherProvider
import com.stack.data.local.db.dao.SourceFolderDao
import com.stack.data.local.db.dao.TrackDao
import com.stack.data.scanner.ContentObserverManager
import com.stack.data.scanner.MediaStoreScanner
import com.stack.data.scanner.ScanManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ScannerModule {

    @Provides
    @Singleton
    fun provideContentResolver(
        @ApplicationContext context: Context
    ): ContentResolver = context.contentResolver

    @Provides
    @Singleton
    fun provideMediaStoreScanner(
        contentResolver: ContentResolver,
        dispatchers: DispatcherProvider,
        logger: Logger
    ): MediaStoreScanner = MediaStoreScanner(
        contentResolver = contentResolver,
        dispatchers = dispatchers,
        logger = logger
    )

    @Provides
    @Singleton
    fun provideScanManager(
        mediaStoreScanner: MediaStoreScanner,
        trackDao: TrackDao,
        sourceFolderDao: SourceFolderDao,
        dispatchers: DispatcherProvider,
        logger: Logger
    ): ScanManager = ScanManager(
        mediaStoreScanner = mediaStoreScanner,
        trackDao = trackDao,
        sourceFolderDao = sourceFolderDao,
        dispatchers = dispatchers,
        logger = logger
    )

    @Provides
    @Singleton
    fun provideContentObserverManager(
        contentResolver: ContentResolver,
        scanManager: ScanManager,
        logger: Logger
    ): ContentObserverManager = ContentObserverManager(
        contentResolver = contentResolver,
        scanManager = scanManager,
        logger = logger
    )
}
