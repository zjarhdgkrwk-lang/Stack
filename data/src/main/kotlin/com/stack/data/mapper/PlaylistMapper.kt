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

    /**
     * Convert entity to domain with default zero counts (for list mapping).
     * Caller should populate trackCount and totalDuration separately.
     */
    fun toDomain(entity: PlaylistEntity): Playlist {
        return toDomain(entity, trackCount = 0, totalDuration = 0L)
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

    fun toDomainList(entities: List<PlaylistEntity>): List<Playlist> {
        return entities.map { toDomain(it) }
    }
}
