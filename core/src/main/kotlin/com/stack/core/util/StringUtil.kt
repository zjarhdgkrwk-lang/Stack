package com.stack.core.util

import java.text.Normalizer
import java.util.Locale

/**
 * Utility object for string manipulation operations.
 * Provides consistent string handling throughout the app.
 */
object StringUtil {

    /**
     * Truncate string to specified length with ellipsis.
     * Example: "This is a very long title" -> "This is a very lo..."
     */
    fun truncate(text: String, maxLength: Int, ellipsis: String = "..."): String {
        if (text.length <= maxLength) return text
        if (maxLength <= ellipsis.length) return ellipsis.take(maxLength)
        return text.take(maxLength - ellipsis.length) + ellipsis
    }

    /**
     * Normalize string for search/comparison.
     * Removes diacritics, converts to lowercase.
     * Example: "Café" -> "cafe"
     */
    fun normalize(text: String): String {
        val normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
        return normalized
            .replace(Regex("\\p{M}"), "") // Remove diacritics
            .lowercase(Locale.getDefault())
    }

    /**
     * Extract initials from text.
     * Used for album art placeholders.
     * Example: "The Beatles" -> "TB"
     */
    fun getInitials(text: String, maxChars: Int = 2): String {
        return text
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .take(maxChars)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
    }

    /**
     * Format file size in human-readable format.
     * Example: 1536000 -> "1.5 MB"
     */
    fun formatFileSize(bytes: Long): String {
        if (bytes < 0) return "0 B"

        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0

        while (size >= 1024 && unitIndex < units.lastIndex) {
            size /= 1024
            unitIndex++
        }

        return if (unitIndex == 0) {
            "${size.toLong()} ${units[unitIndex]}"
        } else {
            String.format(Locale.getDefault(), "%.1f %s", size, units[unitIndex])
        }
    }

    /**
     * Format bitrate in human-readable format.
     * Example: 320000 -> "320 kbps"
     */
    fun formatBitrate(bitsPerSecond: Long): String {
        val kbps = bitsPerSecond / 1000
        return "$kbps kbps"
    }

    /**
     * Format sample rate in human-readable format.
     * Example: 44100 -> "44.1 kHz"
     */
    fun formatSampleRate(hz: Int): String {
        val khz = hz / 1000.0
        return if (khz == khz.toLong().toDouble()) {
            "${khz.toLong()} kHz"
        } else {
            String.format(Locale.getDefault(), "%.1f kHz", khz)
        }
    }

    /**
     * Clean filename from path.
     * Example: "/storage/music/Song.mp3" -> "Song"
     */
    fun extractFileName(path: String, removeExtension: Boolean = true): String {
        val name = path.substringAfterLast('/')
        return if (removeExtension) {
            name.substringBeforeLast('.')
        } else {
            name
        }
    }

    /**
     * Extract file extension.
     * Example: "song.mp3" -> "mp3"
     */
    fun getExtension(path: String): String {
        return path.substringAfterLast('.', "").lowercase(Locale.getDefault())
    }

    /**
     * Check if string contains another string (case-insensitive).
     */
    fun containsIgnoreCase(text: String, query: String): Boolean {
        return text.lowercase(Locale.getDefault())
            .contains(query.lowercase(Locale.getDefault()))
    }

    /**
     * Join strings with a separator, filtering out blanks.
     * Example: listOf("Artist", "", "Album") -> "Artist • Album"
     */
    fun joinNonBlank(vararg parts: String?, separator: String = " • "): String {
        return parts
            .filterNotNull()
            .filter { it.isNotBlank() }
            .joinToString(separator)
    }

    /**
     * Pluralize a word based on count.
     * Example: pluralize(5, "track", "tracks") -> "5 tracks"
     */
    fun pluralize(count: Int, singular: String, plural: String): String {
        return "$count ${if (count == 1) singular else plural}"
    }

    /**
     * Format track count.
     * Example: 15 -> "15 tracks"
     */
    fun formatTrackCount(count: Int): String {
        return pluralize(count, "track", "tracks")
    }

    /**
     * Format album count.
     */
    fun formatAlbumCount(count: Int): String {
        return pluralize(count, "album", "albums")
    }

    /**
     * Check if string is a valid URI/path.
     */
    fun isValidPath(path: String?): Boolean {
        return !path.isNullOrBlank() &&
               (path.startsWith("/") || path.startsWith("content://"))
    }

    /**
     * Sanitize string for use as filename.
     * Removes/replaces invalid characters.
     */
    fun sanitizeFileName(name: String): String {
        return name
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .replace(Regex("\\s+"), " ")
            .trim()
            .take(255)
    }
}
