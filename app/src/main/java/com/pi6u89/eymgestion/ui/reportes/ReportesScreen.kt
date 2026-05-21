package com.pi6u89.eymgestion.ui.reportes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pi6u89.eymgestion.data.CajaManager
import com.pi6u89.eymgestion.data.ComprasRepository
import com.pi6u89.eymgestion.data.VentasRepository
import kotlinx.coroutines.launch

@Composable
fun ReportesScreen() {
    val contexto = LocalContext.current
    val cajaManager = remember { CajaManager(contexto) }
    val ventasRepository = remember { VentasRepository() }
    val comprasRepository = remember { ComprasRepository() }
    val coroutineScope = rememberCoroutineScope()

    // Saber si la caja está abierta o ya se cerró
    val cajaAbierta by cajaManager.cajaAbiertaFlow.collectAsState(initial = false)

    var ingresosTotales by remember { mutableDoubleStateOf(0.0) }
    var egresosTotales by remember { mutableDoubleStateOf(0.0) }
    var cargando by remember { mutableStateOf(true) }
    var mostrarConfirmacionCierre by remember { mutableStateOf(false) }

    // Calcula los totales descargando los datos de hoy
    val cargarCuadreDeCaja = {
        coroutineScope.launch {
            cargando = true
            val ventasHoy = ventasRepository.obtenerVentasDeHoy()
            val gastosHoy = comprasRepository.obtenerGastosDeHoy()

            ingresosTotales = ventasHoy.sumOf { it.montoTotal }
            egresosTotales = gastosHoy.sumOf { it.costo }
            cargando = false
        }
    }

    // Se ejecuta al abrir la pestaña
    LaunchedEffect(Unit) {
        if (cajaAbierta) cargarCuadreDeCaja()
    }

    val gananciaNeta = ingresosTotales - egresosTotales

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Cuadre de Caja Diario", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        if (!cajaAbierta) {
            // Pantalla si la caja ya fue cerrada
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("La caja de hoy ya está cerrada.", fontSize = 18.sp, color = MaterialTheme.colorScheme.secondary)
            Text("Regresa mañana para una nueva jornada.", fontSize = 14.sp)
        } else if (cargando) {
            CircularProgressIndicator()
        } else {
            // --- TARJETA DE INGRESOS ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Ingresos por Ventas", fontSize = 16.sp)
                    Text(text = "S/. $ingresosTotales", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- TARJETA DE GASTOS ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Gastos en Insumos", fontSize = 16.sp)
                    Text(text = "S/. $egresosTotales", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- TARJETA DE GANANCIA NETA ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (gananciaNeta >= 0) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Efectivo Neto (Caja Final)", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "S/. $gananciaNeta",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (gananciaNeta >= 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // BOTÓN DE CIERRE DE JORNADA
            Button(
                onClick = { mostrarConfirmacionCierre = true },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CERRAR JORNADA", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // DIÁLOGO DE CONFIRMACIÓN
    if (mostrarConfirmacionCierre) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionCierre = false },
            title = { Text("¿Cerrar Caja?") },
            text = { Text("¿Estás seguro de que deseas cerrar la jornada? No podrás registrar más ventas hasta abrir la caja nuevamente mañana.") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            // Cambiamos el estado a cerrado en el almacenamiento interno del celular
                            cajaManager.cerrarCaja()
                            mostrarConfirmacionCierre = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sí, Cerrar Caja")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacionCierre = false }) { Text("Cancelar") }
            }
        )
    }
}