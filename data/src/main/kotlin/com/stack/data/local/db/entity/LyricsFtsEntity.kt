package com.stack.data.local.db.entity

import androidx.room.Entity
import androidx.room.Fts4

@Entity(tableName = "lyrics_fts")
@Fts4(contentEntity = LyricsEntity::class)
data class LyricsFtsEntity(
    val content: String
)
