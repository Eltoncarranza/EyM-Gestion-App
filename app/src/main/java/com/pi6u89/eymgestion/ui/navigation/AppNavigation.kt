package com.pi6u89.eymgestion.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pi6u89.eymgestion.ui.main.MainScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Ahora la app arranca directamente en el menú principal (MainScreen)
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen()
        }
    }
}