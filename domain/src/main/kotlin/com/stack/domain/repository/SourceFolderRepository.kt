package com.stack.domain.repository

import com.stack.domain.model.SourceFolder
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Source Folder operations (SAF).
 */
interface SourceFolderRepository {

    /**
     * Observe all source folders.
     */
    fun observeSourceFolders(): Flow<List<SourceFolder>>

    /**
     * Observe active source folders only.
     */
    fun observeActiveSourceFolders(): Flow<List<SourceFolder>>

    /**
     * Get source folder by ID.
     */
    suspend fun getSourceFolderById(id: Long): SourceFolder?

    /**
     * Get source folder by tree URI.
     */
    suspend fun getSourceFolderByUri(treeUri: String): SourceFolder?

    /**
     * Add a new source folder.
     */
    suspend fun addSourceFolder(
        treeUri: String,
        displayName: String,
        displayPath: String
    ): Long

    /**
     * Remove a source folder.
     */
    suspend fun removeSourceFolder(id: Long)

    /**
     * Update last scanned time.
     */
    suspend fun updateLastScanned(id: Long, timestamp: Long)

    /**
     * Update track count.
     */
    suspend fun updateTrackCount(id: Long, count: Int)

    /**
     * Mark folder as inactive (permission lost).
     */
    suspend fun markAsInactive(id: Long)

    /**
     * Get source folder count.
     */
    suspend fun getSourceFolderCount(): Int

    /**
     * Check if any active source folders exist.
     */
    suspend fun hasActiveSourceFolders(): Boolean
}
