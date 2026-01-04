package com.stack.data.scanner

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.stack.core.logging.Logger
import com.stack.core.util.DispatcherProvider
import com.stack.data.local.db.entity.TrackEntity
import com.stack.domain.model.TrackStatus
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreScanner @Inject constructor(
    private val contentResolver: ContentResolver,
    private val dispatchers: DispatcherProvider,
    private val logger: Logger
) {
    companion object {
        private const val TAG = "MediaStoreScanner"

        /**
         * MANDATORY PROJECTION - Do not modify without SSOT update
         * Covers all fields required by TrackEntity (SSOT 6.2)
         */
        private val PROJECTION = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM_ARTIST,
            MediaStore.Audio.Media.GENRE,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DISC_NUMBER,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.BITRATE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.RELATIVE_PATH,
            MediaStore.Audio.Media.DATA  // Deprecated but needed for fallback
        )

        // Minimum duration to filter out notification sounds (30 seconds)
        private const val MIN_DURATION_MS = 30_000L

        private val AUDIO_COLLECTION: Uri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
    }

    /**
     * Scans MediaStore for audio files.
     *
     * @param onProgress Callback for progress updates (0.0 to 1.0)
     * @return List of TrackEntity from MediaStore
     */
    suspend fun scanMediaStore(
        onProgress: (Float) -> Unit = {}
    ): List<TrackEntity> = withContext(dispatchers.io) {
        logger.d(TAG, "Starting MediaStore scan")
        val tracks = mutableListOf<TrackEntity>()
        val now = System.currentTimeMillis()

        val selection = "${MediaStore.Audio.Media.DURATION} >= ?"
        val selectionArgs = arrayOf(MIN_DURATION_MS.toString())
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        try {
            contentResolver.query(
                AUDIO_COLLECTION,
                PROJECTION,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val total = cursor.count
                if (total == 0) {
                    logger.d(TAG, "No audio files found in MediaStore")
                    return@withContext emptyList()
                }

                var processed = 0
                while (cursor.moveToNext()) {
                    try {
                        val entity = cursorToTrackEntity(cursor, now)
                        if (entity != null) {
                            tracks.add(entity)
                        }
                    } catch (e: Exception) {
                        logger.w(TAG, "Failed to parse track at position ${cursor.position}")
                    }

                    processed++
                    if (processed % 50 == 0) {
                        onProgress(processed.toFloat() / total)
                    }
                }

                onProgress(1f)
                logger.d(TAG, "MediaStore scan complete: ${tracks.size} tracks found")
            }
        } catch (e: SecurityException) {
            logger.e(TAG, "Permission denied for MediaStore access", e)
            throw e
        } catch (e: Exception) {
            logger.e(TAG, "MediaStore scan failed", e)
            throw e
        }

        tracks
    }

    /**
     * Converts a MediaStore cursor row to TrackEntity.
     * Returns null if critical fields are missing.
     */
    private fun cursorToTrackEntity(cursor: Cursor, scanTime: Long): TrackEntity? {
        val id = cursor.getLongOrNull(MediaStore.Audio.Media._ID) ?: return null
        val title = cursor.getStringOrNull(MediaStore.Audio.Media.TITLE)
            ?: cursor.getStringOrNull(MediaStore.Audio.Media.DISPLAY_NAME)
            ?: return null
        val duration = cursor.getLongOrNull(MediaStore.Audio.Media.DURATION) ?: return null
        val size = cursor.getLongOrNull(MediaStore.Audio.Media.SIZE) ?: 0L

        val contentUri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            id
        ).toString()

        // Build display path (SSOT 4.2 priority)
        val relativePath = cursor.getStringOrNull(MediaStore.Audio.Media.RELATIVE_PATH) ?: ""
        val fileName = cursor.getStringOrNull(MediaStore.Audio.Media.DISPLAY_NAME) ?: title
        val displayPath = buildDisplayPath(relativePath, fileName)

        // Extract folder path for grouping
        val folderPath = relativePath.trimEnd('/')

        // Album art URI
        val albumId = cursor.getLongOrNull(MediaStore.Audio.Media.ALBUM_ID)
        val albumArtUri = albumId?.let {
            ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"),
                it
            ).toString()
        }

        return TrackEntity(
            id = 0, // Auto-generated by Room
            contentUri = contentUri,
            title = title,
            artist = cursor.getStringOrNull(MediaStore.Audio.Media.ARTIST),
            albumArtist = cursor.getStringOrNull(MediaStore.Audio.Media.ALBUM_ARTIST),
            album = cursor.getStringOrNull(MediaStore.Audio.Media.ALBUM),
            albumId = albumId,
            artistId = cursor.getLongOrNull(MediaStore.Audio.Media.ARTIST_ID),
            genre = cursor.getStringOrNull(MediaStore.Audio.Media.GENRE),
            year = cursor.getIntOrNull(MediaStore.Audio.Media.YEAR),
            trackNumber = cursor.getIntOrNull(MediaStore.Audio.Media.TRACK),
            discNumber = cursor.getIntOrNull(MediaStore.Audio.Media.DISC_NUMBER),
            duration = duration,
            size = size,
            bitRate = cursor.getIntOrNull(MediaStore.Audio.Media.BITRATE),
            sampleRate = null, // Not available in MediaStore
            mimeType = cursor.getStringOrNull(MediaStore.Audio.Media.MIME_TYPE) ?: "audio/*",
            folderPath = folderPath,
            fileName = fileName,
            displayPath = displayPath,
            albumArtUri = albumArtUri,
            status = TrackStatus.ACTIVE,
            dateAdded = scanTime,
            dateModified = cursor.getLongOrNull(MediaStore.Audio.Media.DATE_MODIFIED)
                ?.let { it * 1000 } ?: scanTime, // Convert seconds to ms
            lastScanned = scanTime
        )
    }

    /**
     * Builds user-friendly display path per SSOT 4.2
     */
    private fun buildDisplayPath(relativePath: String, fileName: String): String {
        return if (relativePath.isNotBlank()) {
            "Storage / ${relativePath.replace("/", " / ")}$fileName"
        } else {
            "Storage / ... / $fileName"
        }
    }

    // Extension functions for null-safe cursor access
    private fun Cursor.getStringOrNull(column: String): String? {
        val index = getColumnIndex(column)
        return if (index >= 0 && !isNull(index)) getString(index) else null
    }

    private fun Cursor.getLongOrNull(column: String): Long? {
        val index = getColumnIndex(column)
        return if (index >= 0 && !isNull(index)) getLong(index) else null
    }

    private fun Cursor.getIntOrNull(column: String): Int? {
        val index = getColumnIndex(column)
        return if (index >= 0 && !isNull(index)) getInt(index) else null
    }
}
