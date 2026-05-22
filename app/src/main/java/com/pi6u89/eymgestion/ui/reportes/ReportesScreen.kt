package com.pi6u89.eymgestion.ui.reportes

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pi6u89.eymgestion.data.CajaManager
import com.pi6u89.eymgestion.data.VentasRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReportesScreen() {
    val contexto = LocalContext.current
    val cajaManager = remember { CajaManager(contexto) }
    val ventasRepository = remember { VentasRepository() }
    val coroutineScope = rememberCoroutineScope()

    val cajaAbierta by cajaManager.cajaAbiertaFlow.collectAsState(initial = false)
    var fechaActual by remember { mutableStateOf(java.time.LocalDate.now()) }
    var reporte by remember { mutableStateOf<VentasRepository.ReporteCierre?>(null) }
    var cantidadVentasTotal by remember { mutableStateOf(0) } // Para debug
    var cargando by remember { mutableStateOf(true) }
    var mostrarConfirmacionCierre by remember { mutableStateOf(false) }

    val cargarDatos = {
        coroutineScope.launch {
            cargando = true
            val fechaStr = fechaActual.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val todasLasVentas = ventasRepository.obtenerVentasPorFecha(fechaStr)
            cantidadVentasTotal = todasLasVentas.size
            reporte = ventasRepository.obtenerReporteCierreDeCaja(fechaStr)
            cargando = false
        }
    }

    LaunchedEffect(fechaActual) { cargarDatos() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        // Navegador de Fechas
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { fechaActual = fechaActual.minusDays(1) }) { Icon(Icons.Default.ArrowBack, null) }
            Text(fechaActual.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = { fechaActual = fechaActual.plusDays(1) }) { Icon(Icons.Default.ArrowForward, null) }
        }

        if (cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            // TARJETA DE DEBUG (Para ver si llegan ventas aunque sea a 0)
            Text("Ventas encontradas en DB: $cantidadVentasTotal", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)

            reporte?.let {
                // Tarjeta Total
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TOTAL RECAUDADO", fontWeight = FontWeight.SemiBold)
                        Text("S/. ${it.totalGeneral}", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                // Desglose
                // DESGLOSE CANALES
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // He añadido el icono correspondiente a cada tarjeta
                    TarjetaDato("Efectivo", it.ventasEfectivo, Icons.Default.Payments, Modifier.weight(1f))
                    TarjetaDato("Yape", it.ventasYape, Icons.Default.PhoneAndroid, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TarjetaDato("Plin", it.ventasPlin, Icons.Default.QrCode, Modifier.weight(1f))
                    TarjetaDato("Fiado", it.ventasFiadas, Icons.Default.Warning, Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // BOTÓN DE CIERRE
            if (cajaAbierta) {
                Button(
                    onClick = { mostrarConfirmacionCierre = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Lock, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CERRAR JORNADA", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (mostrarConfirmacionCierre) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionCierre = false },
            title = { Text("¿Cerrar Caja?") },
            text = { Text("Al cerrar la caja, la jornada terminará. ¿Continuar?") },
            confirmButton = {
                Button(onClick = { coroutineScope.launch { cajaManager.cerrarCaja(); mostrarConfirmacionCierre = false } }) { Text("Sí, Cerrar") }
            },
            dismissButton = { TextButton(onClick = { mostrarConfirmacionCierre = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
fun TarjetaDato(
    titulo: String,
    monto: Double,
    icono: androidx.compose.ui.graphics.vector.ImageVector, // El icono va aquí
    modifier: Modifier
) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(titulo, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text("S/. $monto", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}