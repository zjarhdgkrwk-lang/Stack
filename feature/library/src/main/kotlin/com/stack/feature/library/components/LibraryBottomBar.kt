package com.stack.feature.library.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.stack.feature.library.LibraryContract.LibraryTab

/**
 * Bottom navigation bar for Library screen.
 * Three tabs: Tracks, Albums, Artists.
 *
 * SSOT Reference: Section 7.2
 */
@Composable
fun LibraryBottomBar(
    currentTab: LibraryTab,
    onTabSelected: (LibraryTab) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentTab == LibraryTab.TRACKS,
            onClick = { onTabSelected(LibraryTab.TRACKS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == LibraryTab.TRACKS)
                        Icons.Filled.MusicNote else Icons.Outlined.MusicNote,
                    contentDescription = "Tracks"
                )
            },
            label = { Text("Tracks") }
        )
        NavigationBarItem(
            selected = currentTab == LibraryTab.ALBUMS,
            onClick = { onTabSelected(LibraryTab.ALBUMS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == LibraryTab.ALBUMS)
                        Icons.Filled.Album else Icons.Outlined.Album,
                    contentDescription = "Albums"
                )
            },
            label = { Text("Albums") }
        )
        NavigationBarItem(
            selected = currentTab == LibraryTab.ARTISTS,
            onClick = { onTabSelected(LibraryTab.ARTISTS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == LibraryTab.ARTISTS)
                        Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Artists"
                )
            },
            label = { Text("Artists") }
        )
    }
}
