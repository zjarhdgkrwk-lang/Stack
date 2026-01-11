package com.stack.domain.usecase.scan

import com.stack.core.util.Result
import com.stack.domain.model.ScanState
import com.stack.domain.repository.ScanRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Main entry point for scan operations.
 * This UseCase orchestrates scan requests from the UI layer.
 */
class ScanLibraryUseCase @Inject constructor(
    private val scanRepository: ScanRepository
) {
    /**
     * Observable scan state for UI
     */
    val scanState: StateFlow<ScanState> = scanRepository.scanState

    /**
     * Perform a full library scan.
     * Use for:
     * - Initial scan after Gate is ready
     * - Manual "Rescan All" button
     */
    suspend fun performFullScan(): Result<ScanState.Completed> {
        return scanRepository.performFullScan()
    }

    /**
     * Perform an incremental scan (check for changes only).
     * Use for:
     * - App startup (subsequent launches)
     * - Pull-to-refresh in library
     */
    suspend fun performIncrementalScan(): Result<ScanState.Completed> {
        return scanRepository.performIncrementalScan()
    }

    /**
     * Cancel any ongoing scan.
     */
    fun cancelScan() {
        scanRepository.cancelScan()
    }

    /**
     * Check if scan is in progress.
     */
    fun isScanning(): Boolean = scanRepository.isScanning()
}
