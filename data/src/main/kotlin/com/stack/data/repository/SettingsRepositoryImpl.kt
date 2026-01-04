package com.stack.data.repository

import com.stack.data.local.prefs.PreferencesDataStore
import com.stack.domain.model.TrackSortOrder
import com.stack.domain.repository.RepeatMode
import com.stack.domain.repository.SettingsRepository
import com.stack.domain.repository.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore
) : SettingsRepository {

    // Theme

    override fun observeThemeMode(): Flow<ThemeMode> {
        return preferencesDataStore.observeThemeMode().map { mode ->
            try {
                ThemeMode.valueOf(mode)
            } catch (e: IllegalArgumentException) {
                ThemeMode.SYSTEM
            }
        }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        preferencesDataStore.setThemeMode(mode.name)
    }

    // Dynamic Color

    override fun observeDynamicColorEnabled(): Flow<Boolean> {
        return preferencesDataStore.observeDynamicColorEnabled()
    }

    override suspend fun setDynamicColorEnabled(enabled: Boolean) {
        preferencesDataStore.setDynamicColorEnabled(enabled)
    }

    // Default Sort Order

    override fun observeDefaultSortOrder(): Flow<TrackSortOrder> {
        return preferencesDataStore.observeDefaultSortOrder().map { order ->
            try {
                TrackSortOrder.valueOf(order)
            } catch (e: IllegalArgumentException) {
                TrackSortOrder.DATE_ADDED_DESC
            }
        }
    }

    override suspend fun setDefaultSortOrder(sortOrder: TrackSortOrder) {
        preferencesDataStore.setDefaultSortOrder(sortOrder.name)
    }

    // Playback Settings

    override fun observeShuffleEnabled(): Flow<Boolean> {
        return preferencesDataStore.observeShuffleEnabled()
    }

    override suspend fun setShuffleEnabled(enabled: Boolean) {
        preferencesDataStore.setShuffleEnabled(enabled)
    }

    override fun observeRepeatMode(): Flow<RepeatMode> {
        return preferencesDataStore.observeRepeatMode().map { mode ->
            try {
                RepeatMode.valueOf(mode)
            } catch (e: IllegalArgumentException) {
                RepeatMode.OFF
            }
        }
    }

    override suspend fun setRepeatMode(mode: RepeatMode) {
        preferencesDataStore.setRepeatMode(mode.name)
    }

    // Lyrics indexing

    override fun observeLyricsIndexingEnabled(): Flow<Boolean> {
        return preferencesDataStore.observeLyricsIndexingEnabled()
    }

    override suspend fun setLyricsIndexingEnabled(enabled: Boolean) {
        preferencesDataStore.setLyricsIndexingEnabled(enabled)
    }

    // Gate completion

    override fun observeGateCompleted(): Flow<Boolean> {
        return preferencesDataStore.observeGateCompleted()
    }

    override suspend fun setGateCompleted(completed: Boolean) {
        preferencesDataStore.setGateCompleted(completed)
    }

    // Tutorial shown

    override fun observeTutorialShown(): Flow<Boolean> {
        return preferencesDataStore.observeTutorialShown()
    }

    override suspend fun setTutorialShown(shown: Boolean) {
        preferencesDataStore.setTutorialShown(shown)
    }

    // Last scan timestamp

    override fun observeLastScanTime(): Flow<Long?> {
        return preferencesDataStore.observeLastScanTime()
    }

    override suspend fun setLastScanTime(timestamp: Long) {
        preferencesDataStore.setLastScanTime(timestamp)
    }
}
