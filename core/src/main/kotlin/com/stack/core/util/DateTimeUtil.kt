package com.stack.core.util

import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Utility object for date and time formatting operations.
 * Provides consistent formatting throughout the app.
 */
object DateTimeUtil {

    private val mediumDateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    private val shortDateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
    private val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    /**
     * Format duration in milliseconds to MM:SS or HH:MM:SS format.
     * Used for track duration display.
     */
    fun formatDuration(durationMs: Long): String {
        if (durationMs < 0) return "0:00"

        val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60

        return if (hours > 0) {
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
        }
    }

    /**
     * Format duration in seconds to MM:SS or HH:MM:SS format.
     */
    fun formatDurationSeconds(durationSeconds: Long): String {
        return formatDuration(durationSeconds * 1000)
    }

    /**
     * Format timestamp to localized date string.
     * Example: "Jan 3, 2025"
     */
    fun formatDate(timestamp: Long): String {
        val instant = Instant.ofEpochMilli(timestamp)
        val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        return localDate.format(mediumDateFormatter)
    }

    /**
     * Format timestamp to short date string.
     * Example: "1/3/25"
     */
    fun formatDateShort(timestamp: Long): String {
        val instant = Instant.ofEpochMilli(timestamp)
        val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        return localDate.format(shortDateFormatter)
    }

    /**
     * Format timestamp to time string.
     * Example: "3:45 PM"
     */
    fun formatTime(timestamp: Long): String {
        val instant = Instant.ofEpochMilli(timestamp)
        val localTime = instant.atZone(ZoneId.systemDefault()).toLocalTime()
        return localTime.format(timeFormatter)
    }

    /**
     * Format timestamp to date and time string.
     * Example: "Jan 3, 2025 3:45 PM"
     */
    fun formatDateTime(timestamp: Long): String {
        return "${formatDate(timestamp)} ${formatTime(timestamp)}"
    }

    /**
     * Format timestamp to relative time string.
     * Examples: "Just now", "5 minutes ago", "Yesterday", "Jan 3"
     */
    fun formatRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "$hours ${if (hours == 1L) "hour" else "hours"} ago"
            }
            diff < TimeUnit.DAYS.toMillis(2) -> "Yesterday"
            diff < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                "$days days ago"
            }
            else -> formatDate(timestamp)
        }
    }

    /**
     * Get LocalDate from timestamp.
     */
    fun toLocalDate(timestamp: Long): LocalDate {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    /**
     * Get LocalDateTime from timestamp.
     */
    fun toLocalDateTime(timestamp: Long): LocalDateTime {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }

    /**
     * Get timestamp from LocalDate (start of day).
     */
    fun toTimestamp(date: LocalDate): Long {
        return date.atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    /**
     * Get timestamp from LocalDateTime.
     */
    fun toTimestamp(dateTime: LocalDateTime): Long {
        return dateTime.atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    /**
     * Check if timestamp is today.
     */
    fun isToday(timestamp: Long): Boolean {
        return toLocalDate(timestamp) == LocalDate.now()
    }

    /**
     * Check if timestamp is yesterday.
     */
    fun isYesterday(timestamp: Long): Boolean {
        return toLocalDate(timestamp) == LocalDate.now().minusDays(1)
    }

    /**
     * Check if timestamp is within the last N days.
     */
    fun isWithinDays(timestamp: Long, days: Int): Boolean {
        val date = toLocalDate(timestamp)
        val threshold = LocalDate.now().minusDays(days.toLong())
        return !date.isBefore(threshold)
    }

    /**
     * Get start of today timestamp.
     */
    fun startOfToday(): Long {
        return toTimestamp(LocalDate.now())
    }

    /**
     * Get start of week timestamp.
     */
    fun startOfWeek(): Long {
        val today = LocalDate.now()
        val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        return toTimestamp(startOfWeek)
    }

    /**
     * Format year from timestamp.
     */
    fun formatYear(timestamp: Long): String {
        return toLocalDate(timestamp).year.toString()
    }
}
