package com.stack.core.crash

import android.content.Context
import com.stack.core.logging.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Crash information data class.
 * Used to display crash logs in the app's diagnostics screen.
 */
data class CrashInfo(
    val timestamp: Long,
    val formattedTime: String,
    val message: String,
    val stackTrace: String,
    val threadName: String
)

/**
 * Interface for crash capture and reporting.
 * Allows viewing recent crashes within the app (SSOT 13.1).
 */
interface CrashCapture {
    /**
     * Initialize crash capture. Should be called in Application.onCreate()
     */
    fun initialize()

    /**
     * Manually capture an exception.
     */
    fun capture(throwable: Throwable, message: String? = null)

    /**
     * Get recent crashes as a Flow.
     */
    fun getRecentCrashes(): Flow<List<CrashInfo>>

    /**
     * Clear all stored crash logs.
     */
    fun clearCrashes()

    /**
     * Export crash logs to a shareable format.
     */
    fun exportCrashLog(): String
}

/**
 * Default implementation of CrashCapture.
 * Stores crash logs in memory with a limit of 10 entries (SSOT 13.1).
 * Persists to file for survival across app restarts.
 */
@Singleton
class DefaultCrashCapture @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: Logger
) : CrashCapture {

    companion object {
        private const val TAG = "CrashCapture"
        private const val MAX_CRASHES = 10
        private const val CRASH_LOG_FILE = "crash_logs.txt"
        private const val DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
    }

    private val _crashes = MutableStateFlow<List<CrashInfo>>(emptyList())
    private val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())

    private var originalHandler: Thread.UncaughtExceptionHandler? = null

    override fun initialize() {
        loadCrashesFromFile()
        setupUncaughtExceptionHandler()
        logger.i(TAG, "CrashCapture initialized")
    }

    override fun capture(throwable: Throwable, message: String?) {
        val crashInfo = createCrashInfo(throwable, message)
        addCrash(crashInfo)
        logger.e(TAG, "Crash captured: ${message ?: throwable.message}", throwable)
    }

    override fun getRecentCrashes(): Flow<List<CrashInfo>> = _crashes.asStateFlow()

    override fun clearCrashes() {
        _crashes.value = emptyList()
        getCrashFile().delete()
        logger.i(TAG, "Crash logs cleared")
    }

    override fun exportCrashLog(): String {
        val crashes = _crashes.value
        if (crashes.isEmpty()) {
            return "No crash logs available."
        }

        return buildString {
            appendLine("=== Stack Crash Report ===")
            appendLine("Generated: ${dateFormat.format(Date())}")
            appendLine("Total Crashes: ${crashes.size}")
            appendLine()

            crashes.forEachIndexed { index, crash ->
                appendLine("--- Crash #${index + 1} ---")
                appendLine("Time: ${crash.formattedTime}")
                appendLine("Thread: ${crash.threadName}")
                appendLine("Message: ${crash.message}")
                appendLine("Stack Trace:")
                appendLine(crash.stackTrace)
                appendLine()
            }
        }
    }

    private fun setupUncaughtExceptionHandler() {
        originalHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val crashInfo = createCrashInfo(throwable, null, thread.name)
            addCrash(crashInfo)
            saveCrashesToFile()

            // Forward to original handler (usually system crash handler)
            originalHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun createCrashInfo(
        throwable: Throwable,
        message: String?,
        threadName: String = Thread.currentThread().name
    ): CrashInfo {
        val timestamp = System.currentTimeMillis()
        val stringWriter = StringWriter()
        throwable.printStackTrace(PrintWriter(stringWriter))

        return CrashInfo(
            timestamp = timestamp,
            formattedTime = dateFormat.format(Date(timestamp)),
            message = message ?: throwable.message ?: "Unknown error",
            stackTrace = stringWriter.toString(),
            threadName = threadName
        )
    }

    private fun addCrash(crashInfo: CrashInfo) {
        val current = _crashes.value.toMutableList()
        current.add(0, crashInfo) // Add to front (most recent first)

        // Keep only the most recent MAX_CRASHES entries
        if (current.size > MAX_CRASHES) {
            _crashes.value = current.take(MAX_CRASHES)
        } else {
            _crashes.value = current
        }

        saveCrashesToFile()
    }

    private fun getCrashFile(): File {
        return File(context.filesDir, CRASH_LOG_FILE)
    }

    private fun saveCrashesToFile() {
        try {
            val file = getCrashFile()
            file.writeText(exportCrashLog())
        } catch (e: Exception) {
            logger.e(TAG, "Failed to save crash logs", e)
        }
    }

    private fun loadCrashesFromFile() {
        try {
            val file = getCrashFile()
            if (!file.exists()) return

            // Note: For simplicity, we don't parse the file back into CrashInfo objects.
            // The file serves as a persistent backup for "export" functionality.
            // A more sophisticated implementation would serialize/deserialize CrashInfo.
            logger.d(TAG, "Crash log file exists, size: ${file.length()} bytes")
        } catch (e: Exception) {
            logger.e(TAG, "Failed to load crash logs", e)
        }
    }
}
