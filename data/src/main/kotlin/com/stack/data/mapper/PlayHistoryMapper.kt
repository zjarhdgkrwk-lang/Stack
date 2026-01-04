package com.stack.data.mapper

import com.stack.data.local.db.entity.PlayHistoryEntity
import com.stack.domain.model.PlayHistory

object PlayHistoryMapper {

    fun toDomain(entity: PlayHistoryEntity): PlayHistory {
        return PlayHistory(
            id = entity.id,
            trackId = entity.trackId,
            playedAt = entity.playedAt,
            playDuration = entity.playDuration,
            playCompletionRate = entity.playCompletionRate,
            playSource = entity.playSource
        )
    }

    fun toEntity(domain: PlayHistory): PlayHistoryEntity {
        return PlayHistoryEntity(
            id = domain.id,
            trackId = domain.trackId,
            playedAt = domain.playedAt,
            playDuration = domain.playDuration,
            playCompletionRate = domain.playCompletionRate,
            playSource = domain.playSource
        )
    }

    fun toDomainList(entities: List<PlayHistoryEntity>): List<PlayHistory> {
        return entities.map { toDomain(it) }
    }

    fun toEntityList(domains: List<PlayHistory>): List<PlayHistoryEntity> {
        return domains.map { toEntity(it) }
    }
}
