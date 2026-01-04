package com.stack.feature.library.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stack.feature.library.LibraryContract.ArtistUiModel
import com.stack.feature.library.components.ArtistRow

/**
 * Artist list screen - Displays all artists in a vertical list.
 *
 * SSOT Reference: Section 7.5
 */
@Composable
fun ArtistListScreen(
    artists: List<ArtistUiModel>,
    isLoading: Boolean,
    isRefreshing: Boolean,
    onArtistClick: (ArtistUiModel) -> Unit,
    onRefresh: () -> Unit
) {
    if (isLoading && artists.isEmpty()) {
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
                items = artists,
                key = { artist -> artist.artistId }  // CRITICAL: Stable key
            ) { artist ->
                ArtistRow(
                    artist = artist,
                    onClick = { onArtistClick(artist) }
                )
            }
        }
    }
}
