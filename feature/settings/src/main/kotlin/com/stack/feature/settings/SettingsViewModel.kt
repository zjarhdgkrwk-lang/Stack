package com.stack.feature.settings

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stack.domain.repository.SettingsRepository
import com.stack.domain.repository.TrackRepository
import com.stack.domain.usecase.scan.ScanLibraryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val scanLibraryUseCase: ScanLibraryUseCase,
    private val trackRepository: TrackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        loadAppVersion()
        loadTrackCount()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.observeThemeMode().collect { themeMode ->
                _uiState.update { it.copy(themeMode = themeMode) }
            }
        }
        viewModelScope.launch {
            settingsRepository.observeLastScanTime().collect { timestamp ->
                val formatted = timestamp?.let {
                    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(it))
                }
                _uiState.update { it.copy(lastScanTime = formatted) }
            }
        }
    }

    private fun loadAppVersion() {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            _uiState.update { it.copy(appVersion = packageInfo.versionName ?: "Unknown") }
        } catch (e: PackageManager.NameNotFoundException) {
            _uiState.update { it.copy(appVersion = "Unknown") }
        }
    }

    private fun loadTrackCount() {
        viewModelScope.launch {
            val count = trackRepository.getTrackCount()
            _uiState.update { it.copy(trackCount = count) }
        }
    }

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.SetTheme -> {
                viewModelScope.launch { settingsRepository.setThemeMode(intent.themeMode) }
            }
            SettingsIntent.RescanLibrary -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isScanning = true) }
                    try {
                        scanLibraryUseCase.invoke(fullScan = true)
                        loadTrackCount()
                    } finally {
                        _uiState.update { it.copy(isScanning = false) }
                    }
                }
            }
        }
    }
}
