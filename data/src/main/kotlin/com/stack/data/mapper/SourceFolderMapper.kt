package com.stack.data.mapper

import com.stack.data.local.db.entity.SourceFolderEntity
import com.stack.domain.model.SourceFolder

object SourceFolderMapper {

    fun toDomain(entity: SourceFolderEntity): SourceFolder {
        return SourceFolder(
            id = entity.id,
            treeUri = entity.treeUri,
            displayName = entity.displayName,
            displayPath = entity.displayPath,
            trackCount = entity.trackCount,
            lastScanned = entity.lastScanned,
            addedAt = entity.addedAt,
            isActive = entity.isActive
        )
    }

    fun toEntity(domain: SourceFolder): SourceFolderEntity {
        return SourceFolderEntity(
            id = domain.id,
            treeUri = domain.treeUri,
            displayName = domain.displayName,
            displayPath = domain.displayPath,
            trackCount = domain.trackCount,
            lastScanned = domain.lastScanned,
            addedAt = domain.addedAt,
            isActive = domain.isActive
        )
    }

    fun toDomainList(entities: List<SourceFolderEntity>): List<SourceFolder> {
        return entities.map { toDomain(it) }
    }

    fun toEntityList(domains: List<SourceFolder>): List<SourceFolderEntity> {
        return domains.map { toEntity(it) }
    }
}
