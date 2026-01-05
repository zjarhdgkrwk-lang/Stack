package com.stack.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.stack.data.local.db.entity.SourceFolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceFolderDao {

    @Query("SELECT * FROM source_folders ORDER BY added_at DESC")
    fun observeAll(): Flow<List<SourceFolderEntity>>

    @Query("SELECT * FROM source_folders WHERE is_active = 1 ORDER BY added_at DESC")
    fun observeActive(): Flow<List<SourceFolderEntity>>

    @Query("SELECT * FROM source_folders WHERE id = :id")
    suspend fun getById(id: Long): SourceFolderEntity?

    @Query("SELECT * FROM source_folders WHERE tree_uri = :treeUri")
    suspend fun getByTreeUri(treeUri: String): SourceFolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: SourceFolderEntity): Long

    @Update
    suspend fun update(folder: SourceFolderEntity)

    @Query("DELETE FROM source_folders WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE source_folders SET last_scanned = :timestamp WHERE id = :id")
    suspend fun updateLastScanned(id: Long, timestamp: Long)

    @Query("UPDATE source_folders SET track_count = :count WHERE id = :id")
    suspend fun updateTrackCount(id: Long, count: Int)

    @Query("UPDATE source_folders SET is_active = :isActive WHERE id = :id")
    suspend fun updateActiveStatus(id: Long, isActive: Boolean)

    @Query("SELECT COUNT(*) FROM source_folders")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM source_folders WHERE is_active = 1")
    suspend fun getActiveCount(): Int
}
