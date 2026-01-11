package com.stack.domain.usecase.scan

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import com.stack.core.logging.Logger
import com.stack.core.util.Result
import com.stack.domain.model.ScanTrigger
import com.stack.domain.model.SourceFolder
import com.stack.domain.repository.ScanRepository
import com.stack.domain.repository.SourceFolderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Manages SAF source folders.
 *
 * SSOT 3.1: Users must select at least 1 SAF folder (GateReady requirement)
 * SSOT 3.2: Folders use Persisted URI Permission
 */
class ManageSourceFoldersUseCase @Inject constructor(
    private val sourceFolderRepository: SourceFolderRepository,
    private val scanRepository: ScanRepository,
    private val contentResolver: ContentResolver,
    private val logger: Logger
) {
    companion object {
        private const val TAG = "ManageSourceFoldersUseCase"
    }

    /**
     * Get all registered source folders
     */
    fun getSourceFolders(): Flow<List<SourceFolder>> {
        return sourceFolderRepository.observeSourceFolders()
    }

    /**
     * Add a new source folder from SAF picker result.
     *
     * @param treeUri The tree URI returned from ACTION_OPEN_DOCUMENT_TREE
     * @return Result indicating success or failure
     */
    suspend fun addSourceFolder(treeUri: Uri): Result<SourceFolder> {
        return try {
            // Step 1: Take persistable permission (SSOT 3.1)
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            contentResolver.takePersistableUriPermission(treeUri, takeFlags)
            logger.d(TAG, "Took persistable permission for: $treeUri")

            // Step 2: Extract display name and path
            val displayName = extractDisplayName(treeUri)
            val displayPath = extractDisplayPath(treeUri)

            // Step 3: Save to database
            val id = sourceFolderRepository.addSourceFolder(
                treeUri = treeUri.toString(),
                displayName = displayName,
                displayPath = displayPath
            )

            val savedFolder = sourceFolderRepository.getSourceFolderById(id)
                ?: return Result.Error(Exception("Failed to retrieve saved folder"))

            // Step 4: Trigger scan for this folder (SSOT 5.1)
            scanRepository.queueDebouncedScan(ScanTrigger.SourceFolderAdded(treeUri.toString()))

            logger.d(TAG, "Added source folder: $displayName")
            Result.Success(savedFolder)

        } catch (e: SecurityException) {
            logger.e(TAG, "Failed to take permission for: $treeUri", e)
            Result.Error(e)
        } catch (e: Exception) {
            logger.e(TAG, "Failed to add source folder", e)
            Result.Error(e)
        }
    }

    /**
     * Remove a source folder.
     */
    suspend fun removeSourceFolder(folderId: Long): Result<Unit> {
        return try {
            val folder = sourceFolderRepository.getSourceFolderById(folderId)
            if (folder != null) {
                // Release persistable permission
                try {
                    val uri = Uri.parse(folder.treeUri)
                    contentResolver.releasePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    logger.w(TAG, "Failed to release permission (may already be released)")
                }

                sourceFolderRepository.removeSourceFolder(folderId)
                logger.d(TAG, "Removed source folder: ${folder.displayName}")
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            logger.e(TAG, "Failed to remove source folder", e)
            Result.Error(e)
        }
    }

    /**
     * Get count of active source folders (for Gate check)
     */
    suspend fun getSourceFolderCount(): Int {
        return sourceFolderRepository.getSourceFolderCount()
    }

    /**
     * Extracts a user-friendly name from the tree URI
     */
    private fun extractDisplayName(treeUri: Uri): String {
        // Tree URIs typically end with the folder name
        val path = treeUri.lastPathSegment ?: return "Unknown"
        // Format: "primary:Music" or "xxxx-xxxx:Download/Music"
        return path.substringAfterLast(":").substringAfterLast("/")
    }

    /**
     * Extracts a display path from the tree URI per SSOT 4.2
     */
    private fun extractDisplayPath(treeUri: Uri): String {
        val path = treeUri.lastPathSegment ?: return "Storage"
        val parts = path.split(":")

        return when {
            parts.size >= 2 -> {
                val volumeType = if (parts[0] == "primary") "Internal Storage" else "External Storage"
                val folderPath = parts[1].replace("/", " / ")
                "$volumeType / $folderPath"
            }
            else -> "Storage / ${path.substringAfterLast("/")}"
        }
    }
}
