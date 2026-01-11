package com.stack.domain.repository

import com.stack.core.util.Result
import com.stack.domain.model.ScanState
import com.stack.domain.model.ScanTrigger
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface for scan operations.
 * Abstracts the scan manager implementation for the domain layer.
 */
interface ScanRepository {
    /**
     * Observable scan state for UI
     */
    val scanState: StateFlow<ScanState>

    /**
     * Perform a full library scan.
     */
    suspend fun performFullScan(): Result<ScanState.Completed>

    /**
     * Perform an incremental scan (check for changes only).
     */
    suspend fun performIncrementalScan(): Result<ScanState.Completed>

    /**
     * Cancel any ongoing scan.
     */
    fun cancelScan()

    /**
     * Check if scan is in progress.
     */
    fun isScanning(): Boolean

    /**
     * Queue a debounced scan with specified trigger.
     */
    fun queueDebouncedScan(trigger: ScanTrigger)
}
