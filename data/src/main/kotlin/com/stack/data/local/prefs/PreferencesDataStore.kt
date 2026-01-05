package com.stack.data.local.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "stack_settings")

/**
 * DataStore wrapper for app preferences.
 */
@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val dataStore = context.dataStore

    // ===== Generic accessors =====

    fun <T> observe(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
        return dataStore.data.map { preferences ->
            preferences[key] ?: defaultValue
        }
    }

    fun <T> observeNullable(key: Preferences.Key<T>): Flow<T?> {
        return dataStore.data.map { preferences ->
            preferences[key]
        }
    }

    suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    suspend fun <T> remove(key: Preferences.Key<T>) {
        dataStore.edit { preferences ->
            preferences.remove(key)
        }
    }

    // ===== Theme =====

    fun observeThemeMode(): Flow<String> = observe(PreferencesKeys.THEME_MODE, "SYSTEM")
    suspend fun setThemeMode(mode: String) = set(PreferencesKeys.THEME_MODE, mode)

    fun observeDynamicColorEnabled(): Flow<Boolean> = observe(PreferencesKeys.DYNAMIC_COLOR_ENABLED, false)
    suspend fun setDynamicColorEnabled(enabled: Boolean) = set(PreferencesKeys.DYNAMIC_COLOR_ENABLED, enabled)

    // ===== Library =====

    fun observeDefaultSortOrder(): Flow<String> = observe(PreferencesKeys.DEFAULT_SORT_ORDER, "DATE_ADDED_DESC")
    suspend fun setDefaultSortOrder(sortOrder: String) = set(PreferencesKeys.DEFAULT_SORT_ORDER, sortOrder)

    // ===== Playback =====

    fun observeShuffleEnabled(): Flow<Boolean> = observe(PreferencesKeys.SHUFFLE_ENABLED, false)
    suspend fun setShuffleEnabled(enabled: Boolean) = set(PreferencesKeys.SHUFFLE_ENABLED, enabled)

    fun observeRepeatMode(): Flow<String> = observe(PreferencesKeys.REPEAT_MODE, "OFF")
    suspend fun setRepeatMode(mode: String) = set(PreferencesKeys.REPEAT_MODE, mode)

    // ===== Lyrics =====

    fun observeLyricsIndexingEnabled(): Flow<Boolean> = observe(PreferencesKeys.LYRICS_INDEXING_ENABLED, true)
    suspend fun setLyricsIndexingEnabled(enabled: Boolean) = set(PreferencesKeys.LYRICS_INDEXING_ENABLED, enabled)

    // ===== Gate =====

    fun observeGateCompleted(): Flow<Boolean> = observe(PreferencesKeys.GATE_COMPLETED, false)
    suspend fun setGateCompleted(completed: Boolean) = set(PreferencesKeys.GATE_COMPLETED, completed)

    fun observeTutorialShown(): Flow<Boolean> = observe(PreferencesKeys.TUTORIAL_SHOWN, false)
    suspend fun setTutorialShown(shown: Boolean) = set(PreferencesKeys.TUTORIAL_SHOWN, shown)

    // ===== Scan =====

    fun observeLastScanTime(): Flow<Long?> = observeNullable(PreferencesKeys.LAST_SCAN_TIME)
    suspend fun setLastScanTime(timestamp: Long) = set(PreferencesKeys.LAST_SCAN_TIME, timestamp)
}
