package com.stack.player.navigation

sealed class NavRoutes(val route: String) {
    data object Gate : NavRoutes("gate")
    data object Library : NavRoutes("library")
    data object Player : NavRoutes("player")
    data object NowPlaying : NavRoutes("now_playing")
    data object Search : NavRoutes("search")
    data object Tags : NavRoutes("tags")
    data object Playlists : NavRoutes("playlists")
    data object Settings : NavRoutes("settings")
}
