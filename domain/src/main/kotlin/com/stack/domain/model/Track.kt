package com.stack.domain.model

/**
 * Domain model representing a music track.
 * This is a pure Kotlin object independent of Room or any framework.
 */
data class Track(
    val id: Long,
    val contentUri: String,

    // Metadata
    val title: String,
    val artist: String?,
    val albumArtist: String?,
    val album: String?,
    val albumId: Long?,
    val artistId: Long?,
    val genre: String?,
    val year: Int?,
    val trackNumber: Int?,
    val discNumber: Int?,

    // File info
    val duration: Long,           // ms
    val size: Long,               // bytes
    val bitRate: Int?,            // kbps
    val sampleRate: Int?,         // Hz
    val mimeType: String,

    // Path info
    val folderPath: String,
    val fileName: String,
    val displayPath: String,

    // Album art
    val albumArtUri: String?,

    // Status
    val status: TrackStatus,

    // Timestamps
    val dateAdded: Long,
    val dateModified: Long,
    val lastScanned: Long
) {
    /**
     * Display name: title or filename without extension
     */
    val displayTitle: String
        get() = title.ifBlank { fileName.substringBeforeLast('.') }

    /**
     * Display artist: artist or "Unknown Artist"
     */
    val displayArtist: String
        get() = artist?.takeIf { it.isNotBlank() } ?: "Unknown Artist"

    /**
     * Display album: album or "Unknown Album"
     */
    val displayAlbum: String
        get() = album?.takeIf { it.isNotBlank() } ?: "Unknown Album"

    /**
     * Check if track is playable (not ghost/deleted)
     */
    val isPlayable: Boolean
        get() = status == TrackStatus.ACTIVE
}

/**
 * Track status for Ghost tracking (SSOT 5.3)
 */
enum class TrackStatus {
    ACTIVE,    // Normal, playable
    GHOST,     // File not accessible (deleted/moved)
    DELETED    // User confirmed deletion from library
}
