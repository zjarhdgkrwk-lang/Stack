package com.stack.data.scanner

/**
 * Represents the current state of the scan pipeline.
 * Exposed via StateFlow from ScanManager.
 */
sealed class ScanState {
    /** No scan in progress */
    object Idle : ScanState()

    /** Scan is currently running */
    data class Scanning(
        val progress: Float = 0f,        // 0.0 to 1.0
        val currentFolder: String = "",
        val scannedCount: Int = 0,
        val totalCount: Int = 0
    ) : ScanState()

    /** Scan completed successfully */
    data class Completed(
        val addedCount: Int,
        val updatedCount: Int,
        val removedCount: Int,
        val durationMs: Long
    ) : ScanState()

    /** Scan failed with error */
    data class Error(
        val message: String,
        val exception: Throwable? = null
    ) : ScanState()
}
