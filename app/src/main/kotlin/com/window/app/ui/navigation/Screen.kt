package com.window.app.ui.navigation

/**
 * Sealed class defining all nav destinations in the Window app.
 */
sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Settings  : Screen("settings")
}

