package com.stack.feature.settings

import com.stack.domain.repository.ThemeMode

sealed interface SettingsIntent {
    data class SetTheme(val themeMode: ThemeMode) : SettingsIntent
    data object RescanLibrary : SettingsIntent
}
