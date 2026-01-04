package com.stack.domain.repository

import com.stack.domain.model.TrackSortOrder
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for app settings (DataStore).
 */
interface SettingsRepository {

    // Theme
    fun observeThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)

    // Dynamic Color
    fun observeDynamicColorEnabled(): Flow<Boolean>
    suspend fun setDynamicColorEnabled(enabled: Boolean)

    // Default Sort Order
    fun observeDefaultSortOrder(): Flow<TrackSortOrder>
    suspend fun setDefaultSortOrder(sortOrder: TrackSortOrder)

    // Playback Settings
    fun observeShuffleEnabled(): Flow<Boolean>
    suspend fun setShuffleEnabled(enabled: Boolean)

    fun observeRepeatMode(): Flow<RepeatMode>
    suspend fun setRepeatMode(mode: RepeatMode)

    // Lyrics indexing
    fun observeLyricsIndexingEnabled(): Flow<Boolean>
    suspend fun setLyricsIndexingEnabled(enabled: Boolean)

    // Gate completion
    fun observeGateCompleted(): Flow<Boolean>
    suspend fun setGateCompleted(completed: Boolean)

    // Tutorial shown
    fun observeTutorialShown(): Flow<Boolean>
    suspend fun setTutorialShown(shown: Boolean)

    // Last scan timestamp
    fun observeLastScanTime(): Flow<Long?>
    suspend fun setLastScanTime(timestamp: Long)
}

enum class ThemeMode {
    SYSTEM,  // Follow system (default, SSOT 9.4)
    LIGHT,
    DARK
}

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}
