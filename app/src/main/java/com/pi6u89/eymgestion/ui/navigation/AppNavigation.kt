package com.pi6u89.eymgestion.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pi6u89.eymgestion.ui.apertura.AperturaScreen
import com.pi6u89.eymgestion.ui.main.MainScreen // <- Importante

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "apertura") {

        composable("apertura") {
            AperturaScreen(navController = navController)
        }

        // Cuando la apertura termine, vamos al MainScreen (que tiene el menú inferior)
        composable("venta") {
            MainScreen()
        }
    }
}