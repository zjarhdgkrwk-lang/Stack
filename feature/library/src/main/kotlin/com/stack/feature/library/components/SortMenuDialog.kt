package com.stack.feature.library.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stack.domain.model.TrackSortOrder
import com.stack.domain.model.AlbumSortOrder
import com.stack.feature.library.LibraryContract.LibraryTab

/**
 * Sort option selection dialog.
 */
@Composable
fun SortMenuDialog(
    currentTab: LibraryTab,
    trackSortOrder: TrackSortOrder,
    albumSortOrder: AlbumSortOrder,
    onTrackSortOrderChange: (TrackSortOrder) -> Unit,
    onAlbumSortOrderChange: (AlbumSortOrder) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort by") },
        text = {
            when (currentTab) {
                LibraryTab.TRACKS -> {
                    TrackSortOptions(
                        currentOrder = trackSortOrder,
                        onOrderSelected = onTrackSortOrderChange
                    )
                }
                LibraryTab.ALBUMS -> {
                    AlbumSortOptions(
                        currentOrder = albumSortOrder,
                        onOrderSelected = onAlbumSortOrderChange
                    )
                }
                LibraryTab.ARTISTS -> {
                    // Artists are always sorted alphabetically
                    Text("Artists are sorted alphabetically")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun TrackSortOptions(
    currentOrder: TrackSortOrder,
    onOrderSelected: (TrackSortOrder) -> Unit
) {
    val options = listOf(
        TrackSortOrder.TITLE_ASC to "Title (A-Z)",
        TrackSortOrder.TITLE_DESC to "Title (Z-A)",
        TrackSortOrder.ARTIST_ASC to "Artist (A-Z)",
        TrackSortOrder.ARTIST_DESC to "Artist (Z-A)",
        TrackSortOrder.ALBUM_ASC to "Album (A-Z)",
        TrackSortOrder.ALBUM_DESC to "Album (Z-A)",
        TrackSortOrder.DATE_ADDED_DESC to "Recently Added",
        TrackSortOrder.DATE_ADDED_ASC to "Oldest First",
        TrackSortOrder.DURATION_DESC to "Duration (Longest)",
        TrackSortOrder.DURATION_ASC to "Duration (Shortest)"
    )

    LazyColumn {
        items(options) { (order, label) ->
            SortOptionItem(
                label = label,
                isSelected = currentOrder == order,
                onClick = { onOrderSelected(order) }
            )
        }
    }
}

@Composable
private fun AlbumSortOptions(
    currentOrder: AlbumSortOrder,
    onOrderSelected: (AlbumSortOrder) -> Unit
) {
    val options = listOf(
        AlbumSortOrder.NAME_ASC to "Name (A-Z)",
        AlbumSortOrder.NAME_DESC to "Name (Z-A)",
        AlbumSortOrder.ARTIST_ASC to "Artist (A-Z)",
        AlbumSortOrder.ARTIST_DESC to "Artist (Z-A)",
        AlbumSortOrder.YEAR_DESC to "Year (Newest)",
        AlbumSortOrder.YEAR_ASC to "Year (Oldest)",
        AlbumSortOrder.TRACK_COUNT_DESC to "Track Count (Most)",
        AlbumSortOrder.TRACK_COUNT_ASC to "Track Count (Least)"
    )

    LazyColumn {
        items(options) { (order, label) ->
            SortOptionItem(
                label = label,
                isSelected = currentOrder == order,
                onClick = { onOrderSelected(order) }
            )
        }
    }
}

@Composable
private fun SortOptionItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
