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

    // 1. Creamos una función suspendida pura (sin lanzar dobles corrutinas)
    suspend fun recargarPantalla() {
        cargando = true
        clientes = clienteRepository.obtenerClientes()
        listaFiados = clienteRepository.obtenerDeudasActivas()
        listaPlatos = clienteRepository.obtenerPlatosSinDevolver()
        cargando = false
    }

    // 2. La carga inicial al abrir la pestaña
    LaunchedEffect(Unit) {
        recargarPantalla()
    }


    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogoNuevoCliente = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (clientes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aún no tienes clientes registrados.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(clientes) { cliente ->
                        val deudaTotal = listaFiados.filter { it.clienteId == cliente.id }.sumOf { it.monto }
                        val platosTotales = listaPlatos.filter { it.clienteId == cliente.id }.sumOf { it.cantidadPlatos }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { clienteParaDetalle = cliente },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
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
                    Text(text = "💰 Dinero Adeudado", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.error)
                    if (deudasCliente.isEmpty()) {
                        Text("No registra deudas de dinero.", color = MaterialTheme.colorScheme.secondary)
                    } else {
                        Text(
                            text = "Total Pendiente: S/. ${deudasCliente.sumOf { it.monto }}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Button(
                            onClick = {
                                mostrarDialogoMetodoPago = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Marcar Deuda Como Pagada")
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(text = "🍲 Vajilla Prestada", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    if (platosCliente.isEmpty()) {
                        Text("No tiene platos prestados.", color = MaterialTheme.colorScheme.secondary)
                    } else {
                        Text(
                            text = "Platos Pendientes: ${platosCliente.sumOf { it.cantidadPlatos }}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            platosCliente.forEach { registro ->
                                Text(
                                    text = "• ${registro.cantidadPlatos} platos el día ${registro.fechaPrestamo}",
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
                                        recargarPantalla()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Registrar Devolución")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { clienteParaDetalle = null }) { Text("Cerrar") }
            }
        )
    }

    if (mostrarDialogoMetodoPago && clienteParaDetalle != null) {
        val totalMontoCobro = listaFiados.filter { it.clienteId == clienteParaDetalle!!.id }.sumOf { it.monto }

        AlertDialog(
            onDismissRequest = { if (!procesandoPago) mostrarDialogoMetodoPago = false },
            title = { Text("Registrar Ingreso de Caja", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Se registrará la liquidación de deuda para **${clienteParaDetalle!!.nombre}**.")
                    Text("Monto Total a Recaudar: **S/. $totalMontoCobro**", fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Selecciona cómo se recibió el dinero:", fontWeight = FontWeight.SemiBold)

                    val opcionesPago = listOf("Efectivo", "Yape", "Plin")
                    opcionesPago.forEach { opcion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { if (!procesandoPago) metodoSeleccionado = opcion }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (metodoSeleccionado == opcion),
                                onClick = { if (!procesandoPago) metodoSeleccionado = opcion }
                            )
                            Text(text = opcion, modifier = Modifier.padding(start = 8.dp))
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

                            // CORRECCIÓN: Agregamos emptySet() para que compile correctamente con el nuevo CajaManager
                            val fechaParaIngreso = if (cajaAbierta && fechaJornada != null) {
                                fechaJornada!!
                            } else {
                                cajaManager.abrirCaja(emptySet(), fechaReloj)
                                fechaReloj
                            }

                            val exito = clienteRepository.liquidarDeudaConPago(
                                clienteId = clienteParaDetalle!!.id,
                                metodoPago = metodoSeleccionado,
                                fechaHoy = fechaParaIngreso
                            )

                            if (exito) {
                                mostrarDialogoMetodoPago = false
                                clienteParaDetalle = null
                                recargarPantalla()
                            }
                            procesandoPago = false
                        }
                    }
                ) {
                    if (procesandoPago) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Confirmar Pago")
                    }
                }

            },
            dismissButton = {
                if (!procesandoPago) {
                    TextButton(onClick = { mostrarDialogoMetodoPago = false }) { Text("Cancelar") }
                }
            }
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