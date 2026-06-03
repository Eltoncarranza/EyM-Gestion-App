package com.pi6u89.eymgestion.ui.Compras

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprasScreen() {
    val comprasRepository = remember { ComprasRepository() }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var listaItems by remember { mutableStateOf<List<ItemCompra>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var mostrarDialogoAgregar by remember { mutableStateOf(false) }
    var itemParaComprar by remember { mutableStateOf<ItemCompra?>(null) }

    // Función segura para recargar datos
    val cargarDatos = {
        coroutineScope.launch {
            cargando = true
            try {
                listaItems = comprasRepository.obtenerListaCompras()
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Error al cargar datos: ${e.message}")
            }
            cargando = false
        }
    }

    LaunchedEffect(Unit) { cargarDatos() }

    val pendientes = listaItems.filter { !it.comprado }
    val comprados = listaItems.filter { it.comprado }
    val totalGastado = comprados.sumOf { it.costo ?: 0.0 }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogoAgregar = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            Text(text = "Abastecimiento", fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total invertido en insumos:", fontSize = 14.sp)
                    Text("S/. ${"%.2f".format(totalGastado)}", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { Text("Pendientes", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) }

                    items(pendientes) { item ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(item.producto, fontWeight = FontWeight.Bold)
                                    Text("Cant: ${item.cantidad}", fontSize = 12.sp)
                                }
                                Row {
                                    IconButton(onClick = { itemParaComprar = item }) { Icon(Icons.Default.Check, null, tint = Color.Green) }
                                    IconButton(onClick = {
                                        coroutineScope.launch {
                                            try {
                                                comprasRepository.eliminarItem(item.id)
                                                cargarDatos()
                                                snackbarHostState.showSnackbar("Eliminado")
                                            } catch (e: Exception) {
                                                snackbarHostState.showSnackbar("Error al eliminar")
                                            }
                                        }
                                    }) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)); Text("Ya comprados", fontWeight = FontWeight.Bold) }
                    items(comprados) { item ->
                        ListItem(
                            headlineContent = { Text(item.producto) },
                            trailingContent = { Text("S/. ${item.costo}", fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        }
    }

    // DIÁLOGO AGREGAR
    if (mostrarDialogoAgregar) {
        var prod by remember { mutableStateOf("") }
        var cant by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { mostrarDialogoAgregar = false },
            title = { Text("¿Qué insumo falta?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = prod, onValueChange = { prod = it }, label = { Text("Producto") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = cant, onValueChange = { cant = it }, label = { Text("Cantidad") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (prod.isNotBlank() && cant.isNotBlank()) {
                        coroutineScope.launch {
                            try {
                                comprasRepository.agregarItem(ItemCompra(producto = prod, cantidad = cant))
                                cargarDatos()
                                mostrarDialogoAgregar = false
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Error al agregar: ${e.message}")
                            }
                        }
                    }
                }) { Text("Apuntar") }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogoAgregar = false }) { Text("Cancelar") } }
        )
    }

    // DIÁLOGO COMPRAR
    itemParaComprar?.let { item ->
        var costoInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { itemParaComprar = null },
            title = { Text("Registrar Gasto: ${item.producto}") },
            text = {
                OutlinedTextField(
                    value = costoInput,
                    onValueChange = { costoInput = it },
                    label = { Text("Costo Total (S/.)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    val c = costoInput.toDoubleOrNull()
                    if (c != null) {
                        coroutineScope.launch {
                            try {
                                comprasRepository.marcarComoComprado(item.id, c)
                                itemParaComprar = null
                                cargarDatos()
                                snackbarHostState.showSnackbar("Gasto registrado con éxito")
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Error al guardar en Supabase")
                            }
                        }
                    } else {
                        coroutineScope.launch { snackbarHostState.showSnackbar("Ingresa un monto válido") }
                    }
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { itemParaComprar = null }) { Text("Cancelar") } }
        )
    }
}