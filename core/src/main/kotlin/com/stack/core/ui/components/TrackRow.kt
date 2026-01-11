package com.stack.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Reusable track list item component.
 * Displays: Artwork (48dp), Title, Artist, Duration.
 *
 * IMPORTANT: This component uses pure parameters (no domain dependency).
 * It is designed for reuse across:
 * - TrackListScreen
 * - AlbumDetailScreen
 * - PlaylistDetailScreen
 * - SearchResultsScreen
 */
@Composable
fun TrackRow(
    title: String,
    artist: String,
    albumArtUri: String?,
    durationMs: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    albumName: String? = null,
    trackNumber: Int? = null,
    isPlaying: Boolean = false
) {
    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        leadingContent = {
            ArtworkImage(
                uri = albumArtUri,
                contentDescription = albumName,
                size = 48.dp
            )
        },
        headlineContent = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            Text(
                text = artist,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Text(
                text = formatDuration(durationMs),
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
