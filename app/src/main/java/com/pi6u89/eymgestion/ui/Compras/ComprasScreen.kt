package com.pi6u89.eymgestion.ui.compras

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    var listaItems by remember { mutableStateOf<List<ItemCompra>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    var mostrarDialogoAgregar by remember { mutableStateOf(false) }
    var itemParaComprar by remember { mutableStateOf<ItemCompra?>(null) }

    val cargarDatos = {
        coroutineScope.launch {
            cargando = true
            listaItems = comprasRepository.obtenerListaCompras()
            cargando = false
        }
    }

    LaunchedEffect(Unit) { cargarDatos() }
    val pendientes = listaItems.filter { !it.comprado }
    val comprados = listaItems.filter { it.comprado }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogoAgregar = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir falta")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(text = "Abastecimiento e Insumos", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(text = "📌 Pendientes para el próximo mercado", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                    }

                    if (pendientes.isEmpty()) {
                        item { Text("¡Todo abastecido! No hay pendientes.", fontSize = 14.sp, modifier = Modifier.padding(start = 8.dp)) }
                    } else {
                        items(pendientes) { item ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = item.producto, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text(text = "Cantidad: ${item.cantidad}", fontSize = 14.sp)
                                    }
                                    Row {
                                        IconButton(onClick = { itemParaComprar = item }) {
                                            Icon(Icons.Default.Check, contentDescription = "Comprado", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = {
                                            coroutineScope.launch {
                                                comprasRepository.eliminarItem(item.id)
                                                cargarDatos()
                                            }
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "✅ Registro de Gastos Realizados", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    }

                    if (comprados.isEmpty()) {
                        item { Text("Aún no registras compras completadas.", fontSize = 14.sp, modifier = Modifier.padding(start = 8.dp)) }
                    } else {
                        items(comprados) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = item.producto, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                                        Text(text = "Comprado: ${item.cantidad}", fontSize = 13.sp)
                                    }
                                    Text(text = "S/. ${item.costo}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoAgregar) {
        var prod by remember { mutableStateOf("") }
        var cant by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { mostrarDialogoAgregar = false },
            title = { Text("¿Qué insumo falta?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = prod, onValueChange = { prod = it }, label = { Text("Producto (Ej: Leche Gloria)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = cant, onValueChange = { cant = it }, label = { Text("Cantidad (Ej: 1 doce o 3 kg)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (prod.isNotBlank() && cant.isNotBlank()) {
                        coroutineScope.launch {
                            comprasRepository.agregarItem(ItemCompra(producto = prod, cantidad = cant))
                            cargarDatos()
                        }
                        mostrarDialogoAgregar = false
                    }
                }) { Text("Apuntar") }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogoAgregar = false }) { Text("Cancelar") } }
        )
    }

    itemParaComprar?.let { item ->
        var costoInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { itemParaComprar = null },
            title = { Text("Registrar Gasto: ${item.producto}") },
            text = {
                Column {
                    Text("Ingresa el monto total pagado en el mercado por este producto:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = costoInput,
                        onValueChange = { costoInput = it },
                        label = { Text("Costo Total (S/.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val c = costoInput.toDoubleOrNull() ?: 0.0
                    coroutineScope.launch {
                        comprasRepository.marcarComoComprado(item.id, c)
                        itemParaComprar = null
                        cargarDatos()
                    }
                }) { Text("Confirmar e Ingresar Gasto") }
            },
            dismissButton = { TextButton(onClick = { itemParaComprar = null }) { Text("Cancelar") } }
        )
    }
}