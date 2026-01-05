package com.stack.domain.model

import java.time.LocalDate

/**
 * Domain model representing a play history record (SSOT 6.4).
 */
data class PlayHistory(
    val id: Long,
    val trackId: Long,
    val playedAt: Long,
    val playedDuration: Long,     // ms actually played
    val completed: Boolean,       // Played to completion
    val weekKey: String           // "2025-W01" format for aggregation
)

/**
 * Aggregated weekly play statistics.
 */
data class WeeklyStat(
    val weekKey: String,
    val trackId: Long,
    val playCount: Int,
    val totalDuration: Long       // Total ms played that week
)

/**
 * Track play statistics summary.
 */
data class TrackPlayStats(
    val trackId: Long,
    val totalPlayCount: Int,
    val totalPlayDuration: Long,
    val lastPlayedAt: Long?,
    val firstPlayedAt: Long?
)
