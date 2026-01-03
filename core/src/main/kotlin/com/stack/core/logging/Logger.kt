package com.stack.core.logging

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Logging abstraction for Stack app.
 * Allows for easy switching between implementations (Logcat, Timber, etc.)
 */
interface Logger {
    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
    fun w(tag: String, message: String)
    fun e(tag: String, message: String, throwable: Throwable? = null)
    fun v(tag: String, message: String)
}

/**
 * Extension functions for Logger with default tag
 */
inline fun <reified T> Logger.d(message: String) = d(T::class.java.simpleName, message)
inline fun <reified T> Logger.i(message: String) = i(T::class.java.simpleName, message)
inline fun <reified T> Logger.w(message: String) = w(T::class.java.simpleName, message)
inline fun <reified T> Logger.e(message: String, throwable: Throwable? = null) =
    e(T::class.java.simpleName, message, throwable)
inline fun <reified T> Logger.v(message: String) = v(T::class.java.simpleName, message)

/**
 * Default Logger implementation using Android Logcat.
 * Can be replaced with Timber or other logging frameworks.
 *
 * Note: In production builds, consider filtering log levels or
 * replacing with a no-op implementation.
 */
@Singleton
class TimberLogger @Inject constructor() : Logger {

    companion object {
        private const val MAX_LOG_LENGTH = 4000
        private const val APP_PREFIX = "Stack."
    }

    override fun d(tag: String, message: String) {
        log(Log.DEBUG, tag, message)
    }

    override fun i(tag: String, message: String) {
        log(Log.INFO, tag, message)
    }

    override fun w(tag: String, message: String) {
        log(Log.WARN, tag, message)
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        val fullMessage = if (throwable != null) {
            "$message\n${Log.getStackTraceString(throwable)}"
        } else {
            message
        }
        log(Log.ERROR, tag, fullMessage)
    }

    override fun v(tag: String, message: String) {
        log(Log.VERBOSE, tag, message)
    }

    /**
     * Handles long messages by splitting them into chunks.
     * Android Logcat has a ~4000 character limit per log entry.
     */
    private fun log(priority: Int, tag: String, message: String) {
        val prefixedTag = "$APP_PREFIX$tag"

        if (message.length <= MAX_LOG_LENGTH) {
            Log.println(priority, prefixedTag, message)
            return
        }

        // Split long messages
        var i = 0
        while (i < message.length) {
            val end = minOf(i + MAX_LOG_LENGTH, message.length)
            Log.println(priority, prefixedTag, message.substring(i, end))
            i = end
        }
    }
}

/**
 * No-op Logger implementation for release builds or testing.
 */
class NoOpLogger : Logger {
    override fun d(tag: String, message: String) = Unit
    override fun i(tag: String, message: String) = Unit
    override fun w(tag: String, message: String) = Unit
    override fun e(tag: String, message: String, throwable: Throwable?) = Unit
    override fun v(tag: String, message: String) = Unit
}
