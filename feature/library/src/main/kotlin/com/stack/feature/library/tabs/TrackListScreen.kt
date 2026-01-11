package com.stack.feature.library.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stack.core.ui.components.TrackRow
import com.stack.domain.model.Track

/**
 * Track list screen - Displays all tracks in a vertical list.
 *
 * PERFORMANCE CRITICAL:
 * - Uses LazyColumn with stable keys (track.id)
 * - Artwork loaded via Coil with memory/disk caching
 * - Each item is a separate composable for recomposition optimization
 *
 * SSOT Reference: Section 7.3
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackListScreen(
    tracks: List<Track>,
    isLoading: Boolean,
    isRefreshing: Boolean,
    onTrackClick: (Track) -> Unit,
    onRefresh: () -> Unit
) {
    if (isLoading && tracks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(
                items = tracks,
                key = { track -> track.id }  // CRITICAL: Stable key for performance
            ) { track ->
                TrackRow(
                    title = track.displayTitle,
                    artist = track.displayArtist,
                    albumArtUri = track.albumArtUri,
                    durationMs = track.duration,
                    albumName = track.displayAlbum,
                    onClick = { onTrackClick(track) }
                )
            }
        }
    }
}
