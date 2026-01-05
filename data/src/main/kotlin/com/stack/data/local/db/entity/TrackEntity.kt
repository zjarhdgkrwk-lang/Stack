package com.stack.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.stack.domain.model.TrackStatus

@Entity(
    tableName = "tracks",
    indices = [
        Index("content_uri", unique = true),
        Index("album_id"),
        Index("artist_id"),
        Index("folder_path"),
        Index("status")
    ]
)
data class TrackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Identifier
    @ColumnInfo(name = "content_uri")
    val contentUri: String,

    // Metadata
    val title: String,
    val artist: String?,
    @ColumnInfo(name = "album_artist")
    val albumArtist: String?,
    val album: String?,
    @ColumnInfo(name = "album_id")
    val albumId: Long?,
    @ColumnInfo(name = "artist_id")
    val artistId: Long?,
    val genre: String?,
    val year: Int?,
    @ColumnInfo(name = "track_number")
    val trackNumber: Int?,
    @ColumnInfo(name = "disc_number")
    val discNumber: Int?,

    // File info
    val duration: Long,           // ms
    val size: Long,               // bytes
    @ColumnInfo(name = "bit_rate")
    val bitRate: Int?,            // kbps
    @ColumnInfo(name = "sample_rate")
    val sampleRate: Int?,         // Hz
    @ColumnInfo(name = "mime_type")
    val mimeType: String,

    // Path info
    @ColumnInfo(name = "folder_path")
    val folderPath: String,
    @ColumnInfo(name = "file_name")
    val fileName: String,
    @ColumnInfo(name = "display_path")
    val displayPath: String,

    // Album art
    @ColumnInfo(name = "album_art_uri")
    val albumArtUri: String?,

    // Status
    val status: TrackStatus = TrackStatus.ACTIVE,

    // Timestamps
    @ColumnInfo(name = "date_added")
    val dateAdded: Long,
    @ColumnInfo(name = "date_modified")
    val dateModified: Long,
    @ColumnInfo(name = "last_scanned")
    val lastScanned: Long
)
