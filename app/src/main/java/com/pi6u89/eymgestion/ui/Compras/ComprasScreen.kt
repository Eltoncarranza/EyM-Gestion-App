package com.pi6u89.eymgestion.ui.compras

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pi6u89.eymgestion.data.ComprasRepository
import com.pi6u89.eymgestion.domain.ItemCompra
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprasScreen() {
    val comprasRepository = remember { ComprasRepository() }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var listaItems by remember { mutableStateOf<List<ItemCompra>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var procesandoAccion by remember { mutableStateOf(false) }
    var mostrarDialogoAgregar by remember { mutableStateOf(false) }
    var itemParaComprar by remember { mutableStateOf<ItemCompra?>(null) }

    suspend fun cargarDatos() {
        cargando = true
        listaItems = comprasRepository.obtenerListaCompras()
        cargando = false
    }

    LaunchedEffect(Unit) { cargarDatos() }

    val pendientes = listaItems.filter { !it.comprado }
    val comprados = listaItems.filter { it.comprado }
    val totalGastado = comprados.sumOf { it.costo }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogoAgregar = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(text = "Abastecimiento", fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total invertido (historial):", fontSize = 14.sp)
                    Text(
                        "S/. ${"%.2f".format(totalGastado)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            "Pendientes para comprar",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    if (pendientes.isEmpty()) {
                        item {
                            Text(
                                "No hay insumos pendientes.",
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }

                    items(pendientes, key = { it.id }) { item ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.producto, fontWeight = FontWeight.Bold)
                                    Text("Cant: ${item.cantidad}", fontSize = 12.sp)
                                }
                                Row {
                                    IconButton(onClick = { itemParaComprar = item }) {
                                        Icon(Icons.Default.Check, null, tint = Color(0xFF2E7D32))
                                    }
                                    IconButton(
                                        enabled = !procesandoAccion,
                                        onClick = {
                                            coroutineScope.launch {
                                                procesandoAccion = true
                                                val ok = comprasRepository.eliminarItem(item.id)
                                                if (ok) {
                                                    cargarDatos()
                                                } else {
                                                    snackbarHostState.showSnackbar("Error al eliminar")
                                                }
                                                procesandoAccion = false
                                            }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Ya comprados", fontWeight = FontWeight.Bold)
                    }

                    if (comprados.isEmpty()) {
                        item {
                            Text(
                                "Sin compras registradas aún.",
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }

                    items(comprados, key = { it.id }) { item ->
                        ListItem(
                            headlineContent = { Text(item.producto) },
                            supportingContent = {
                                Text(item.fecha ?: "", fontSize = 12.sp)
                            },
                            trailingContent = {
                                Text(
                                    "S/. ${"%.2f".format(item.costo)}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    if (mostrarDialogoAgregar) {
        var prod by remember { mutableStateOf("") }
        var cant by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { if (!procesandoAccion) mostrarDialogoAgregar = false },
            title = { Text("Apuntar nuevo insumo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = prod,
                        onValueChange = { prod = it },
                        label = { Text("Producto") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cant,
                        onValueChange = { cant = it },
                        label = { Text("Cantidad") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = prod.isNotBlank() && cant.isNotBlank() && !procesandoAccion,
                    onClick = {
                        coroutineScope.launch {
                            procesandoAccion = true
                            val ok = comprasRepository.agregarItem(
                                ItemCompra(producto = prod.trim(), cantidad = cant.trim())
                            )
                            if (ok) {
                                cargarDatos()
                                mostrarDialogoAgregar = false
                            } else {
                                snackbarHostState.showSnackbar("Error al guardar")
                            }
                            procesandoAccion = false
                        }
                    }
                ) { Text("Guardar") }
            },
            dismissButton = {
                if (!procesandoAccion) {
                    TextButton(onClick = { mostrarDialogoAgregar = false }) { Text("Cancelar") }
                }
            }
        )
    }

    itemParaComprar?.let { item ->
        var costoInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { if (!procesandoAccion) itemParaComprar = null },
            title = { Text("Compraste: ${item.producto}") },
            text = {
                OutlinedTextField(
                    value = costoInput,
                    onValueChange = { costoInput = it },
                    label = { Text("Costo total pagado (S/.)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    enabled = costoInput.isNotBlank() && !procesandoAccion,
                    onClick = {
                        val costo = costoInput.toDoubleOrNull()
                        if (costo == null || costo <= 0.0) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Ingresa un monto válido")
                            }
                            return@Button
                        }
                        coroutineScope.launch {
                            procesandoAccion = true
                            val fechaHoy = LocalDate.now().toString()
                            val ok = comprasRepository.marcarComoComprado(item.id, costo, fechaHoy)
                            if (ok) {
                                itemParaComprar = null
                                cargarDatos()
                                snackbarHostState.showSnackbar("Gasto registrado")
                            } else {
                                snackbarHostState.showSnackbar("Error al guardar")
                            }
                            procesandoAccion = false
                        }
                    }
                ) { Text("Confirmar Gasto") }
            },
            dismissButton = {
                if (!procesandoAccion) {
                    TextButton(onClick = { itemParaComprar = null }) { Text("Cancelar") }
                }
            }
        )
    }
}