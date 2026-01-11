package com.stack.domain.model

/**
 * Represents the current state of the scan pipeline.
 * Domain layer representation of scan state.
 */
sealed class ScanState {
    /** No scan in progress */
    data object Idle : ScanState()

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

/**
 * Represents what triggered a scan operation.
 */
sealed class ScanTrigger {
    /** A new source folder was added */
    data class SourceFolderAdded(val folderUri: String) : ScanTrigger()

    /** Manual rescan requested */
    data object ManualRescan : ScanTrigger()

    /** Content change detected */
    data object ContentChange : ScanTrigger()

    /** App startup */
    data object AppStartup : ScanTrigger()
}
