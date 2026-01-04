package com.stack.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.stack.data.local.db.entity.TagEntity
import com.stack.data.local.db.entity.TrackEntity
import com.stack.data.local.db.entity.TrackTagCrossRef
import com.stack.domain.model.SystemTagType
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    // ===== Tags =====

    @Query("SELECT * FROM tags ORDER BY is_system DESC, name ASC")
    fun observeAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE is_system = 1")
    fun observeSystemTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE is_system = 0 ORDER BY name ASC")
    fun observeUserTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getById(id: Long): TagEntity?

    @Query("SELECT * FROM tags WHERE name = :name")
    suspend fun getByName(name: String): TagEntity?

    @Query("SELECT * FROM tags WHERE system_type = :type")
    suspend fun getBySystemType(type: SystemTagType): TagEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: TagEntity): Long

    @Update
    suspend fun update(tag: TagEntity)

    @Query("DELETE FROM tags WHERE id = :tagId AND is_system = 0")
    suspend fun deleteUserTag(tagId: Long)

    // ===== Tag-Track Relations =====

    @Query("""
        SELECT tags.* FROM tags
        INNER JOIN track_tag_cross_ref ON tags.id = track_tag_cross_ref.tag_id
        WHERE track_tag_cross_ref.track_id = :trackId
        ORDER BY tags.is_system DESC, tags.name ASC
    """)
    fun observeTagsForTrack(trackId: Long): Flow<List<TagEntity>>

    @Query("""
        SELECT tracks.* FROM tracks
        INNER JOIN track_tag_cross_ref ON tracks.id = track_tag_cross_ref.track_id
        WHERE track_tag_cross_ref.tag_id = :tagId AND tracks.status = 'ACTIVE'
        ORDER BY tracks.title ASC
    """)
    fun observeTracksForTag(tagId: Long): Flow<List<TrackEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: TrackTagCrossRef)

    @Query("DELETE FROM track_tag_cross_ref WHERE track_id = :trackId AND tag_id = :tagId")
    suspend fun deleteCrossRef(trackId: Long, tagId: Long)

    @Query("DELETE FROM track_tag_cross_ref WHERE track_id = :trackId")
    suspend fun deleteAllCrossRefsForTrack(trackId: Long)

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM track_tag_cross_ref
            WHERE track_id = :trackId AND tag_id = :tagId
        )
    """)
    suspend fun hasTagOnTrack(trackId: Long, tagId: Long): Boolean

    // ===== Tag track count =====

    @Query("""
        SELECT COUNT(*) FROM track_tag_cross_ref
        INNER JOIN tracks ON track_tag_cross_ref.track_id = tracks.id
        WHERE track_tag_cross_ref.tag_id = :tagId AND tracks.status = 'ACTIVE'
    """)
    suspend fun getTrackCountForTag(tagId: Long): Int
}
