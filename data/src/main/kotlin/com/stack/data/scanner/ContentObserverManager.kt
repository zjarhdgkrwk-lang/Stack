package com.stack.data.scanner

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.stack.core.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Monitors MediaStore for changes and triggers rescans.
 *
 * SSOT 5.1: "MediaStore 변경 감지 → 디바운스 후 증분 스캔 큐 적재"
 * Note: Debounce is handled by ScanManager (3 seconds per SSOT 5.4)
 */
@Singleton
class ContentObserverManager @Inject constructor(
    private val contentResolver: ContentResolver,
    private val scanManager: ScanManager,
    private val logger: Logger
) {
    companion object {
        private const val TAG = "ContentObserverManager"

        private val AUDIO_COLLECTION_URI: Uri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
    }

    private var isRegistered = false

    private val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            onChange(selfChange, null)
        }

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            logger.d(TAG, "MediaStore change detected: $uri")
            // Delegate to ScanManager which handles debounce
            scanManager.queueDebouncedScan(ScanTrigger.MediaStoreChanged)
        }
    }

    /**
     * Start observing MediaStore changes.
     * Call this after Gate is ready.
     */
    fun startObserving() {
        if (isRegistered) {
            logger.d(TAG, "Already observing MediaStore")
            return
        }

        try {
            contentResolver.registerContentObserver(
                AUDIO_COLLECTION_URI,
                true, // notifyForDescendants
                contentObserver
            )
            isRegistered = true
            logger.d(TAG, "Started observing MediaStore changes")
        } catch (e: Exception) {
            logger.e(TAG, "Failed to register ContentObserver", e)
        }
    }

    /**
     * Stop observing MediaStore changes.
     * Call this when app is terminated or Gate becomes invalid.
     */
    fun stopObserving() {
        if (!isRegistered) {
            return
        }

        try {
            contentResolver.unregisterContentObserver(contentObserver)
            isRegistered = false
            logger.d(TAG, "Stopped observing MediaStore changes")
        } catch (e: Exception) {
            logger.e(TAG, "Failed to unregister ContentObserver", e)
        }
    }
}
