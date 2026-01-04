package com.stack.data.repository

import com.stack.data.local.db.dao.SourceFolderDao
import com.stack.data.local.db.entity.SourceFolderEntity
import com.stack.data.mapper.SourceFolderMapper
import com.stack.domain.model.SourceFolder
import com.stack.domain.repository.SourceFolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SourceFolderRepositoryImpl @Inject constructor(
    private val sourceFolderDao: SourceFolderDao
) : SourceFolderRepository {

    override fun observeSourceFolders(): Flow<List<SourceFolder>> {
        return sourceFolderDao.observeAll().map { entities ->
            SourceFolderMapper.toDomainList(entities)
        }
    }

    override fun observeActiveSourceFolders(): Flow<List<SourceFolder>> {
        return sourceFolderDao.observeActive().map { entities ->
            SourceFolderMapper.toDomainList(entities)
        }
    }

    override suspend fun getSourceFolderById(id: Long): SourceFolder? {
        return sourceFolderDao.getById(id)?.let { entity ->
            SourceFolderMapper.toDomain(entity)
        }
    }

    override suspend fun getSourceFolderByUri(treeUri: String): SourceFolder? {
        return sourceFolderDao.getByTreeUri(treeUri)?.let { entity ->
            SourceFolderMapper.toDomain(entity)
        }
    }

    override suspend fun addSourceFolder(
        treeUri: String,
        displayName: String,
        displayPath: String
    ): Long {
        val now = System.currentTimeMillis()
        val entity = SourceFolderEntity(
            treeUri = treeUri,
            displayName = displayName,
            displayPath = displayPath,
            trackCount = 0,
            lastScanned = null,
            addedAt = now,
            isActive = true
        )
        return sourceFolderDao.insert(entity)
    }

    override suspend fun removeSourceFolder(id: Long) {
        sourceFolderDao.deleteById(id)
    }

    override suspend fun updateLastScanned(id: Long, timestamp: Long) {
        sourceFolderDao.updateLastScanned(id, timestamp)
    }

    override suspend fun updateTrackCount(id: Long, count: Int) {
        sourceFolderDao.updateTrackCount(id, count)
    }

    override suspend fun markAsInactive(id: Long) {
        sourceFolderDao.updateActiveStatus(id, false)
    }

    override suspend fun getSourceFolderCount(): Int {
        return sourceFolderDao.getCount()
    }

    override suspend fun hasActiveSourceFolders(): Boolean {
        return sourceFolderDao.getActiveCount() > 0
    }
}
