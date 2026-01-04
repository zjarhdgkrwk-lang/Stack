package com.stack.domain.repository

import com.stack.domain.model.SystemTagType
import com.stack.domain.model.Tag
import com.stack.domain.model.Track
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Tag operations.
 */
interface TagRepository {

    /**
     * Observe all tags.
     */
    fun observeTags(): Flow<List<Tag>>

    /**
     * Observe system tags only.
     */
    fun observeSystemTags(): Flow<List<Tag>>

    /**
     * Observe user-created tags.
     */
    fun observeUserTags(): Flow<List<Tag>>

    /**
     * Observe tags for a specific track.
     */
    fun observeTagsForTrack(trackId: Long): Flow<List<Tag>>

    /**
     * Observe tracks for a specific tag.
     */
    fun observeTracksForTag(tagId: Long): Flow<List<Track>>

    /**
     * Get tag by ID.
     */
    suspend fun getTagById(id: Long): Tag?

    /**
     * Get tag by name.
     */
    suspend fun getTagByName(name: String): Tag?

    /**
     * Get system tag by type.
     */
    suspend fun getSystemTag(type: SystemTagType): Tag?

    /**
     * Create a new user tag.
     */
    suspend fun createTag(name: String, color: Int): Long

    /**
     * Update tag (name, color). System tags cannot be updated.
     */
    suspend fun updateTag(tag: Tag)

    /**
     * Delete tag by ID. System tags cannot be deleted.
     */
    suspend fun deleteTag(tagId: Long)

    /**
     * Add tag to track.
     */
    suspend fun addTagToTrack(trackId: Long, tagId: Long)

    /**
     * Remove tag from track.
     */
    suspend fun removeTagFromTrack(trackId: Long, tagId: Long)

    /**
     * Set tags for a track (replaces existing non-system tags).
     */
    suspend fun setTagsForTrack(trackId: Long, tagIds: List<Long>)

    /**
     * Toggle favorite status for a track.
     */
    suspend fun toggleFavorite(trackId: Long): Boolean

    /**
     * Check if track is favorited.
     */
    suspend fun isFavorite(trackId: Long): Boolean

    /**
     * Initialize system tags if not exist.
     */
    suspend fun ensureSystemTagsExist()
}
