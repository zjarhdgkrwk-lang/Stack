package com.stack.data.local.db.converter

import androidx.room.TypeConverter
import com.stack.domain.model.LyricsSource
import com.stack.domain.model.SystemTagType
import com.stack.domain.model.TrackStatus

/**
 * Room TypeConverters for custom enum types.
 */
class Converters {

    // TrackStatus
    @TypeConverter
    fun fromTrackStatus(status: TrackStatus): String = status.name

    @TypeConverter
    fun toTrackStatus(value: String): TrackStatus = TrackStatus.valueOf(value)

    // SystemTagType
    @TypeConverter
    fun fromSystemTagType(type: SystemTagType?): String? = type?.name

    @TypeConverter
    fun toSystemTagType(value: String?): SystemTagType? =
        value?.let { SystemTagType.valueOf(it) }

    // LyricsSource
    @TypeConverter
    fun fromLyricsSource(source: LyricsSource): String = source.name

    @TypeConverter
    fun toLyricsSource(value: String): LyricsSource = LyricsSource.valueOf(value)
}
