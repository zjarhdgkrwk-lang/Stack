package com.stack.data.mapper

import com.stack.data.local.db.entity.TrackEntity
import com.stack.domain.model.Track

object TrackMapper {

    fun toDomain(entity: TrackEntity): Track {
        return Track(
            id = entity.id,
            contentUri = entity.contentUri,
            title = entity.title,
            artist = entity.artist,
            albumArtist = entity.albumArtist,
            album = entity.album,
            albumId = entity.albumId,
            artistId = entity.artistId,
            genre = entity.genre,
            year = entity.year,
            trackNumber = entity.trackNumber,
            discNumber = entity.discNumber,
            duration = entity.duration,
            size = entity.size,
            bitRate = entity.bitRate,
            sampleRate = entity.sampleRate,
            mimeType = entity.mimeType,
            folderPath = entity.folderPath,
            fileName = entity.fileName,
            displayPath = entity.displayPath,
            albumArtUri = entity.albumArtUri,
            status = entity.status,
            dateAdded = entity.dateAdded,
            dateModified = entity.dateModified,
            lastScanned = entity.lastScanned
        )
    }

    fun toEntity(domain: Track): TrackEntity {
        return TrackEntity(
            id = domain.id,
            contentUri = domain.contentUri,
            title = domain.title,
            artist = domain.artist,
            albumArtist = domain.albumArtist,
            album = domain.album,
            albumId = domain.albumId,
            artistId = domain.artistId,
            genre = domain.genre,
            year = domain.year,
            trackNumber = domain.trackNumber,
            discNumber = domain.discNumber,
            duration = domain.duration,
            size = domain.size,
            bitRate = domain.bitRate,
            sampleRate = domain.sampleRate,
            mimeType = domain.mimeType,
            folderPath = domain.folderPath,
            fileName = domain.fileName,
            displayPath = domain.displayPath,
            albumArtUri = domain.albumArtUri,
            status = domain.status,
            dateAdded = domain.dateAdded,
            dateModified = domain.dateModified,
            lastScanned = domain.lastScanned
        )
    }

    fun toDomainList(entities: List<TrackEntity>): List<Track> {
        return entities.map { toDomain(it) }
    }

    fun toEntityList(domains: List<Track>): List<TrackEntity> {
        return domains.map { toEntity(it) }
    }
}
