package com.pi6u89.eymgestion.ui.fiados

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pi6u89.eymgestion.data.ClienteRepository
import com.pi6u89.eymgestion.domain.Cliente
import com.pi6u89.eymgestion.domain.Fiado
import com.pi6u89.eymgestion.domain.PlatoPrestado
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiadosScreen() {
    val clienteRepository = remember { ClienteRepository() }
    val coroutineScope = rememberCoroutineScope()

    var clientes by remember { mutableStateOf<List<Cliente>>(emptyList()) }
    var listaFiados by remember { mutableStateOf<List<Fiado>>(emptyList()) }
    var listaPlatos by remember { mutableStateOf<List<PlatoPrestado>>(emptyList()) }

    var cargando by remember { mutableStateOf(true) }
    var mostrarDialogoNuevoCliente by remember { mutableStateOf(false) }

    // Estado para saber qué cliente se seleccionó para ver sus detalles
    var clienteParaDetalle by remember { mutableStateOf<Cliente?>(null) }

    // Función reutilizable para recargar todos los datos desde Supabase
    val cargarDatos = {
        coroutineScope.launch {
            cargando = true
            clientes = clienteRepository.obtenerClientes()
            listaFiados = clienteRepository.obtenerDeudasActivas()
            listaPlatos = clienteRepository.obtenerPlatosSinDevolver()
            cargando = false
        }
    }

    LaunchedEffect(Unit) {
        cargarDatos()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogoNuevoCliente = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Cliente")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(text = "Mis Caseritos y Deudas", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (clientes.isEmpty()) {
                Text("Aún no tienes clientes registrados.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(clientes) { cliente ->
                        val deudaTotal = listaFiados.filter { it.clienteId == cliente.id }.sumOf { it.monto }
                        val platosTotales = listaPlatos.filter { it.clienteId == cliente.id }.sumOf { it.cantidadPlatos }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { clienteParaDetalle = cliente }, // 👈 AHORA ES CLICKABLE
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp))
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(text = cliente.nombre, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                                        if (cliente.telefono.isNotEmpty()) {
                                            Text(text = "Cel: ${cliente.telefono}", fontSize = 14.sp)
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    if (deudaTotal > 0) {
                                        Text(text = "S/. $deudaTotal", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                    }
                                    if (platosTotales > 0) {
                                        Text(text = "$platosTotales platos", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                    if (deudaTotal == 0.0 && platosTotales == 0) {
                                        Text(text = "Sin deudas", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // VENTANA EMERGENTE: DETALLE DE DEUDAS Y PLATOS PRESTADOS
    clienteParaDetalle?.let { cliente ->
        val deudasCliente = listaFiados.filter { it.clienteId == cliente.id }
        val platosCliente = listaPlatos.filter { it.clienteId == cliente.id }

        AlertDialog(
            onDismissRequest = { clienteParaDetalle = null },
            title = { Text(text = "Cuenta de ${cliente.nombre}", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // --- SECCIÓN MONETARIA ---
                    Text(text = "💰 Dinero Adeudado:", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    if (deudasCliente.isEmpty()) {
                        Text("No registra deudas de dinero.", color = MaterialTheme.colorScheme.secondary)
                    } else {
                        Text(
                            text = "Total Pendiente: S/. ${deudasCliente.sumOf { it.monto }}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 18.sp
                        )
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val ok = clienteRepository.liquidarDeudaCliente(cliente.id)
                                    if (ok) {
                                        clienteParaDetalle = null
                                        cargarDatos() // Recarga la lista en vivo
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Marcar Deuda Como Pagada")
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    // --- SECCIÓN MÁGICA DE PLATOS PRESTADOS ---
                    Text(text = "🍲 Vajilla Prestada Detallada:", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    if (platosCliente.isEmpty()) {
                        Text("No tiene platos prestados.", color = MaterialTheme.colorScheme.secondary)
                    } else {
                        Text(
                            text = "Total Platos Pendientes: ${platosCliente.sumOf { it.cantidadPlatos }}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp
                        )

                        // Historial interno de platos llevados
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            platosCliente.forEach { registro ->
                                Text(
                                    text = "• ${registro.cantidadPlatos} platos lentos el día ${registro.fechaPrestamo}",
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val ok = clienteRepository.registrarDevolucionPlatos(cliente.id)
                                    if (ok) {
                                        clienteParaDetalle = null
                                        cargarDatos()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Registrar Devolución de Vajilla")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { clienteParaDetalle = null }) { Text("Cerrar") }
            }
        )
    }

    // Ventana emergente para registrar un nuevo cliente
    if (mostrarDialogoNuevoCliente) {
        var nombreNuevo by remember { mutableStateOf("") }
        var telefonoNuevo by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { mostrarDialogoNuevoCliente = false },
            title = { Text("Nuevo Cliente") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nombreNuevo,
                        onValueChange = { nombreNuevo = it },
                        label = { Text("Nombre Completo o Apodo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = telefonoNuevo,
                        onValueChange = { telefonoNuevo = it },
                        label = { Text("Teléfono (Opcional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nombreNuevo.isNotBlank()) {
                            coroutineScope.launch {
                                val nuevoCliente = Cliente(nombre = nombreNuevo, telefono = telefonoNuevo)
                                clienteRepository.agregarCliente(nuevoCliente)
                                cargarDatos()
                            }
                            mostrarDialogoNuevoCliente = false
                        }
                    }
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoNuevoCliente = false }) { Text("Cancelar") }
            }
        )
    }
}