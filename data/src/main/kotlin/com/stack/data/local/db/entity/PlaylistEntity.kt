package com.stack.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "playlists",
    indices = [Index("name")]
)
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String?,
    @ColumnInfo(name = "cover_art_uri")
    val coverArtUri: String?,
    @ColumnInfo(name = "is_smart_playlist")
    val isSmartPlaylist: Boolean = false,
    @ColumnInfo(name = "smart_criteria")
    val smartCriteria: String? = null,  // JSON for smart playlist rules
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
