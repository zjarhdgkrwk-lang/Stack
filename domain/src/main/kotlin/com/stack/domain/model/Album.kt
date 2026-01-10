package com.stack.domain.model

/**
 * Domain model representing a music album.
 * Aggregates tracks from the same album.
 */
data class Album(
    val id: Long,
    val name: String,
    val artistId: Long,
    val artistName: String,
    val artworkUri: String? = null,
    val year: Int? = null,
    val trackCount: Int = 0
)
