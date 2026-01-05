package com.stack.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

/**
 * Coil-powered artwork image with placeholder fallback.
 *
 * PERFORMANCE:
 * - Uses Coil's built-in memory and disk caching (256MB as per SSOT 9.4)
 * - Crossfade animation for smooth loading
 * - Placeholder shown during load and on error
 *
 * @param uri Content URI or file path for the artwork
 * @param contentDescription Accessibility description
 * @param size Size of the image (both width and height)
 * @param modifier Additional modifiers
 */
@Composable
fun ArtworkImage(
    uri: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val context = LocalContext.current

    if (uri.isNullOrBlank()) {
        // Placeholder when no artwork available
        ArtworkPlaceholder(
            contentDescription = contentDescription,
            modifier = modifier.size(size)
        )
    } else {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(uri)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(MaterialTheme.shapes.small),
            onError = {
                ArtworkPlaceholder(
                    contentDescription = contentDescription,
                    modifier = Modifier.size(size)
                )
            }
        )
    }
}

/**
 * Placeholder shown when artwork is unavailable.
 */
@Composable
private fun ArtworkPlaceholder(
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}
