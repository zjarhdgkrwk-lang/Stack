package com.stack.feature.library.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.stack.feature.library.LibraryContract.LibraryTab

/**
 * Top app bar for Library screen.
 * Contains title, search button, sort button, and settings button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryTopBar(
    currentTab: LibraryTab,
    onSearchClick: () -> Unit,
    onSortClick: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    val title = when (currentTab) {
        LibraryTab.TRACKS -> "Tracks"
        LibraryTab.ALBUMS -> "Albums"
        LibraryTab.ARTISTS -> "Artists"
    }

    TopAppBar(
        title = { Text(text = title) },
        actions = {
            // Search button
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            }
            // Sort button
            IconButton(onClick = onSortClick) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = "Sort"
                )
            }
            // Settings button (Phase 4.4)
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            }
        }
    )
}
