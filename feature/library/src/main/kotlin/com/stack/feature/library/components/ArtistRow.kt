package com.stack.feature.library.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stack.core.ui.components.ArtworkImage
import com.stack.feature.library.LibraryContract.ArtistUiModel

/**
 * Reusable artist list item component.
 * Displays: Circular artwork, Artist name, Album/Track count.
 */
@Composable
fun ArtistRow(
    artist: ArtistUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        leadingContent = {
            ArtworkImage(
                uri = artist.artworkUri,
                contentDescription = artist.name,
                size = 56.dp,
                modifier = Modifier.clip(CircleShape)  // Circular for artists
            )
        },
        headlineContent = {
            Text(
                text = artist.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = "${artist.albumCount} albums · ${artist.trackCount} tracks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}
