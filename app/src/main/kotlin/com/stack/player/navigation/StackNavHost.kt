package com.stack.player.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stack.feature.gate.navigation.gateScreen
import com.stack.feature.library.navigation.LIBRARY_ROUTE
import com.stack.feature.library.navigation.libraryScreen
import com.stack.feature.library.album.AlbumDetailScreen
import com.stack.feature.library.artist.ArtistDetailScreen
import com.stack.feature.nowplaying.NowPlayingScreen
import com.stack.feature.search.SearchScreen
import com.stack.feature.settings.SettingsScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument

@Composable
fun StackNavHost(
    isGateReady: Boolean,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val startDestination = if (isGateReady) {
        LIBRARY_ROUTE
    } else {
        NavRoutes.Gate.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Gate screen - onboarding flow
        gateScreen(
            onGateReady = {
                navController.navigate(LIBRARY_ROUTE) {
                    popUpTo(NavRoutes.Gate.route) { inclusive = true }
                }
            }
        )

        // Library screen - main music browsing
        libraryScreen(
            onSearchClick = {
                navController.navigate(NavRoutes.Search.route)
            },
            onSettingsClick = {
                navController.navigate(NavRoutes.Settings.route)
            },
            onAlbumClick = { albumId ->
                navController.navigate(NavRoutes.AlbumDetail.createRoute(albumId))
            },
            onArtistClick = { artistId ->
                navController.navigate(NavRoutes.ArtistDetail.createRoute(artistId))
            }
        )

        // NowPlaying - Full-screen player (Phase 4.4)
        composable(
            route = NavRoutes.NowPlaying.route,
            enterTransition = { slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) },
            exitTransition = { slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) }
        ) {
            NowPlayingScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Search screen (Phase 4.4)
        composable(
            route = NavRoutes.Search.route,
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) }
        ) {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Settings screen (Phase 4.4)
        composable(
            route = NavRoutes.Settings.route,
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) }
        ) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Album Detail screen (Phase 5.1)
        composable(
            route = NavRoutes.AlbumDetail.route,
            arguments = listOf(navArgument("albumId") { type = NavType.StringType }),
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) }
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId") ?: "0"
            AlbumDetailScreen(
                albumId = albumId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToArtist = { artistId ->
                    navController.navigate(NavRoutes.ArtistDetail.createRoute(artistId))
                }
            )
        }

        // Artist Detail screen (Phase 5.1)
        composable(
            route = NavRoutes.ArtistDetail.route,
            arguments = listOf(navArgument("artistId") { type = NavType.StringType }),
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) }
        ) { backStackEntry ->
            val artistId = backStackEntry.arguments?.getString("artistId") ?: "0"
            ArtistDetailScreen(
                artistId = artistId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAlbum = { albumId ->
                    navController.navigate(NavRoutes.AlbumDetail.createRoute(albumId))
                }
            )
        }

        // Placeholder screens for future phases
        composable(NavRoutes.Player.route) {
            PlaceholderScreen("Player")
        }
        composable(NavRoutes.Tags.route) {
            PlaceholderScreen("Tags")
        }
        composable(NavRoutes.Playlists.route) {
            PlaceholderScreen("Playlists")
        }
    }
}

@Composable
private fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "$name Screen")
    }
}
