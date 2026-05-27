package com.pi6u89.eymgestion.ui.reportes

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pi6u89.eymgestion.data.VentasRepository
import com.pi6u89.eymgestion.domain.Venta
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReportesScreen() {
    val ventasRepository = remember { VentasRepository() }
    val coroutineScope = rememberCoroutineScope()

    var listaVentas by remember { mutableStateOf<List<Venta>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    // --- NUEVO: ESTADO PARA EL CONTROL DEL DÍA SELECCIONADO ---
    var fechaSeleccionada by remember { mutableStateOf(LocalDate.now()) }
    val formateador = remember { DateTimeFormatter.ofPattern("dd / MM / yyyy") }

    // --- ESTADOS PARA EL PANEL DESLIZABLE ---
    val sheetState = rememberModalBottomSheetState()
    var mostrarBottomSheet by remember { mutableStateOf(false) }
    var categoriaSeleccionada by remember { mutableStateOf("") }

    // El LaunchedEffect ahora "escucha" a fechaSeleccionada. Si cambia el día, vuelve a descargar de Supabase.
    LaunchedEffect(fechaSeleccionada) {
        coroutineScope.launch {
            cargando = true
            listaVentas = ventasRepository.obtenerVentasPorFecha(fechaSeleccionada.toString())
            cargando = false
        }
    }

    // --- CÁLCULO DE TOTALES EN TIEMPO REAL ---
    val ventasEfectivo = listaVentas.filter { it.metodoPago == "Efectivo" && it.estadoPago == "PAGADO" }
    val ventasYape = listaVentas.filter { it.metodoPago == "Yape" && it.estadoPago == "PAGADO" }
    val ventasPlin = listaVentas.filter { it.metodoPago == "Plin" && it.estadoPago == "PAGADO" }
    val ventasFiado = listaVentas.filter { it.estadoPago == "FIADO" }

    val totalEfectivo = ventasEfectivo.sumOf { it.montoTotal }
    val totalYape = ventasYape.sumOf { it.montoTotal }
    val totalPlin = ventasPlin.sumOf { it.montoTotal }
    val totalFiado = ventasFiado.sumOf { it.montoTotal }
    val totalCaja = totalEfectivo + totalYape + totalPlin

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Caja y Reportes", fontSize = 28.sp, fontWeight = FontWeight.Bold)

            // --- BARRA NAVEGADORA DE DÍAS (<- Día ->) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { fechaSeleccionada = fechaSeleccionada.minusDays(1) }) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Día Anterior")
                    }

                    Text(
                        text = if (fechaSeleccionada == LocalDate.now()) "Hoy (${fechaSeleccionada.format(formateador)})"
                        else fechaSeleccionada.format(formateador),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    IconButton(onClick = { fechaSeleccionada = fechaSeleccionada.plusDays(1) }) {
                        Icon(Icons.Default.ArrowForwardIos, contentDescription = "Día Siguiente")
                    }
                }
            }

            if (cargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // --- CUADRO TOTAL ---
                CuadroReporte(
                    titulo = "TOTAL EN CAJA",
                    monto = totalCaja,
                    colorFondo = MaterialTheme.colorScheme.primaryContainer,
                    colorTexto = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                ) {
                    categoriaSeleccionada = "Total General"
                    mostrarBottomSheet = true
                }

                // --- FILA 1: EFECTIVO Y YAPE ---
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CuadroReporte(
                        titulo = "Efectivo",
                        monto = totalEfectivo,
                        colorFondo = Color(0xFFE8F5E9),
                        colorTexto = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f).height(90.dp)
                    ) {
                        categoriaSeleccionada = "Efectivo"
                        mostrarBottomSheet = true
                    }

                    CuadroReporte(
                        titulo = "Yape",
                        monto = totalYape,
                        colorFondo = Color(0xFFF3E5F5),
                        colorTexto = Color(0xFF6A1B9A),
                        modifier = Modifier.weight(1f).height(90.dp)
                    ) {
                        categoriaSeleccionada = "Yape"
                        mostrarBottomSheet = true
                    }
                }

                // --- FILA 2: PLIN Y FIADOS ---
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CuadroReporte(
                        titulo = "Plin",
                        monto = totalPlin,
                        colorFondo = Color(0xFFE0F7FA),
                        colorTexto = Color(0xFF00838F),
                        modifier = Modifier.weight(1f).height(90.dp)
                    ) {
                        categoriaSeleccionada = "Plin"
                        mostrarBottomSheet = true
                    }

                    CuadroReporte(
                        titulo = "Fiados",
                        monto = totalFiado,
                        colorFondo = Color(0xFFE0F7FA), // Puedes usar el color de tu preferencia
                        colorTexto = Color(0xFFC62828),
                        modifier = Modifier.weight(1f).height(90.dp)
                    ) {
                        categoriaSeleccionada = "Fiados"
                        mostrarBottomSheet = true
                    }
                }
            }
        }

        // --- PANEL DESLIZABLE COMPARTIDO POR CONTEXTO ---
        if (mostrarBottomSheet) {
            val ventasAMostrar = when (categoriaSeleccionada) {
                "Efectivo" -> ventasEfectivo
                "Yape" -> ventasYape
                "Plin" -> ventasPlin
                "Fiados" -> ventasFiado
                else -> listaVentas // Muestra todo si es "Total General"
            }

            ModalBottomSheet(
                onDismissRequest = { mostrarBottomSheet = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "Pedidos en $categoriaSeleccionada",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Día: ${fechaSeleccionada.format(formateador)}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    if (ventasAMostrar.isEmpty()) {
                        Text(
                            text = "No hay platos vendidos bajo este método hoy.",
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxHeight(0.6f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(ventasAMostrar) { venta ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = venta.detalles.takeIf { it.isNotBlank() } ?: "Sin detalles de platos",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 16.sp
                                            )
                                            Text(
                                                text = "Monto: S/. ${venta.montoTotal}",
                                                color = MaterialTheme.colorScheme.secondary,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { mostrarBottomSheet = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}

@Composable
fun CuadroReporte(
    titulo: String,
    monto: Double,
    colorFondo: Color,
    colorTexto: Color,
    modifier: Modifier = Modifier,
    alPresionar: () -> Unit
) {
    Card(
        modifier = modifier.clickable { alPresionar() },
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = titulo, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colorTexto)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "S/. $monto", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colorTexto)
        }
    }
}