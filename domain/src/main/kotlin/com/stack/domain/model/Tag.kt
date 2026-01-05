package com.stack.domain.model

/**
 * Domain model representing a tag.
 * Tags can be user-created or system-managed (SSOT 6.3).
 */
data class Tag(
    val id: Long,
    val name: String,
    val color: Int,               // ARGB color value
    val isSystem: Boolean,
    val systemType: SystemTagType?,
    val createdAt: Long,
    val updatedAt: Long,
    val trackCount: Int = 0       // Number of tracks with this tag
) {
    /**
     * Check if this tag can be deleted.
     * System tags cannot be deleted (SSOT 6.3).
     */
    val isDeletable: Boolean
        get() = !isSystem

    /**
     * Check if this tag can be edited.
     */
    val isEditable: Boolean
        get() = !isSystem
}

/**
 * System tag types (SSOT 6.3).
 * These are auto-managed tags with special behaviors.
 */
enum class SystemTagType {
    FAVORITE,       // ❤️ User favorites
    RECENT_PLAY,    // 🕐 Recently played (last 30 days)
    RECENT_ADD,     // 🆕 Recently added (last 30 days)
    MOST_PLAYED     // 🔥 Most played (top 100)
}

/**
 * Represents the relationship between a Track and a Tag.
 */
data class TrackTag(
    val trackId: Long,
    val tagId: Long,
    val taggedAt: Long
)
