package com.stack.data.scanner

import android.net.Uri
import com.stack.core.logging.Logger
import com.stack.core.util.DispatcherProvider
import com.stack.data.local.db.dao.TrackDao
import com.stack.data.local.db.dao.SourceFolderDao
import com.stack.data.local.db.entity.TrackEntity
import com.stack.domain.model.TrackStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central orchestrator for all scan operations.
 *
 * SSOT 13.2 COMPLIANCE:
 * - Uses Mutex to ensure only ONE scan runs at a time
 * - All scan requests go through this single pipeline
 *
 * SSOT 5.4 COMPLIANCE:
 * - Debounce: 3 seconds for auto-triggered scans
 * - Thread: Dispatchers.IO
 * - Cancelable: via coroutine Job
 */
@Singleton
class ScanManager @Inject constructor(
    private val mediaStoreScanner: MediaStoreScanner,
    private val trackDao: TrackDao,
    private val sourceFolderDao: SourceFolderDao,
    private val dispatchers: DispatcherProvider,
    private val logger: Logger
) {
    companion object {
        private const val TAG = "ScanManager"

        /**
         * SSOT 5.4: Scan debounce duration
         * DO NOT CHANGE without SSOT update
         */
        const val DEBOUNCE_DURATION_MS = 3000L
    }

    // ============================================================
    // SSOT 13.2: Single Pipeline Enforcement
    // ============================================================

    /**
     * Mutex ensures only one scan operation runs at a time.
     * Any concurrent scan request will wait for the lock.
     */
    private val scanMutex = Mutex()

    /**
     * Tracks the current scan job for cancellation support.
     */
    private var currentScanJob: Job? = null

    // ============================================================
    // State Management
    // ============================================================

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)

    /**
     * Observable scan state for UI consumption.
     * Use sample() or throttleLatest() per SSOT 5.4 when collecting in UI.
     */
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    /**
     * Debounced scan trigger channel.
     * MediaStore changes flow through here.
     */
    private val scanTriggerFlow = MutableSharedFlow<ScanTrigger>(
        replay = 0,
        extraBufferCapacity = 1
    )

    private val scope = CoroutineScope(SupervisorJob() + dispatchers.io)

    init {
        // Set up debounced scan listener
        setupDebouncedScanListener()
    }

    /**
     * SSOT 5.4: Debounce auto-triggered scans by 3 seconds
     */
    private fun setupDebouncedScanListener() {
        scope.launch {
            scanTriggerFlow
                .debounce(DEBOUNCE_DURATION_MS)
                .collect { trigger ->
                    logger.d(TAG, "Debounced scan triggered: $trigger")
                    when (trigger) {
                        is ScanTrigger.MediaStoreChanged -> performIncrementalScan()
                        is ScanTrigger.SourceFolderAdded -> performFolderScan(trigger.folderUri)
                    }
                }
        }
    }

    // ============================================================
    // Public API
    // ============================================================

    /**
     * Triggers a full library scan.
     * Call this on:
     * - First app launch after Gate is ready
     * - User manual "Rescan" action
     *
     * @return Result with scan summary
     */
    suspend fun performFullScan(): com.stack.core.util.Result<ScanState.Completed> {
        return executeScan(isIncremental = false)
    }

    /**
     * Triggers an incremental scan (only checks for changes).
     * Call this on:
     * - App startup (after first launch)
     * - Pull-to-refresh
     */
    suspend fun performIncrementalScan(): com.stack.core.util.Result<ScanState.Completed> {
        return executeScan(isIncremental = true)
    }

    /**
     * Scans a specific SAF folder when newly added.
     * SSOT 5.1: "소스폴더 추가 → 해당 폴더 풀스캔"
     */
    suspend fun performFolderScan(folderUri: Uri): com.stack.core.util.Result<ScanState.Completed> {
        // TODO: Implement SAF-specific scanning in Phase 3.5 or 4
        // For MVP, this triggers a full scan
        logger.d(TAG, "Folder scan requested for: $folderUri (delegating to full scan)")
        return performFullScan()
    }

    /**
     * Queues a debounced scan. Use this for ContentObserver callbacks.
     */
    fun queueDebouncedScan(trigger: ScanTrigger) {
        scope.launch {
            scanTriggerFlow.emit(trigger)
        }
    }

    /**
     * Cancels any ongoing scan.
     */
    fun cancelScan() {
        currentScanJob?.cancel()
        currentScanJob = null
        _scanState.value = ScanState.Idle
        logger.d(TAG, "Scan cancelled by user")
    }

    /**
     * Returns true if a scan is currently in progress.
     */
    fun isScanning(): Boolean = _scanState.value is ScanState.Scanning

    // ============================================================
    // Internal Implementation
    // ============================================================

    /**
     * Core scan execution with Mutex protection.
     * SSOT 13.2: "스캔 → 단일 파이프라인 (Mutex)"
     */
    private suspend fun executeScan(
        isIncremental: Boolean
    ): com.stack.core.util.Result<ScanState.Completed> = withContext(dispatchers.io) {

        // Try to acquire the mutex
        // If another scan is running, this will suspend until it completes
        scanMutex.withLock {
            val startTime = System.currentTimeMillis()

            try {
                _scanState.value = ScanState.Scanning()
                logger.d(TAG, "Starting ${if (isIncremental) "incremental" else "full"} scan")

                // Track this job for cancellation
                currentScanJob = coroutineContext[Job]

                // Step 1: Scan MediaStore
                val scannedTracks = mediaStoreScanner.scanMediaStore { progress ->
                    _scanState.value = ScanState.Scanning(
                        progress = progress * 0.5f, // First 50% is scanning
                        currentFolder = "MediaStore",
                        scannedCount = (progress * 100).toInt(),
                        totalCount = 100
                    )
                }

                // Step 2: Sync with database
                val (added, updated, removed) = syncWithDatabase(
                    scannedTracks = scannedTracks,
                    isIncremental = isIncremental
                ) { progress ->
                    _scanState.value = ScanState.Scanning(
                        progress = 0.5f + (progress * 0.5f), // Last 50% is syncing
                        currentFolder = "Database Sync",
                        scannedCount = scannedTracks.size,
                        totalCount = scannedTracks.size
                    )
                }

                val duration = System.currentTimeMillis() - startTime
                val completedState = ScanState.Completed(
                    addedCount = added,
                    updatedCount = updated,
                    removedCount = removed,
                    durationMs = duration
                )

                _scanState.value = completedState
                logger.d(TAG, "Scan completed: +$added, ~$updated, -$removed in ${duration}ms")

                // Reset to idle after showing completion briefly
                delay(500)
                _scanState.value = ScanState.Idle

                com.stack.core.util.Result.Success(completedState)

            } catch (e: CancellationException) {
                _scanState.value = ScanState.Idle
                logger.d(TAG, "Scan was cancelled")
                throw e
            } catch (e: Exception) {
                val errorState = ScanState.Error(
                    message = e.message ?: "Unknown error during scan",
                    exception = e
                )
                _scanState.value = errorState
                logger.e(TAG, "Scan failed", e)
                com.stack.core.util.Result.Error(e)
            } finally {
                currentScanJob = null
            }
        }
    }

    /**
     * Synchronizes scanned tracks with the database.
     *
     * @return Triple of (added, updated, removed) counts
     */
    private suspend fun syncWithDatabase(
        scannedTracks: List<TrackEntity>,
        isIncremental: Boolean,
        onProgress: (Float) -> Unit
    ): Triple<Int, Int, Int> {
        var added = 0
        var updated = 0
        var removed = 0

        val scannedUris = scannedTracks.map { it.contentUri }.toSet()
        val existingTracks = trackDao.getAllTracksOnce()
        val existingUriMap = existingTracks.associateBy { it.contentUri }

        // Process scanned tracks
        val total = scannedTracks.size
        scannedTracks.forEachIndexed { index, scannedTrack ->
            val existing = existingUriMap[scannedTrack.contentUri]

            if (existing == null) {
                // New track
                trackDao.insert(scannedTrack)
                added++
            } else {
                // Check if track was modified (SSOT 5.2)
                if (isTrackModified(existing, scannedTrack)) {
                    trackDao.update(scannedTrack.copy(
                        id = existing.id,
                        dateAdded = existing.dateAdded // Preserve original add date
                    ))
                    updated++
                }
            }

            if (index % 100 == 0) {
                onProgress(index.toFloat() / total)
            }
        }

        // Detect removed/ghost tracks (SSOT 5.3)
        existingTracks.forEach { existingTrack ->
            if (existingTrack.contentUri !in scannedUris &&
                existingTrack.status == TrackStatus.ACTIVE) {
                // Mark as GHOST instead of deleting
                trackDao.updateStatus(existingTrack.id, TrackStatus.GHOST)
                removed++
                logger.d(TAG, "Marked track as GHOST: ${existingTrack.title}")
            }
        }

        onProgress(1f)
        return Triple(added, updated, removed)
    }

    /**
     * Checks if track metadata has changed.
     * SSOT 5.2: Uses contentUri + size + dateModified for identification
     */
    private fun isTrackModified(existing: TrackEntity, scanned: TrackEntity): Boolean {
        return existing.size != scanned.size ||
               existing.dateModified != scanned.dateModified
    }
}

/**
 * Types of scan triggers for debounce handling
 */
sealed class ScanTrigger {
    object MediaStoreChanged : ScanTrigger()
    data class SourceFolderAdded(val folderUri: Uri) : ScanTrigger()
}
