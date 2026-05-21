package com.pi6u89.eymgestion.ui.fiados

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
    // Memoria para las deudas y platos
    var listaFiados by remember { mutableStateOf<List<Fiado>>(emptyList()) }
    var listaPlatos by remember { mutableStateOf<List<PlatoPrestado>>(emptyList()) }

    var cargando by remember { mutableStateOf(true) }
    var mostrarDialogoNuevoCliente by remember { mutableStateOf(false) }

    // Descargamos Clientes, Deudas y Platos al mismo tiempo
    LaunchedEffect(Unit) {
        clientes = clienteRepository.obtenerClientes()
        listaFiados = clienteRepository.obtenerDeudasActivas()
        listaPlatos = clienteRepository.obtenerPlatosSinDevolver()
        cargando = false
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

                        // MAGIA: Filtramos y sumamos lo de este cliente específico
                        val deudaTotal = listaFiados.filter { it.clienteId == cliente.id }.sumOf { it.monto }
                        val platosTotales = listaPlatos.filter { it.clienteId == cliente.id }.sumOf { it.cantidadPlatos }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween // Separa el nombre de las deudas
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

                                // COLUMNA DE DEUDAS (Lado derecho)
                                Column(horizontalAlignment = Alignment.End) {
                                    if (deudaTotal > 0) {
                                        Text(text = "S/. $deudaTotal", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                    }
                                    if (platosTotales > 0) {
                                        Text(text = "$platosTotales platos", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                    // Si no debe nada, mostramos un mensaje amigable
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
                                cargando = true
                                val nuevoCliente = Cliente(nombre = nombreNuevo, telefono = telefonoNuevo)
                                clienteRepository.agregarCliente(nuevoCliente)
                                clientes = clienteRepository.obtenerClientes()
                                cargando = false
                            }
                            mostrarDialogoNuevoCliente = false
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoNuevoCliente = false }) { Text("Cancelar") }
            }
        )
    }
}