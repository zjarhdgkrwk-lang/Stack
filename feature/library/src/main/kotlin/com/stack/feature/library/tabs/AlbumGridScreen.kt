package com.stack.feature.library.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stack.feature.library.LibraryContract.AlbumUiModel
import com.stack.feature.library.LibraryContract.ViewMode
import com.stack.feature.library.components.AlbumCard

/**
 * Album grid screen - Displays albums in a grid layout.
 *
 * Uses adaptive columns: 2 for compact, 3 for medium, 4 for expanded.
 *
 * SSOT Reference: Section 7.4
 */
@Composable
fun AlbumGridScreen(
    albums: List<AlbumUiModel>,
    isLoading: Boolean,
    isRefreshing: Boolean,
    viewMode: ViewMode,
    onAlbumClick: (AlbumUiModel) -> Unit,
    onRefresh: () -> Unit
) {
    if (isLoading && albums.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = albums,
                key = { album -> album.albumId }  // CRITICAL: Stable key
            ) { album ->
                AlbumCard(
                    album = album,
                    onClick = { onAlbumClick(album) }
                )
            }
        }
    }
}
