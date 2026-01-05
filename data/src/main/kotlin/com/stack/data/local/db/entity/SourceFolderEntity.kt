package com.stack.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "source_folders",
    indices = [Index("tree_uri", unique = true)]
)
data class SourceFolderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "tree_uri")
    val treeUri: String,
    @ColumnInfo(name = "display_name")
    val displayName: String,
    @ColumnInfo(name = "display_path")
    val displayPath: String,
    @ColumnInfo(name = "track_count")
    val trackCount: Int = 0,
    @ColumnInfo(name = "last_scanned")
    val lastScanned: Long? = null,
    @ColumnInfo(name = "added_at")
    val addedAt: Long,
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true
)
