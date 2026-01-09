package com.stack.feature.settings

import com.stack.domain.repository.ThemeMode

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isScanning: Boolean = false,
    val appVersion: String = "",
    val lastScanTime: String? = null,
    val trackCount: Int = 0
)
