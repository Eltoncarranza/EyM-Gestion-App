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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiadosScreen() {
    val clienteRepository = remember { ClienteRepository() }
    val coroutineScope = rememberCoroutineScope()

    // Lista de clientes que se mostrará en pantalla
    var clientes by remember { mutableStateOf<List<Cliente>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    // Controla si se muestra la ventana para agregar un cliente
    var mostrarDialogoNuevoCliente by remember { mutableStateOf(false) }

    // Esta función se ejecuta apenas abres la pestaña para descargar los datos
    LaunchedEffect(Unit) {
        clientes = clienteRepository.obtenerClientes()
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
            Text(text = "Mis Clientes (Caseritos)", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (clientes.isEmpty()) {
                Text("Aún no tienes clientes registrados.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(clientes) { cliente ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(text = cliente.nombre, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                                    if (cliente.telefono.isNotEmpty()) {
                                        Text(text = "Cel: ${cliente.telefono}", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
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
                                // 1. Marcamos como cargando
                                cargando = true
                                // 2. Guardamos en Supabase
                                val nuevoCliente = Cliente(nombre = nombreNuevo, telefono = telefonoNuevo)
                                clienteRepository.agregarCliente(nuevoCliente)
                                // 3. Volvemos a descargar la lista actualizada
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
                TextButton(onClick = { mostrarDialogoNuevoCliente = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}