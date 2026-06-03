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

    // Control de Fechas y Filtros (0=Día, 1=Semana, 2=Mes)
    var tipoFiltro by remember { mutableIntStateOf(0) }
    var fechaBase by remember { mutableStateOf(LocalDate.now()) }

    // BottomSheet para ver el detalle
    val sheetState = rememberModalBottomSheetState()
    var mostrarBottomSheet by remember { mutableStateOf(false) }
    var categoriaSeleccionada by remember { mutableStateOf("") }

    // Carga de datos masiva al entrar a la pantalla
    LaunchedEffect(Unit) {
        cargando = true
        todasLasVentas = ventasRepository.obtenerTodasLasVentas()
        todasLasCompras = comprasRepository.obtenerListaCompras()
        cargando = false
    }

    // 1. CÁLCULO DEL RANGO DE FECHAS SEGÚN EL FILTRO ELEGIDO
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

    val formatoDia = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val formatoMes = DateTimeFormatter.ofPattern("MM/yyyy")

    val textoRango = when (tipoFiltro) {
        0 -> if (fechaBase == LocalDate.now()) "Hoy (${fechaBase.format(formatoDia)})" else fechaBase.format(formatoDia)
        1 -> "${fechaInicio.format(formatoDia)} al ${fechaFin.format(formatoDia)}"
        2 -> "Mes: ${fechaBase.format(formatoMes)}"
        else -> ""
    }

    // 2. FILTRAMOS LAS LISTAS USANDO EL RANGO (Ingresos vs Gastos)
    val ventasFiltradas = todasLasVentas.filter { venta ->
        val fechaVenta = try { LocalDate.parse(venta.fecha) } catch (e: Exception) { null }
        fechaVenta != null && !fechaVenta.isBefore(fechaInicio) && !fechaVenta.isAfter(fechaFin)
    }

    val comprasFiltradas = todasLasCompras.filter { compra ->
        if (!compra.comprado || compra.fecha.isBlank()) false
        else {
            val fechaCompra = try { LocalDate.parse(compra.fecha) } catch (e: Exception) { null }
            fechaCompra != null && !fechaCompra.isBefore(fechaInicio) && !fechaCompra.isAfter(fechaFin)
        }
    }

    // 3. CÁLCULOS MATEMÁTICOS
    val ventasEfectivo = ventasFiltradas.filter { (it.metodoPago).contains("Efectivo", true) && (it.estadoPago ?: "").contains("PAGADO", true) }
    val ventasYape = ventasFiltradas.filter { (it.metodoPago).contains("Yape", true) && (it.estadoPago ?: "").contains("PAGADO", true) }
    val ventasPlin = ventasFiltradas.filter { (it.metodoPago).contains("Plin", true) && (it.estadoPago ?: "").contains("PAGADO", true) }
    val ventasFiado = ventasFiltradas.filter { (it.estadoPago ?: "").contains("FIADO", true) }

    val totalIngresos = ventasEfectivo.sumOf { it.montoTotal } + ventasYape.sumOf { it.montoTotal } + ventasPlin.sumOf { it.montoTotal }
    val totalGastos = comprasFiltradas.sumOf { it.costo }
    val gananciaNeta = totalIngresos - totalGastos

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Balance Financiero", fontSize = 28.sp, fontWeight = FontWeight.Bold)

            // --- TABS DE FILTRO (DÍA / SEMANA / MES) ---
            TabRow(selectedTabIndex = tipoFiltro) {
                listOf("Día", "Semana", "Mes").forEachIndexed { index, title ->
                    Tab(
                        selected = tipoFiltro == index,
                        onClick = { tipoFiltro = index; fechaBase = LocalDate.now() },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // --- NAVEGADOR DE TIEMPO ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        fechaBase = when (tipoFiltro) { 0 -> fechaBase.minusDays(1); 1 -> fechaBase.minusWeeks(1); else -> fechaBase.minusMonths(1) }
                    }) { Icon(Icons.Default.ArrowBackIosNew, null) }

                    Text(text = textoRango, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    IconButton(onClick = {
                        fechaBase = when (tipoFiltro) { 0 -> fechaBase.plusDays(1); 1 -> fechaBase.plusWeeks(1); else -> fechaBase.plusMonths(1) }
                    }) { Icon(Icons.Default.ArrowForwardIos, null) }
                }
            }

            if (cargando) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {

                // --- DASHBOARD DE RESULTADOS ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (gananciaNeta >= 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("GANANCIA LÍQUIDA", fontWeight = FontWeight.Bold, color = if (gananciaNeta >= 0) Color(0xFF2E7D32) else Color(0xFFC62828))
                        Text("S/. ${"%.2f".format(gananciaNeta)}", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = if (gananciaNeta >= 0) Color(0xFF2E7D32) else Color(0xFFC62828))
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CuadroReporte(titulo = "Ingresos", monto = totalIngresos, colorFondo = Color(0xFFE3F2FD), colorTexto = Color(0xFF1565C0), modifier = Modifier.weight(1f).height(80.dp)) {}
                    CuadroReporte(titulo = "Gastos (Mercado)", monto = totalGastos, colorFondo = Color(0xFFFFF3E0), colorTexto = Color(0xFFE65100), modifier = Modifier.weight(1f).height(80.dp)) {}
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Desglose de Ingresos", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CuadroReporte("Efectivo", ventasEfectivo.sumOf { it.montoTotal }, Color(0xFFF1F8E9), Color(0xFF33691E), Modifier.weight(1f).height(70.dp)) { categoriaSeleccionada = "Efectivo"; mostrarBottomSheet = true }
                    CuadroReporte("Yape", ventasYape.sumOf { it.montoTotal }, Color(0xFFF3E5F5), Color(0xFF6A1B9A), Modifier.weight(1f).height(70.dp)) { categoriaSeleccionada = "Yape"; mostrarBottomSheet = true }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CuadroReporte("Plin", ventasPlin.sumOf { it.montoTotal }, Color(0xFFE0F7FA), Color(0xFF00838F), Modifier.weight(1f).height(70.dp)) { categoriaSeleccionada = "Plin"; mostrarBottomSheet = true }
                    CuadroReporte("Fiados", ventasFiado.sumOf { it.montoTotal }, Color(0xFFFFEBEE), Color(0xFFC62828), Modifier.weight(1f).height(70.dp)) { categoriaSeleccionada = "Fiados"; mostrarBottomSheet = true }
                }
            }
        }

        // --- BOTTOM SHEET (Casi idéntico, solo adaptado) ---
        if (mostrarBottomSheet) {
            val ventasAMostrar = when (categoriaSeleccionada) {
                "Efectivo" -> ventasEfectivo
                "Yape" -> ventasYape
                "Plin" -> ventasPlin
                "Fiados" -> ventasFiado
                else -> ventasFiltradas
            }

            ModalBottomSheet(onDismissRequest = { mostrarBottomSheet = false }, sheetState = sheetState) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).padding(bottom = 32.dp)) {
                    Text(text = "Pedidos pagados con $categoriaSeleccionada", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    if (ventasAMostrar.isEmpty()) {
                        Text("No hay registros en este periodo.", modifier = Modifier.padding(vertical = 16.dp))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxHeight(0.6f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ventasAMostrar) { venta ->
                                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Receipt, null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(text = venta.detalles.takeIf { !it.isNullOrBlank() } ?: "Sin detalles", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                            Text(text = "Monto: S/. ${venta.montoTotal}", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { mostrarBottomSheet = false }, modifier = Modifier.fillMaxWidth()) { Text("Cerrar") }
                }
            }
        }
    }
}

@Composable
fun CuadroReporte(titulo: String, monto: Double, colorFondo: Color, colorTexto: Color, modifier: Modifier = Modifier, alPresionar: () -> Unit) {
    Card(
        modifier = modifier.clickable { alPresionar() },
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = titulo, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colorTexto)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "S/. ${"%.1f".format(monto)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colorTexto)
        }
    }
}