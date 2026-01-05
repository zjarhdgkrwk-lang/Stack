package com.stack.feature.library.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.stack.feature.library.LibraryScreen

const val LIBRARY_ROUTE = "library"

fun NavController.navigateToLibrary() {
    navigate(LIBRARY_ROUTE) {
        popUpTo(0) { inclusive = true }  // Clear back stack
    }
}

fun NavGraphBuilder.libraryScreen(
    onSearchClick: () -> Unit
) {
    composable(route = LIBRARY_ROUTE) {
        LibraryScreen(
            onSearchClick = onSearchClick
        )
    }
}
