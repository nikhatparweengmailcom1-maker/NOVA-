package com.nova.assistant.presentation.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nova.assistant.presentation.ui.screens.*

sealed class NavRoute(val route: String) {
    object Chat      : NavRoute("chat")
    object Settings  : NavRoute("settings")
    object Reminders : NavRoute("reminders")
    object Todos     : NavRoute("todos")
    object QRScanner : NavRoute("qr_scanner")
}

@Composable
fun NovaNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavRoute.Chat.route,
        enterTransition  = {
            fadeIn(tween(250)) + slideInHorizontally(tween(250)) { it / 4 }
        },
        exitTransition   = {
            fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { -it / 4 }
        },
        popEnterTransition = {
            fadeIn(tween(250)) + slideInHorizontally(tween(250)) { -it / 4 }
        },
        popExitTransition  = {
            fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { it / 4 }
        }
    ) {
        composable(NavRoute.Chat.route) {
            ChatScreen(
                onNavigateToSettings  = { navController.navigate(NavRoute.Settings.route) },
                onNavigateToReminders = { navController.navigate(NavRoute.Reminders.route) },
                onNavigateToTodos     = { navController.navigate(NavRoute.Todos.route) },
                onNavigateToQRScanner = { navController.navigate(NavRoute.QRScanner.route) }
            )
        }
        composable(NavRoute.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(NavRoute.Reminders.route) {
            RemindersScreen(onBack = { navController.popBackStack() })
        }
        composable(NavRoute.Todos.route) {
            TodoScreen(onBack = { navController.popBackStack() })
        }
        composable(NavRoute.QRScanner.route) {
            QRScannerScreen(onBack = { navController.popBackStack() })
        }
    }
}
