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
import com.stack.feature.nowplaying.NowPlayingScreen
import com.stack.feature.search.SearchScreen
import com.stack.feature.settings.SettingsScreen

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
