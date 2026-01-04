package com.stack.feature.gate

import com.stack.domain.model.SourceFolder

/**
 * Gate screen MVI contract.
 * Adapted from SSOT Section 7.1 to work with existing Phase 3 infrastructure.
 */
object GateContract {

    /**
     * Represents the current step in the Gate flow.
     */
    enum class GateStep {
        PERMISSION,
        FOLDER_SELECTION,
        SCANNING,
        READY
    }

    /**
     * Permission status following Android runtime permission states.
     */
    enum class PermissionStatus {
        NOT_REQUESTED,
        GRANTED,
        DENIED,
        PERMANENTLY_DENIED
    }

    /**
     * Immutable state for Gate screen.
     */
    data class State(
        val currentStep: GateStep = GateStep.PERMISSION,
        val permissionStatus: PermissionStatus = PermissionStatus.NOT_REQUESTED,
        val selectedFolders: List<SourceFolder> = emptyList(),
        val scanProgress: Float = 0f,
        val scanningFile: String = "",
        val scannedCount: Int = 0,
        val totalCount: Int = 0,
        val error: GateError? = null,
        val isLoading: Boolean = false
    ) {
        val canProceedToFolderSelection: Boolean
            get() = permissionStatus == PermissionStatus.GRANTED

        val canStartScan: Boolean
            get() = canProceedToFolderSelection && selectedFolders.isNotEmpty()

        val isGateReady: Boolean
            get() = currentStep == GateStep.READY
    }

    /**
     * Gate-specific errors.
     */
    sealed class GateError {
        data object PermissionRequired : GateError()
        data object NoFoldersSelected : GateError()
        data class ScanFailed(val message: String) : GateError()
        data object Unknown : GateError()
    }

    /**
     * User intents (actions).
     */
    sealed class Intent {
        data object CheckInitialState : Intent()
        data object RequestPermission : Intent()
        data object OpenSafPicker : Intent()
        data class RemoveFolder(val folder: SourceFolder) : Intent()
        data object StartInitialScan : Intent()
        data object RetryPermission : Intent()
        data object DismissError : Intent()
        data class OnPermissionResult(val granted: Boolean, val isPermanentlyDenied: Boolean) : Intent()
        data class OnFolderSelected(val uri: String, val displayName: String) : Intent()
    }

    /**
     * One-time side effects.
     */
    sealed class Effect {
        data object NavigateToLibrary : Effect()
        data object LaunchPermissionRequest : Effect()
        data object LaunchSafPicker : Effect()
        data object OpenSystemSettings : Effect()
        data class ShowError(val error: GateError) : Effect()
    }
}
