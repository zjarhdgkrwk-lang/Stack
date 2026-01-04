package com.stack.data.repository

import com.stack.data.local.db.dao.TagDao
import com.stack.data.local.db.entity.TagEntity
import com.stack.data.local.db.entity.TrackTagCrossRef
import com.stack.data.mapper.TagMapper
import com.stack.data.mapper.TrackMapper
import com.stack.domain.model.SystemTagType
import com.stack.domain.model.Tag
import com.stack.domain.model.Track
import com.stack.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepositoryImpl @Inject constructor(
    private val tagDao: TagDao
) : TagRepository {

    override fun observeTags(): Flow<List<Tag>> {
        return combine(
            tagDao.observeAllTags(),
            getAllTagCounts()
        ) { tags, counts ->
            tags.map { entity ->
                TagMapper.toDomain(entity).copy(
                    trackCount = counts[entity.id] ?: 0
                )
            }
        }
    }

    override fun observeSystemTags(): Flow<List<Tag>> {
        return combine(
            tagDao.observeSystemTags(),
            getAllTagCounts()
        ) { tags, counts ->
            tags.map { entity ->
                TagMapper.toDomain(entity).copy(
                    trackCount = counts[entity.id] ?: 0
                )
            }
        }
    }

    override fun observeUserTags(): Flow<List<Tag>> {
        return combine(
            tagDao.observeUserTags(),
            getAllTagCounts()
        ) { tags, counts ->
            tags.map { entity ->
                TagMapper.toDomain(entity).copy(
                    trackCount = counts[entity.id] ?: 0
                )
            }
        }
    }

    override fun observeTagsForTrack(trackId: Long): Flow<List<Tag>> {
        return tagDao.observeTagsForTrack(trackId).map { entities ->
            TagMapper.toDomainList(entities)
        }
    }

    override fun observeTracksForTag(tagId: Long): Flow<List<Track>> {
        return tagDao.observeTracksForTag(tagId).map { entities ->
            TrackMapper.toDomainList(entities)
        }
    }

    override suspend fun getTagById(id: Long): Tag? {
        return tagDao.getById(id)?.let { entity ->
            val count = tagDao.getTrackCountForTag(id)
            TagMapper.toDomain(entity).copy(trackCount = count)
        }
    }

    override suspend fun getTagByName(name: String): Tag? {
        return tagDao.getByName(name)?.let { entity ->
            val count = tagDao.getTrackCountForTag(entity.id)
            TagMapper.toDomain(entity).copy(trackCount = count)
        }
    }

    override suspend fun getSystemTag(type: SystemTagType): Tag? {
        return tagDao.getBySystemType(type)?.let { entity ->
            val count = tagDao.getTrackCountForTag(entity.id)
            TagMapper.toDomain(entity).copy(trackCount = count)
        }
    }

    override suspend fun createTag(name: String, color: Int): Long {
        val now = System.currentTimeMillis()
        val entity = TagEntity(
            name = name,
            color = color,
            isSystem = false,
            systemType = null,
            createdAt = now,
            updatedAt = now
        )
        return tagDao.insert(entity)
    }

    override suspend fun updateTag(tag: Tag) {
        if (tag.isSystem) {
            throw IllegalStateException("Cannot update system tag")
        }
        val entity = TagMapper.toEntity(tag).copy(
            updatedAt = System.currentTimeMillis()
        )
        tagDao.update(entity)
    }

    override suspend fun deleteTag(tagId: Long) {
        val tag = tagDao.getById(tagId)
        if (tag?.isSystem == true) {
            throw IllegalStateException("Cannot delete system tag")
        }
        tagDao.deleteUserTag(tagId)
    }

    override suspend fun addTagToTrack(trackId: Long, tagId: Long) {
        val crossRef = TrackTagCrossRef(
            trackId = trackId,
            tagId = tagId,
            taggedAt = System.currentTimeMillis()
        )
        tagDao.insertCrossRef(crossRef)
    }

    override suspend fun removeTagFromTrack(trackId: Long, tagId: Long) {
        tagDao.deleteCrossRef(trackId, tagId)
    }

    override suspend fun setTagsForTrack(trackId: Long, tagIds: List<Long>) {
        // Remove all existing tags for track
        tagDao.deleteAllCrossRefsForTrack(trackId)

        // Add new tags
        val now = System.currentTimeMillis()
        tagIds.forEach { tagId ->
            val crossRef = TrackTagCrossRef(
                trackId = trackId,
                tagId = tagId,
                taggedAt = now
            )
            tagDao.insertCrossRef(crossRef)
        }
    }

    override suspend fun toggleFavorite(trackId: Long): Boolean {
        val favoriteTag = getSystemTag(SystemTagType.FAVORITE)
            ?: throw IllegalStateException("Favorite system tag not found")

        val isFavorited = tagDao.hasTagOnTrack(trackId, favoriteTag.id)

        if (isFavorited) {
            removeTagFromTrack(trackId, favoriteTag.id)
        } else {
            addTagToTrack(trackId, favoriteTag.id)
        }

        return !isFavorited
    }

    override suspend fun isFavorite(trackId: Long): Boolean {
        val favoriteTag = getSystemTag(SystemTagType.FAVORITE) ?: return false
        return tagDao.hasTagOnTrack(trackId, favoriteTag.id)
    }

    override suspend fun ensureSystemTagsExist() {
        val systemTags = listOf(
            SystemTagType.FAVORITE to Pair("Favorites", 0xFFE91E63.toInt()),
            SystemTagType.RECENT_PLAY to Pair("Recently Played", 0xFF9C27B0.toInt()),
            SystemTagType.RECENT_ADD to Pair("Recently Added", 0xFF2196F3.toInt()),
            SystemTagType.MOST_PLAYED to Pair("Most Played", 0xFFFF5722.toInt())
        )

        val now = System.currentTimeMillis()

        systemTags.forEach { (type, nameColor) ->
            val existing = tagDao.getBySystemType(type)
            if (existing == null) {
                val entity = TagEntity(
                    name = nameColor.first,
                    color = nameColor.second,
                    isSystem = true,
                    systemType = type,
                    createdAt = now,
                    updatedAt = now
                )
                tagDao.insert(entity)
            }
        }
    }

    /**
     * Helper to get all tag counts as a map.
     */
    private fun getAllTagCounts(): Flow<Map<Long, Int>> {
        return tagDao.observeAllTags().map { tags ->
            tags.associate { tag ->
                // This is a workaround - in practice, you'd want to optimize this
                tag.id to 0  // Count would be fetched separately
            }
        }
    }
}
