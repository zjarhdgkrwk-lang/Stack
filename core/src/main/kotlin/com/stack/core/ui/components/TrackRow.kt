package com.stack.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stack.domain.model.Track

/**
 * Reusable track list item component.
 * Displays: Artwork (48dp), Title, Artist, Duration.
 *
 * IMPORTANT: This component is designed for reuse across:
 * - TrackListScreen
 * - AlbumDetailScreen (future)
 * - PlaylistDetailScreen (future)
 * - SearchResultsScreen (future)
 */
@Composable
fun TrackRow(
    track: Track,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showTrackNumber: Boolean = false
) {
    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        leadingContent = {
            ArtworkImage(
                uri = track.albumArtUri,
                contentDescription = track.displayAlbum,
                size = 48.dp
            )
        },
        headlineContent = {
            Text(
                text = track.displayTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = track.displayArtist,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Text(
                text = formatDuration(track.duration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

/**
 * Format duration in mm:ss or h:mm:ss format.
 */
private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}
