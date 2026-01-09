package com.stack.feature.library

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.stack.feature.library.LibraryContract.*
import com.stack.feature.library.tabs.*
import com.stack.feature.library.components.*

/**
 * Library screen - Main container with tabs and navigation.
 *
 * Contains: TopAppBar, Content Area, Bottom Navigation Bar
 *
 * SSOT Reference: Section 7.2
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onSearchClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Handle one-time effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is Effect.ShowError -> {
                    val message = when (effect.error) {
                        is LibraryError.LoadFailed -> "Failed to load library"
                        is LibraryError.RefreshFailed -> "Failed to refresh library"
                        is LibraryError.Unknown -> effect.error.message
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            LibraryTopBar(
                currentTab = state.currentTab,
                onSearchClick = onSearchClick,
                onSortClick = { viewModel.onIntent(Intent.ShowSortMenu) },
                onSettingsClick = onSettingsClick
            )
        },
        bottomBar = {
            LibraryBottomBar(
                currentTab = state.currentTab,
                onTabSelected = { tab -> viewModel.onIntent(Intent.ChangeTab(tab)) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state.currentTab) {
                LibraryTab.TRACKS -> {
                    TrackListScreen(
                        tracks = state.tracks,
                        isLoading = state.isLoading,
                        isRefreshing = state.isRefreshing,
                        onTrackClick = { track ->
                            viewModel.onIntent(Intent.OnTrackClick(track))
                        },
                        onRefresh = { viewModel.onIntent(Intent.RefreshLibrary) }
                    )
                }
                LibraryTab.ALBUMS -> {
                    AlbumGridScreen(
                        albums = state.albums,
                        isLoading = state.isLoading,
                        isRefreshing = state.isRefreshing,
                        viewMode = state.albumViewMode,
                        onAlbumClick = { album ->
                            viewModel.onIntent(Intent.OnAlbumClick(album))
                        },
                        onRefresh = { viewModel.onIntent(Intent.RefreshLibrary) }
                    )
                }
                LibraryTab.ARTISTS -> {
                    ArtistListScreen(
                        artists = state.artists,
                        isLoading = state.isLoading,
                        isRefreshing = state.isRefreshing,
                        onArtistClick = { artist ->
                            viewModel.onIntent(Intent.OnArtistClick(artist))
                        },
                        onRefresh = { viewModel.onIntent(Intent.RefreshLibrary) }
                    )
                }
            }

            // Empty state
            if (!state.isLoading && state.isEmpty) {
                EmptyLibraryState(
                    currentTab = state.currentTab,
                    onRefresh = { viewModel.onIntent(Intent.RefreshLibrary) }
                )
            }
        }
    }

    // Sort menu dialog
    if (state.showSortMenu) {
        SortMenuDialog(
            currentTab = state.currentTab,
            trackSortOrder = state.trackSortOrder,
            albumSortOrder = state.albumSortOrder,
            onTrackSortOrderChange = { order ->
                viewModel.onIntent(Intent.ChangeTrackSortOrder(order))
            },
            onAlbumSortOrderChange = { order ->
                viewModel.onIntent(Intent.ChangeAlbumSortOrder(order))
            },
            onDismiss = { viewModel.onIntent(Intent.HideSortMenu) }
        )
    }
}
