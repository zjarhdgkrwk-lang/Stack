package com.stack.data.local.prefs

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * DataStore preference keys for app settings.
 */
object PreferencesKeys {

    // Theme
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val DYNAMIC_COLOR_ENABLED = booleanPreferencesKey("dynamic_color_enabled")

    // Library
    val DEFAULT_SORT_ORDER = stringPreferencesKey("default_sort_order")

    // Playback
    val SHUFFLE_ENABLED = booleanPreferencesKey("shuffle_enabled")
    val REPEAT_MODE = stringPreferencesKey("repeat_mode")

    // Lyrics
    val LYRICS_INDEXING_ENABLED = booleanPreferencesKey("lyrics_indexing_enabled")

    // Gate / Onboarding
    val GATE_COMPLETED = booleanPreferencesKey("gate_completed")
    val TUTORIAL_SHOWN = booleanPreferencesKey("tutorial_shown")

    // Scan
    val LAST_SCAN_TIME = longPreferencesKey("last_scan_time")

    // Playback session (for restoration)
    val LAST_TRACK_ID = longPreferencesKey("last_track_id")
    val LAST_POSITION = longPreferencesKey("last_position")
    val LAST_QUEUE_JSON = stringPreferencesKey("last_queue_json")
}
