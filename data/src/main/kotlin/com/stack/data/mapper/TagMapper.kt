package com.stack.data.mapper

import com.stack.data.local.db.entity.TagEntity
import com.stack.domain.model.Tag

object TagMapper {

    fun toDomain(entity: TagEntity, trackCount: Int = 0): Tag {
        return Tag(
            id = entity.id,
            name = entity.name,
            color = entity.color,
            isSystem = entity.isSystem,
            systemType = entity.systemType,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            trackCount = trackCount
        )
    }

    fun toEntity(domain: Tag): TagEntity {
        return TagEntity(
            id = domain.id,
            name = domain.name,
            color = domain.color,
            isSystem = domain.isSystem,
            systemType = domain.systemType,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    fun toDomainList(entities: List<TagEntity>): List<Tag> {
        return entities.map { toDomain(it) }
    }

    fun toEntityList(domains: List<Tag>): List<TagEntity> {
        return domains.map { toEntity(it) }
    }
}
