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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pi6u89.eymgestion.data.CajaManager
import com.pi6u89.eymgestion.data.VentasRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReportesScreen() {
    val contexto = LocalContext.current
    val cajaManager = remember { CajaManager(contexto) }
    val ventasRepository = remember { VentasRepository() }
    val coroutineScope = rememberCoroutineScope()

    val cajaAbierta by cajaManager.cajaAbiertaFlow.collectAsState(initial = false)

    var fechaActual by remember { mutableStateOf(LocalDate.now()) }
    var reporte by remember { mutableStateOf<VentasRepository.ReporteCierre?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var mostrarConfirmacionCierre by remember { mutableStateOf(false) }

    val cargarDatos = {
        coroutineScope.launch {
            cargando = true
            val fechaStr = fechaActual.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            reporte = ventasRepository.obtenerReporteCierreDeCaja(fechaStr)
            cargando = false
        }
    }

    LaunchedEffect(fechaActual) {
        cargarDatos()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Reportes Financieros", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { fechaActual = fechaActual.minusDays(1) }) {
                Icon(Icons.Default.ChevronLeft, "Día anterior")
            }
            Text(fechaActual.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = { fechaActual = fechaActual.plusDays(1) }) {
                Icon(Icons.Default.ChevronRight, "Día siguiente")
            }
        }

        if (cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {

            Column(modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f)) {
                reporte?.let {
                    // Tarjeta Principal (Total)
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TOTAL RECAUDADO", style = MaterialTheme.typography.labelLarge)
                            Text("S/. ${it.totalGeneral}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        TarjetaDato("Efectivo", it.ventasEfectivo, Icons.Default.Payments, Modifier.weight(1f))
                        TarjetaDato("Yape", it.ventasYape, Icons.Default.Smartphone, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        TarjetaDato("Plin", it.ventasPlin, Icons.Default.QrCode, Modifier.weight(1f))
                        TarjetaDato("Fiado", it.ventasFiadas, Icons.Default.Warning, Modifier.weight(1f))
                    }
                }
            }

            if (cajaAbierta && fechaActual == LocalDate.now()) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { mostrarConfirmacionCierre = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Lock, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CERRAR JORNADA", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (mostrarConfirmacionCierre) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionCierre = false },
            title = { Text("¿Cerrar Caja?") },
            text = { Text("Se bloquearán nuevas ventas hasta mañana.") },
            confirmButton = {
                Button(onClick = { coroutineScope.launch { cajaManager.cerrarCaja(); mostrarConfirmacionCierre = false } }) { Text("Sí, Cerrar") }
            },
            dismissButton = { TextButton(onClick = { mostrarConfirmacionCierre = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
fun TarjetaDato(titulo: String, monto: Double, icono: ImageVector, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(titulo, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text("S/. $monto", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}