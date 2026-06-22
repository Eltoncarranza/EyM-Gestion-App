package com.pi6u89.eymgestion.ui.compras

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pi6u89.eymgestion.data.CompraDelDia
import com.pi6u89.eymgestion.data.CompraDeLaSemana
import com.pi6u89.eymgestion.data.ComprasRepository

fun formatearCantidadDouble(cantidad: Double, unidad: String): String {
    val cantStr = if (cantidad == cantidad.toLong().toDouble())
        cantidad.toLong().toString()
    else "%.1f".format(cantidad)
    return "$cantStr $unidad"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialComprasScreen(navController: NavController) {
    val repository = remember { ComprasRepository() }
    var tabSeleccionado by remember { mutableIntStateOf(0) }

    var comprasPorDia by remember { mutableStateOf<List<CompraDelDia>>(emptyList()) }
    var comprasPorSemana by remember { mutableStateOf<List<CompraDeLaSemana>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        comprasPorDia = repository.obtenerHistorialPorDia()
        comprasPorSemana = repository.obtenerHistorialPorSemana()
        cargando = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Compras", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = tabSeleccionado) {
                Tab(
                    selected = tabSeleccionado == 0,
                    onClick = { tabSeleccionado = 0 },
                    text = { Text("Por Día", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = tabSeleccionado == 1,
                    onClick = { tabSeleccionado = 1 },
                    text = { Text("Por Semana", fontWeight = FontWeight.Bold) }
                )
            }

            if (cargando) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Cargando historial...", color = MaterialTheme.colorScheme.secondary)
                    }
                }
            } else {
                when (tabSeleccionado) {
                    0 -> HistorialPorDia(comprasPorDia)
                    1 -> HistorialPorSemana(comprasPorSemana)
                }
            }
        }
    }
}

@Composable
fun HistorialPorDia(dias: List<CompraDelDia>) {
    if (dias.isEmpty()) {
        EstadoVacio()
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(dias) { dia ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            dia.fecha,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "S/. ${"%.2f".format(dia.totalGastado)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    HorizontalDivider()
                    // Lista de productos
                    dia.productos.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.producto, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text(
                                    formatearCantidadDouble(item.cantidad, item.unidad),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Text(
                                "S/. ${"%.2f".format(item.costo)}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistorialPorSemana(semanas: List<CompraDeLaSemana>) {
    if (semanas.isEmpty()) {
        EstadoVacio()
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(semanas) { semana ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Semana",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                semana.etiqueta,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Total",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                "S/. ${"%.2f".format(semana.totalGastado)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    HorizontalDivider()

                    val porDia = semana.productos.groupBy { it.fecha.take(10) }
                    porDia.forEach { (fechaRaw, itemsDia) ->

                        Text(
                            itemsDia.first().fecha.take(10).let { f ->
                                try {
                                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                    val sdfOut = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                                    sdfOut.format(sdf.parse(f)!!)
                                } catch (e: Exception) { f }
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        itemsDia.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.producto, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Text(
                                        formatearCantidadDouble(item.cantidad, item.unidad),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Text(
                                    "S/. ${"%.2f".format(item.costo)}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EstadoVacio() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.ShoppingBag, null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Text("Sin compras registradas", color = MaterialTheme.colorScheme.secondary)
            Text(
                "Marca items como comprados para ver el historial.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}