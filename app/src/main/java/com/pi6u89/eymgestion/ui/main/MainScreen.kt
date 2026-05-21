package com.pi6u89.eymgestion.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pi6u89.eymgestion.ui.fiados.FiadosScreen
import com.pi6u89.eymgestion.ui.reportes.ReportesScreen
import com.pi6u89.eymgestion.ui.compras.ComprasScreen
import com.pi6u89.eymgestion.ui.venta.VentaScreen

@Composable
fun MainScreen() {
    // Controlador de navegación interno para las pestañas de abajo
    val bottomNavController = rememberNavController()
    // Variable para saber en qué pestaña estamos
    var rutaActual by remember { mutableStateOf("venta") }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Venta") },
                    label = { Text("Venta") },
                    selected = rutaActual == "venta",
                    onClick = {
                        rutaActual = "venta"
                        bottomNavController.navigate("venta")
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Fiados") },
                    label = { Text("Fiados") },
                    selected = rutaActual == "fiados",
                    onClick = {
                        rutaActual = "fiados"
                        bottomNavController.navigate("fiados")
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Warning, contentDescription = "Vajilla") },
                    label = { Text("Vajilla") },
                    selected = rutaActual == "vajilla",
                    onClick = {
                        rutaActual = "vajilla"
                        bottomNavController.navigate("vajilla")
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Reportes") },
                    label = { Text("Reportes") },
                    selected = rutaActual == "reportes",
                    onClick = {
                        rutaActual = "reportes"
                        bottomNavController.navigate("reportes")
                    }
                )
            }
        }
    ) { paddingValues ->
        // Aquí se dibuja la pantalla seleccionada
        NavHost(
            navController = bottomNavController,
            startDestination = "venta",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("venta") { VentaScreen() }
            composable("fiados") { FiadosScreen() }
            composable("Compras") { ComprasScreen() }
            composable("reportes") { ReportesScreen() }
        }
    }
}