package com.window.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.window.app.ui.dashboard.DashboardScreen
import com.window.app.ui.settings.SettingsScreen

@Composable
fun WindowNavHost(navController: NavHostController) {
    NavHost(
        navController  = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateUp = { navController.popBackStack() }
            )
        }
    }
}

