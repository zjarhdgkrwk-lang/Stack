package com.stack.domain.model

/**
 * Domain model representing lyrics for a track.
 */
data class Lyrics(
    val id: Long,
    val trackId: Long,
    val content: String,
    val isSynced: Boolean,        // Has LRC time codes
    val syncedLines: List<SyncedLine>?,  // Parsed LRC data
    val source: LyricsSource,
    val updatedAt: Long
)

/**
 * A single line of synced lyrics with timestamp.
 */
data class SyncedLine(
    val timestamp: Long,          // ms from start
    val text: String
)

/**
 * Source of lyrics data (SSOT 7.2).
 */
enum class LyricsSource {
    EMBEDDED,      // Embedded in audio file
    SIDECAR_LRC,   // External .lrc file
    USER_INPUT,    // User manually entered
    IMPORTED       // Imported from external source
}
