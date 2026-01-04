package com.stack.domain.model

/**
 * Domain model representing a playlist.
 */
data class Playlist(
    val id: Long,
    val name: String,
    val description: String?,
    val coverArtUri: String?,
    val trackCount: Int,
    val totalDuration: Long,      // ms
    val createdAt: Long,
    val updatedAt: Long,
    val isSmartPlaylist: Boolean = false,
    val smartCriteria: String? = null  // JSON for smart playlist rules
) {
    /**
     * Display duration formatted as "X hr Y min" or "Y min"
     */
    val displayDuration: String
        get() {
            val minutes = (totalDuration / 1000 / 60).toInt()
            val hours = minutes / 60
            val remainingMinutes = minutes % 60
            return if (hours > 0) {
                "${hours}hr ${remainingMinutes}min"
            } else {
                "${remainingMinutes}min"
            }
        }
}

/**
 * Represents a track within a playlist with ordering.
 */
data class PlaylistTrack(
    val playlistId: Long,
    val trackId: Long,
    val position: Int,            // Order in playlist (0-indexed)
    val addedAt: Long
)
