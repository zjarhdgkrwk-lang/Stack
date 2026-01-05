package com.stack.data.mapper

import com.stack.data.local.db.entity.PlaylistEntity
import com.stack.domain.model.Playlist

object PlaylistMapper {

    fun toDomain(entity: PlaylistEntity, trackCount: Int, totalDuration: Long): Playlist {
        return Playlist(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            coverArtUri = entity.coverArtUri,
            trackCount = trackCount,
            totalDuration = totalDuration,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            isSmartPlaylist = entity.isSmartPlaylist,
            smartCriteria = entity.smartCriteria
        )
    }

    fun toEntity(domain: Playlist): PlaylistEntity {
        return PlaylistEntity(
            id = domain.id,
            name = domain.name,
            description = domain.description,
            coverArtUri = domain.coverArtUri,
            isSmartPlaylist = domain.isSmartPlaylist,
            smartCriteria = domain.smartCriteria,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
}
