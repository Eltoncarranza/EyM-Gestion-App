package com.pi6u89.eymgestion.ui.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pi6u89.eymgestion.ui.compras.ComprasScreen
import com.pi6u89.eymgestion.ui.compras.HistorialComprasScreen
import com.pi6u89.eymgestion.ui.fiados.FiadosScreen
import com.pi6u89.eymgestion.ui.reportes.ReportesScreen
import com.pi6u89.eymgestion.ui.venta.VentaScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route
    val rutasConBottomBar = listOf("venta", "fiados", "compras", "reportes")

    Scaffold(
        bottomBar = {
            if (rutaActual in rutasConBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Venta") },
                        label = { Text("Venta") },
                        selected = rutaActual == "venta",
                        onClick = {
                            navController.navigate("venta") {
                                popUpTo("venta") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Fiados") },
                        label = { Text("Fiados") },
                        selected = rutaActual == "fiados",
                        onClick = {
                            navController.navigate("fiados") {
                                popUpTo("venta") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Compras") },
                        label = { Text("Compras") },
                        selected = rutaActual == "compras",
                        onClick = {
                            navController.navigate("compras") {
                                popUpTo("venta") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Reportes") },
                        label = { Text("Reportes") },
                        selected = rutaActual == "reportes",
                        onClick = {
                            navController.navigate("reportes") {
                                popUpTo("venta") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "venta",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("venta") { VentaScreen() }
            composable("fiados") { FiadosScreen() }
            composable("compras") { ComprasScreen(navController) }
            composable("reportes") { ReportesScreen() }
            composable("historial_compras") { HistorialComprasScreen(navController) }
        }
    }
}