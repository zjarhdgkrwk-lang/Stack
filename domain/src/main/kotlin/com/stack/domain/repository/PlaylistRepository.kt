package com.stack.domain.repository

import com.stack.domain.model.Playlist
import com.stack.domain.model.PlaylistSortOrder
import com.stack.domain.model.PlaylistTrack
import com.stack.domain.model.Track
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Playlist operations.
 */
interface PlaylistRepository {

    /**
     * Observe all playlists.
     */
    fun observePlaylists(sortOrder: PlaylistSortOrder = PlaylistSortOrder.UPDATED_DESC): Flow<List<Playlist>>

    /**
     * Observe a specific playlist.
     */
    fun observePlaylist(playlistId: Long): Flow<Playlist?>

    /**
     * Observe tracks in a playlist (ordered).
     */
    fun observePlaylistTracks(playlistId: Long): Flow<List<Track>>

    /**
     * Get playlist by ID.
     */
    suspend fun getPlaylistById(id: Long): Playlist?

    /**
     * Create a new playlist.
     */
    suspend fun createPlaylist(name: String, description: String? = null): Long

    /**
     * Update playlist metadata.
     */
    suspend fun updatePlaylist(playlist: Playlist)

    /**
     * Delete playlist.
     */
    suspend fun deletePlaylist(playlistId: Long)

    /**
     * Add track to playlist.
     */
    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long)

    /**
     * Add multiple tracks to playlist.
     */
    suspend fun addTracksToPlaylist(playlistId: Long, trackIds: List<Long>)

    /**
     * Remove track from playlist.
     */
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long)

    /**
     * Reorder tracks in playlist.
     */
    suspend fun reorderPlaylistTracks(playlistId: Long, fromPosition: Int, toPosition: Int)

    /**
     * Get playlist count.
     */
    suspend fun getPlaylistCount(): Int
}
