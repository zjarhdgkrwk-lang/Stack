package com.stack.data.repository

import android.net.Uri
import com.stack.core.util.Result
import com.stack.data.scanner.ScanManager
import com.stack.domain.model.ScanState
import com.stack.domain.model.ScanTrigger
import com.stack.domain.repository.ScanRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import javax.inject.Inject
import javax.inject.Singleton
import com.stack.data.scanner.ScanState as DataScanState
import com.stack.data.scanner.ScanTrigger as DataScanTrigger

/**
 * Implementation of ScanRepository that wraps ScanManager.
 * Converts between domain and data layer types.
 */
@Singleton
class ScanRepositoryImpl @Inject constructor(
    private val scanManager: ScanManager
) : ScanRepository {

    private val scope = CoroutineScope(Dispatchers.Default)

    override val scanState: StateFlow<ScanState> = scanManager.scanState
        .map { dataScanState -> dataScanState.toDomain() }
        .stateIn(scope, SharingStarted.Eagerly, ScanState.Idle)

    override suspend fun performFullScan(): Result<ScanState.Completed> {
        return when (val result = scanManager.performFullScan()) {
            is Result.Success -> Result.Success(result.data.toDomain())
            is Result.Error -> Result.Error(result.exception)
        }
    }

    override suspend fun performIncrementalScan(): Result<ScanState.Completed> {
        return when (val result = scanManager.performIncrementalScan()) {
            is Result.Success -> Result.Success(result.data.toDomain())
            is Result.Error -> Result.Error(result.exception)
        }
    }

    override fun cancelScan() {
        scanManager.cancelScan()
    }

    override fun isScanning(): Boolean {
        return scanManager.isScanning()
    }

    override fun queueDebouncedScan(trigger: ScanTrigger) {
        scanManager.queueDebouncedScan(trigger.toData())
    }

    // ============================================================
    // Type Conversions
    // ============================================================

    private fun DataScanState.toDomain(): ScanState {
        return when (this) {
            is DataScanState.Idle -> ScanState.Idle
            is DataScanState.Scanning -> ScanState.Scanning(
                progress = this.progress,
                currentFolder = this.currentFolder,
                scannedCount = this.scannedCount,
                totalCount = this.totalCount
            )
            is DataScanState.Completed -> ScanState.Completed(
                addedCount = this.addedCount,
                updatedCount = this.updatedCount,
                removedCount = this.removedCount,
                durationMs = this.durationMs
            )
            is DataScanState.Error -> ScanState.Error(
                message = this.message,
                exception = this.exception
            )
        }
    }

    private fun DataScanState.Completed.toDomain(): ScanState.Completed {
        return ScanState.Completed(
            addedCount = this.addedCount,
            updatedCount = this.updatedCount,
            removedCount = this.removedCount,
            durationMs = this.durationMs
        )
    }

    private fun ScanTrigger.toData(): DataScanTrigger {
        return when (this) {
            is ScanTrigger.SourceFolderAdded -> DataScanTrigger.SourceFolderAdded(Uri.parse(this.folderUri))
            is ScanTrigger.ManualRescan -> DataScanTrigger.MediaStoreChanged
            is ScanTrigger.ContentChange -> DataScanTrigger.MediaStoreChanged
            is ScanTrigger.AppStartup -> DataScanTrigger.MediaStoreChanged
        }
    }
}
