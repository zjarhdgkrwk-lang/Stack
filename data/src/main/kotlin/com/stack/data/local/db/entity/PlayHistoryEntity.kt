package com.stack.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "play_history",
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["track_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("track_id"),
        Index("played_at"),
        Index("week_key")
    ]
)
data class PlayHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "track_id")
    val trackId: Long,
    @ColumnInfo(name = "played_at")
    val playedAt: Long,
    @ColumnInfo(name = "played_duration")
    val playedDuration: Long,
    val completed: Boolean,
    @ColumnInfo(name = "week_key")
    val weekKey: String           // "2025-W01" format
)
