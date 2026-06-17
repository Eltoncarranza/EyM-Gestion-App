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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pi6u89.eymgestion.data.ComprasRepository
import com.pi6u89.eymgestion.data.VentasRepository
import com.pi6u89.eymgestion.domain.ItemCompra
import com.pi6u89.eymgestion.domain.Venta
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReportesScreen() {
    val ventasRepository = remember { VentasRepository() }
    val comprasRepository = remember { ComprasRepository() }
    val coroutineScope = rememberCoroutineScope()

    var todasLasVentas by remember { mutableStateOf<List<Venta>>(emptyList()) }
    var todasLasCompras by remember { mutableStateOf<List<ItemCompra>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }

    var tipoFiltro by remember { mutableIntStateOf(0) }
    var fechaBase by remember { mutableStateOf(LocalDate.now()) }

    val sheetState = rememberModalBottomSheetState()
    var mostrarBottomSheet by remember { mutableStateOf(false) }
    var categoriaSeleccionada by remember { mutableStateOf("") }

    // ─── Calcula el rango del período seleccionado ───────────────────────────
    val fechaInicio = when (tipoFiltro) {
        0 -> fechaBase
        1 -> fechaBase.with(DayOfWeek.MONDAY)
        2 -> fechaBase.withDayOfMonth(1)
        else -> fechaBase
    }
    val fechaFin = when (tipoFiltro) {
        0 -> fechaBase
        1 -> fechaBase.with(DayOfWeek.SUNDAY)
        2 -> fechaBase.withDayOfMonth(fechaBase.lengthOfMonth())
        else -> fechaBase
    }

    // Formato ISO para la query de Supabase
    val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE  // "yyyy-MM-dd"

    // ─── Recarga datos con el rango correcto desde el servidor ──────────────
    fun recargarDatos() {
        coroutineScope.launch {
            cargando = true
            errorMensaje = null
            val desde = fechaInicio.format(isoFormatter)
            val hasta = fechaFin.format(isoFormatter)
            // Trae solo las ventas del período — no descarga toda la tabla
            todasLasVentas = ventasRepository.obtenerVentasPorRango(desde, hasta)
            todasLasCompras = comprasRepository.obtenerListaCompras()
            if (todasLasVentas.isEmpty() && todasLasCompras.isEmpty()) {
                // No es un error, simplemente no hay datos en ese período
            }
            cargando = false
        }
    }

    // Recarga cuando cambia el rango (tipo o fecha base)
    LaunchedEffect(tipoFiltro, fechaInicio, fechaFin) {
        recargarDatos()
    }

    val formatoDia = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val formatoMes = DateTimeFormatter.ofPattern("MMMM yyyy")

    val textoRango = when (tipoFiltro) {
        0 -> if (fechaBase == LocalDate.now()) "Hoy (${fechaBase.format(formatoDia)})" else fechaBase.format(formatoDia)
        1 -> "${fechaInicio.format(formatoDia)} al ${fechaFin.format(formatoDia)}"
        2 -> fechaBase.format(formatoMes).replaceFirstChar { it.uppercase() }
        else -> ""
    }

    val ventasEfectivo = todasLasVentas.filter {
        (it.metodoPago ?: "").contains("Efectivo", true) &&
                (it.estadoPago ?: "").contains("PAGADO", true)
    }
    val ventasYape = todasLasVentas.filter {
        (it.metodoPago ?: "").contains("Yape", true) &&
                (it.estadoPago ?: "").contains("PAGADO", true)
    }
    val ventasPlin = todasLasVentas.filter {
        (it.metodoPago ?: "").contains("Plin", true) &&
                (it.estadoPago ?: "").contains("PAGADO", true)
    }
    val ventasFiado = todasLasVentas.filter {
        (it.estadoPago ?: "").contains("FIADO", true)
    }

    // Compras del período: filtramos en memoria por fecha
    val comprasFiltradas = todasLasCompras.filter { compra ->
        if (compra.comprado != true || compra.fecha.isNullOrBlank()) return@filter false
        try {
            // Soporta tanto "yyyy-MM-dd" como "yyyy-MM-ddTHH:mm:ss"
            val fechaCompra = LocalDate.parse(compra.fecha.take(10), isoFormatter)
            !fechaCompra.isBefore(fechaInicio) && !fechaCompra.isAfter(fechaFin)
        } catch (e: Exception) {
            false // Fecha inválida = no incluir (antes era true, que causaba datos erróneos)
        }
    }

    val totalEfectivo = ventasEfectivo.sumOf { it.montoTotal ?: 0.0 }
    val totalYape = ventasYape.sumOf { it.montoTotal ?: 0.0 }
    val totalPlin = ventasPlin.sumOf { it.montoTotal ?: 0.0 }
    val totalFiado = ventasFiado.sumOf { it.montoTotal ?: 0.0 }
    val totalIngresos = totalEfectivo + totalYape + totalPlin
    val totalGastos = comprasFiltradas.sumOf { it.costo ?: 0.0 }
    val gananciaNeta = totalIngresos - totalGastos

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ─── Encabezado ─────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Balance Financiero", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { recargarDatos() }) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Actualizar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // ─── Tabs Día / Semana / Mes ────────────────────────────────────
            TabRow(selectedTabIndex = tipoFiltro) {
                listOf("Día", "Semana", "Mes").forEachIndexed { index, title ->
                    Tab(
                        selected = tipoFiltro == index,
                        onClick = {
                            tipoFiltro = index
                            fechaBase = LocalDate.now()
                        },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // ─── Navegación de fechas ────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            fechaBase = when (tipoFiltro) {
                                0 -> fechaBase.minusDays(1)
                                1 -> fechaBase.minusWeeks(1)
                                else -> fechaBase.minusMonths(1)
                            }
                        }
                    ) { Icon(Icons.Default.ArrowBackIosNew, null) }

                    Text(
                        text = textoRango,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    IconButton(
                        onClick = {
                            fechaBase = when (tipoFiltro) {
                                0 -> fechaBase.plusDays(1)
                                1 -> fechaBase.plusWeeks(1)
                                else -> fechaBase.plusMonths(1)
                            }
                        }
                    ) { Icon(Icons.Default.ArrowForwardIos, null) }
                }
            }

            if (cargando) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator()
                        Text("Cargando datos...", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            } else {

                // ─── Ganancia líquida ────────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (gananciaNeta >= 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "GANANCIA LÍQUIDA",
                            fontWeight = FontWeight.Bold,
                            color = if (gananciaNeta >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                        Text(
                            "S/. ${"%.2f".format(gananciaNeta)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (gananciaNeta >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                        // Muestra cuántas ventas hay en el período para confirmar que cargó
                        Text(
                            "${todasLasVentas.size} venta(s) en el período",
                            fontSize = 12.sp,
                            color = if (gananciaNeta >= 0) Color(0xFF388E3C) else Color(0xFFE53935)
                        )
                    }
                }

                // ─── Totales ingresos / gastos ───────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CuadroReporte(
                        titulo = "Ingresos cobrados",
                        monto = totalIngresos,
                        colorFondo = Color(0xFFE3F2FD),
                        colorTexto = Color(0xFF1565C0),
                        modifier = Modifier.weight(1f).height(80.dp)
                    ) {}
                    CuadroReporte(
                        titulo = "Gastos (Mercado)",
                        monto = totalGastos,
                        colorFondo = Color(0xFFFFF3E0),
                        colorTexto = Color(0xFFE65100),
                        modifier = Modifier.weight(1f).height(80.dp)
                    ) {}
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Desglose de ingresos", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                // ─── Cuadros por método de pago ──────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CuadroReporte(
                        "Efectivo", totalEfectivo,
                        Color(0xFFF1F8E9), Color(0xFF33691E),
                        Modifier.weight(1f).height(70.dp)
                    ) { categoriaSeleccionada = "Efectivo"; mostrarBottomSheet = true }

                    CuadroReporte(
                        "Yape", totalYape,
                        Color(0xFFF3E5F5), Color(0xFF6A1B9A),
                        Modifier.weight(1f).height(70.dp)
                    ) { categoriaSeleccionada = "Yape"; mostrarBottomSheet = true }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CuadroReporte(
                        "Plin", totalPlin,
                        Color(0xFFE0F7FA), Color(0xFF00838F),
                        Modifier.weight(1f).height(70.dp)
                    ) { categoriaSeleccionada = "Plin"; mostrarBottomSheet = true }

                    CuadroReporte(
                        "Fiados", totalFiado,
                        Color(0xFFFFEBEE), Color(0xFFC62828),
                        Modifier.weight(1f).height(70.dp)
                    ) { categoriaSeleccionada = "Fiados"; mostrarBottomSheet = true }
                }
            }
        }

        // ─── Bottom Sheet con detalle de ventas por categoría ───────────────
        if (mostrarBottomSheet) {
            val ventasAMostrar = when (categoriaSeleccionada) {
                "Efectivo" -> ventasEfectivo
                "Yape" -> ventasYape
                "Plin" -> ventasPlin
                "Fiados" -> ventasFiado
                else -> todasLasVentas
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
                        text = "Pedidos: $categoriaSeleccionada",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${ventasAMostrar.size} registro(s) — S/. ${"%.2f".format(ventasAMostrar.sumOf { it.montoTotal ?: 0.0 })}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    if (ventasAMostrar.isEmpty()) {
                        Text(
                            "No hay ventas de $categoriaSeleccionada en este período.",
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxHeight(0.6f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(ventasAMostrar) { venta ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Receipt,
                                            null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = venta.detalles.takeIf { !it.isNullOrBlank() }
                                                    ?: "Sin detalles",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = "S/. ${"%.2f".format(venta.montoTotal ?: 0.0)}  •  ${venta.fecha?.take(10) ?: ""}",
                                                color = MaterialTheme.colorScheme.secondary,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { mostrarBottomSheet = false },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Cerrar") }
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
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = titulo,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = colorTexto
            )
            Spacer(modifier = Modifier.height(2.dp))
            // ✅ Consistente: siempre 2 decimales
            Text(
                text = "S/. ${"%.2f".format(monto)}",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = colorTexto
            )
        }
    }
}