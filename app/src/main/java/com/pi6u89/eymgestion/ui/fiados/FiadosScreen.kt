package com.pi6u89.eymgestion.ui.fiados

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pi6u89.eymgestion.data.CajaManager
import com.pi6u89.eymgestion.data.ClienteRepository
import com.pi6u89.eymgestion.domain.Cliente
import com.pi6u89.eymgestion.domain.Fiado
import com.pi6u89.eymgestion.domain.PlatoPrestado
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
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
    var clienteParaDetalle by remember { mutableStateOf<Cliente?>(null) }

    var mostrarDialogoMetodoPago by remember { mutableStateOf(false) }
    var metodoSeleccionado by remember { mutableStateOf("Efectivo") }
    var procesandoPago by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val cajaManager = remember { CajaManager(context) }
    val cajaAbierta by cajaManager.cajaAbiertaFlow.collectAsState(initial = false)
    val fechaJornada by cajaManager.fechaJornadaFlow.collectAsState(initial = null)

    suspend fun recargarPantalla() {
        cargando = true
        clientes = clienteRepository.obtenerClientes()
        listaFiados = clienteRepository.obtenerDeudasActivas()
        listaPlatos = clienteRepository.obtenerPlatosSinDevolver()
        cargando = false
    }

    LaunchedEffect(Unit) { recargarPantalla() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogoNuevoCliente = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Cliente")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            Text(text = "Mis Caseritos y Deudas", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            if (cargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(clientes) { cliente ->
                        val deudaTotal = listaFiados.filter { it.clienteId == cliente.id }.sumOf { it.monto }
                        val platosTotales = listaPlatos.filter { it.clienteId == cliente.id }.sumOf { it.cantidadPlatos }

                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { clienteParaDetalle = cliente },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(text = cliente.nombre, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    if(deudaTotal > 0) Text("S/. $deudaTotal", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                    if(platosTotales > 0) Text("$platosTotales platos", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    clienteParaDetalle?.let { cliente ->
        val deudasCliente = listaFiados.filter { it.clienteId == cliente.id }
        val platosCliente = listaPlatos.filter { it.clienteId == cliente.id }

        AlertDialog(
            onDismissRequest = { clienteParaDetalle = null },
            title = { Text("Cuenta de ${cliente.nombre}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // SECCIÓN DEUDAS
                    Text("💰 Dinero Adeudado", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                    if (deudasCliente.isEmpty()) Text("No registra deudas.")
                    else {
                        Text("Total: S/. ${deudasCliente.sumOf { it.monto }}", fontWeight = FontWeight.Bold)
                        Button(onClick = { mostrarDialogoMetodoPago = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Marcar Deuda Como Pagada")
                        }
                    }

                    HorizontalDivider()

                    // SECCIÓN PLATOS PRESTADOS (RESTAURADA)
                    Text("🍲 Vajilla Prestada", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    if (platosCliente.isEmpty()) {
                        Text("No tiene platos prestados.")
                    } else {
                        Text("Platos pendientes: ${platosCliente.sumOf { it.cantidadPlatos }}", fontWeight = FontWeight.Bold)
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    if (clienteRepository.registrarDevolucionPlatos(cliente.id)) {
                                        recargarPantalla()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Registrar Devolución") }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { clienteParaDetalle = null }) { Text("Cerrar") } }
        )
    }

    if (mostrarDialogoMetodoPago && clienteParaDetalle != null) {
        val cliente = clienteParaDetalle!!
        val totalMontoCobro = listaFiados.filter { it.clienteId == cliente.id }.sumOf { it.monto }

        AlertDialog(
            onDismissRequest = { if (!procesandoPago) mostrarDialogoMetodoPago = false },
            title = { Text("Confirmar Pago") },
            text = {
                Column {
                    Text("Cobrar S/. $totalMontoCobro a ${cliente.nombre}")
                    listOf("Efectivo", "Yape", "Plin").forEach { metodo ->
                        Row(Modifier.fillMaxWidth().clickable { metodoSeleccionado = metodo }, verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = metodoSeleccionado == metodo, onClick = { metodoSeleccionado = metodo })
                            Text(metodo)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !procesandoPago,
                    onClick = {
                        coroutineScope.launch {
                            procesandoPago = true
                            val fechaReloj = LocalDate.now().toString()
                            val fechaParaIngreso = if (cajaAbierta && fechaJornada != null) fechaJornada!! else {
                                cajaManager.abrirCaja(emptySet(), fechaReloj)
                                fechaReloj
                            }

                            val exito = clienteRepository.liquidarDeudaConPago(cliente.id, metodoSeleccionado, fechaParaIngreso)
                            if (exito) {
                                mostrarDialogoMetodoPago = false
                                clienteParaDetalle = null
                                recargarPantalla()
                            }
                            procesandoPago = false
                        }
                    }
                ) {
                    if (procesandoPago) CircularProgressIndicator(Modifier.size(20.dp))
                    else Text("Pagar")
                }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogoMetodoPago = false }) { Text("Cancelar") } }
        )
    }

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
                                recargarPantalla()
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