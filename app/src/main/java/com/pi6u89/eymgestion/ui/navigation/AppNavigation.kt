package com.pi6u89.eymgestion.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pi6u89.eymgestion.ui.apertura.AperturaScreen
import com.pi6u89.eymgestion.ui.venta.VentaScreen

@Composable
fun AppNavigation() {
    // Este es el controlador que mueve las pantallas
    val navController = rememberNavController()

    // NavHost es el contenedor donde se dibujan las pantallas
    NavHost(navController = navController, startDestination = "apertura") {

        // Ruta 1: Pantalla de Apertura
        composable("apertura") {
            // Le pasamos el navController para que pueda viajar a otra pantalla
            AperturaScreen(navController = navController)
        }

        // Ruta 2: Pantalla Principal de Venta
        composable("venta") {
            VentaScreen()
        }
    }
}