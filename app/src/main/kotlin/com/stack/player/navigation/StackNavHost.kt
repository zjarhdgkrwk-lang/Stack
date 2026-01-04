package com.stack.player.navigation

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

@Composable
fun StackNavHost(
    isGateReady: Boolean,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val startDestination = if (isGateReady) {
        NavRoutes.Library.route
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
                navController.navigate(NavRoutes.Library.route) {
                    popUpTo(NavRoutes.Gate.route) { inclusive = true }
                }
            }
        )

        composable(NavRoutes.Library.route) {
            // TODO: Replace with LibraryScreen
            PlaceholderScreen("Library")
        }
        composable(NavRoutes.Player.route) {
            // TODO: Replace with PlayerScreen
            PlaceholderScreen("Player")
        }
        composable(NavRoutes.Search.route) {
            // TODO: Replace with SearchScreen
            PlaceholderScreen("Search")
        }
        composable(NavRoutes.Tags.route) {
            // TODO: Replace with TagsScreen
            PlaceholderScreen("Tags")
        }
        composable(NavRoutes.Playlists.route) {
            // TODO: Replace with PlaylistsScreen
            PlaceholderScreen("Playlists")
        }
        composable(NavRoutes.Settings.route) {
            // TODO: Replace with SettingsScreen
            PlaceholderScreen("Settings")
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
