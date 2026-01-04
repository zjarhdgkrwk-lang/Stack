package com.stack.feature.gate

import androidx.lifecycle.viewModelScope
import com.stack.core.base.BaseViewModel
import com.stack.core.util.Result
import com.stack.data.scanner.ScanState
import com.stack.domain.usecase.gate.GetGateReadyStatusUseCase
import com.stack.domain.usecase.gate.SetGateReadyStatusUseCase
import com.stack.domain.usecase.scan.ManageSourceFoldersUseCase
import com.stack.domain.usecase.scan.ScanLibraryUseCase
import com.stack.feature.gate.GateContract.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Gate screen - manages onboarding flow.
 */
@HiltViewModel
class GateViewModel @Inject constructor(
    private val manageSourceFoldersUseCase: ManageSourceFoldersUseCase,
    private val scanLibraryUseCase: ScanLibraryUseCase,
    private val getGateReadyStatusUseCase: GetGateReadyStatusUseCase,
    private val setGateReadyStatusUseCase: SetGateReadyStatusUseCase
) : BaseViewModel<State, Intent, Effect>(State()) {

    init {
        handleIntent(Intent.CheckInitialState)
        observeSourceFolders()
        observeScanProgress()
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.CheckInitialState -> checkInitialState()
            is Intent.RequestPermission -> sendEffect(Effect.LaunchPermissionRequest)
            is Intent.OpenSafPicker -> openSafPicker()
            is Intent.RemoveFolder -> removeFolder(intent.folder)
            is Intent.StartInitialScan -> startInitialScan()
            is Intent.RetryPermission -> retryPermission()
            is Intent.DismissError -> updateState { copy(error = null) }
            is Intent.OnPermissionResult -> handlePermissionResult(intent.granted, intent.isPermanentlyDenied)
            is Intent.OnFolderSelected -> handleFolderSelected(intent.uri, intent.displayName)
        }
    }

    private fun checkInitialState() {
        viewModelScope.launch {
            val isGateReady = getGateReadyStatusUseCase().first()
            if (isGateReady) {
                updateState { copy(currentStep = GateStep.READY) }
                sendEffect(Effect.NavigateToLibrary)
                return@launch
            }

            val existingFolders = manageSourceFoldersUseCase.getSourceFolders().first()
            if (existingFolders.isNotEmpty()) {
                updateState {
                    copy(
                        selectedFolders = existingFolders,
                        currentStep = GateStep.FOLDER_SELECTION,
                        permissionStatus = PermissionStatus.GRANTED
                    )
                }
            }
        }
    }

    private fun observeSourceFolders() {
        viewModelScope.launch {
            manageSourceFoldersUseCase.getSourceFolders().collect { folders ->
                updateState { copy(selectedFolders = folders) }
            }
        }
    }

    private fun observeScanProgress() {
        viewModelScope.launch {
            scanLibraryUseCase.scanState.collect { scanState ->
                when (scanState) {
                    is ScanState.Scanning -> {
                        updateState {
                            copy(
                                currentStep = GateStep.SCANNING,
                                scanProgress = scanState.progress,
                                scanningFile = scanState.currentFolder,
                                scannedCount = scanState.scannedCount,
                                totalCount = scanState.totalCount
                            )
                        }
                    }
                    is ScanState.Completed -> completeGate()
                    is ScanState.Error -> {
                        updateState {
                            copy(
                                currentStep = GateStep.FOLDER_SELECTION,
                                error = GateError.ScanFailed(scanState.message)
                            )
                        }
                    }
                    is ScanState.Idle -> { /* No action needed */ }
                }
            }
        }
    }

    private fun handlePermissionResult(granted: Boolean, isPermanentlyDenied: Boolean) {
        val newStatus = when {
            granted -> PermissionStatus.GRANTED
            isPermanentlyDenied -> PermissionStatus.PERMANENTLY_DENIED
            else -> PermissionStatus.DENIED
        }

        updateState {
            copy(
                permissionStatus = newStatus,
                currentStep = if (granted) GateStep.FOLDER_SELECTION else GateStep.PERMISSION
            )
        }

        if (!granted) {
            sendEffect(Effect.ShowError(GateError.PermissionRequired))
        }
    }

    private fun retryPermission() {
        if (state.value.permissionStatus == PermissionStatus.PERMANENTLY_DENIED) {
            sendEffect(Effect.OpenSystemSettings)
        } else {
            sendEffect(Effect.LaunchPermissionRequest)
        }
    }

    private fun openSafPicker() {
        if (state.value.permissionStatus != PermissionStatus.GRANTED) {
            sendEffect(Effect.ShowError(GateError.PermissionRequired))
            return
        }
        sendEffect(Effect.LaunchSafPicker)
    }

    private fun handleFolderSelected(uri: String, displayName: String) {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }

            when (val result = manageSourceFoldersUseCase.addSourceFolder(android.net.Uri.parse(uri))) {
                is Result.Success -> {
                    updateState { copy(isLoading = false) }
                }
                is Result.Error -> {
                    updateState {
                        copy(
                            isLoading = false,
                            error = GateError.Unknown
                        )
                    }
                }
            }
        }
    }

    private fun removeFolder(folder: com.stack.domain.model.SourceFolder) {
        viewModelScope.launch {
            manageSourceFoldersUseCase.removeSourceFolder(folder.id)
        }
    }

    private fun startInitialScan() {
        if (!state.value.canStartScan) {
            if (state.value.selectedFolders.isEmpty()) {
                sendEffect(Effect.ShowError(GateError.NoFoldersSelected))
            }
            return
        }

        viewModelScope.launch {
            updateState { copy(currentStep = GateStep.SCANNING) }
            scanLibraryUseCase.performFullScan()
        }
    }

    private fun completeGate() {
        viewModelScope.launch {
            setGateReadyStatusUseCase(true)
            updateState { copy(currentStep = GateStep.READY) }
            sendEffect(Effect.NavigateToLibrary)
        }
    }
}
