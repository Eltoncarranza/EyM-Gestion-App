package com.pi6u89.eymgestion.ui.fiados

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    val snackbarHostState = remember { SnackbarHostState() }

    var clientes by remember { mutableStateOf<List<Cliente>>(emptyList()) }
    var listaFiados by remember { mutableStateOf<List<Fiado>>(emptyList()) }
    var listaPlatos by remember { mutableStateOf<List<PlatoPrestado>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    var mostrarDialogoNuevoCliente by remember { mutableStateOf(false) }
    var clienteParaDetalle by remember { mutableStateOf<Cliente?>(null) }
    var mostrarDialogoMetodoPago by remember { mutableStateOf(false) }
    var metodoSeleccionado by remember { mutableStateOf("Efectivo") }
    var procesandoPago by remember { mutableStateOf(false) }
    var montoAbonoInput by remember { mutableStateOf("") }

    val context = LocalContext.current
    val cajaManager = remember { CajaManager(context) }
    val cajaAbierta by cajaManager.cajaAbiertaFlow.collectAsState(initial = false)
    val fechaJornada by cajaManager.fechaJornadaFlow.collectAsState(initial = null)

    suspend fun recargar() {
        cargando = true
        clientes = clienteRepository.obtenerClientes()
        listaFiados = clienteRepository.obtenerDeudasActivas()
        listaPlatos = clienteRepository.obtenerPlatosSinDevolver()
        cargando = false
    }

    LaunchedEffect(Unit) { recargar() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (clientes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Sin clientes registrados.", color = MaterialTheme.colorScheme.secondary)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(clientes, key = { it.id }) { cliente ->
                        val deudaTotal = listaFiados
                            .filter { it.clienteId == cliente.id }
                            .sumOf { it.monto }
                        val platosTotales = listaPlatos
                            .filter { it.clienteId == cliente.id }
                            .sumOf { it.cantidadPlatos }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { clienteParaDetalle = cliente },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = cliente.nombre,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    if (deudaTotal > 0) {
                                        Text(
                                            "S/. ${"%.2f".format(deudaTotal)}",
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    if (platosTotales > 0) {
                                        Text(
                                            "$platosTotales platos",
                                            color = MaterialTheme.colorScheme.primary
                                        )
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
        val deudaTotal = deudasCliente.sumOf { it.monto }

        AlertDialog(
            onDismissRequest = { clienteParaDetalle = null },
            title = { Text("Cuenta de ${cliente.nombre}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Dinero Adeudado",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                    if (deudasCliente.isEmpty()) {
                        Text("No registra deudas de dinero.")
                    } else {
                        Text(
                            "Total pendiente: S/. ${"%.2f".format(deudaTotal)}",
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = {
                                montoAbonoInput = "%.2f".format(deudaTotal)
                                mostrarDialogoMetodoPago = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Abonar o Liquidar Deuda")
                        }
                    }

                    HorizontalDivider()

                    Text(
                        "Vajilla Prestada",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (platosCliente.isEmpty()) {
                        Text("No tiene platos prestados.")
                    } else {
                        Text(
                            "Platos pendientes: ${platosCliente.sumOf { it.cantidadPlatos }}",
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val ok = clienteRepository.registrarDevolucionPlatos(cliente.id)
                                    if (ok) {
                                        recargar()
                                        snackbarHostState.showSnackbar("Platos devueltos")
                                    } else {
                                        snackbarHostState.showSnackbar("Error al registrar devolución")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Registrar Devolución") }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { clienteParaDetalle = null }) { Text("Cerrar") }
            }
        )
    }

    if (mostrarDialogoMetodoPago && clienteParaDetalle != null) {
        val cliente = clienteParaDetalle!!
        val deudaTotal = listaFiados.filter { it.clienteId == cliente.id }.sumOf { it.monto }

        AlertDialog(
            onDismissRequest = { if (!procesandoPago) mostrarDialogoMetodoPago = false },
            title = { Text("Registrar Abono: ${cliente.nombre}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Deuda total: S/. ${"%.2f".format(deudaTotal)}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        "Ingresa el monto que el cliente paga ahora:",
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = montoAbonoInput,
                        onValueChange = { montoAbonoInput = it },
                        label = { Text("Monto a Pagar (S/.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text(
                        "Método de pago:",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Efectivo", "Yape", "Plin").forEach { metodo ->
                            val seleccionado = metodoSeleccionado == metodo
                            OutlinedButton(
                                onClick = { if (!procesandoPago) metodoSeleccionado = metodo },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (seleccionado)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surface,
                                    contentColor = if (seleccionado)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                            ) { Text(metodo, fontSize = 12.sp) }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !procesandoPago && montoAbonoInput.isNotBlank(),
                    onClick = {
                        val montoAbono = montoAbonoInput.toDoubleOrNull() ?: 0.0
                        if (montoAbono <= 0.0 || montoAbono > deudaTotal) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Monto invalido. Debe ser mayor a 0 y no exceder la deuda.")
                            }
                            return@Button
                        }
                        coroutineScope.launch {
                            procesandoPago = true
                            val fecha = if (cajaAbierta && !fechaJornada.isNullOrBlank())
                                fechaJornada!!
                            else
                                LocalDate.now().toString()

                            val ok = clienteRepository.registrarAbonoDeuda(
                                clienteId = cliente.id,
                                montoPagado = montoAbono,
                                metodoPago = metodoSeleccionado,
                                fecha = fecha
                            )

                            if (ok) {
                                mostrarDialogoMetodoPago = false
                                clienteParaDetalle = null
                                recargar()
                                snackbarHostState.showSnackbar("Abono registrado correctamente")
                            } else {
                                snackbarHostState.showSnackbar("Error de conexión al registrar el abono")
                            }
                            procesandoPago = false
                        }
                    }
                ) {
                    if (procesandoPago) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Confirmar Abono")
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
                        label = { Text("Nombre o Apodo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = telefonoNuevo,
                        onValueChange = { telefonoNuevo = it },
                        label = { Text("Telefono (Opcional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = nombreNuevo.isNotBlank(),
                    onClick = {
                        coroutineScope.launch {
                            val ok = clienteRepository.agregarCliente(
                                Cliente(nombre = nombreNuevo.trim(), telefono = telefonoNuevo.trim())
                            )
                            mostrarDialogoNuevoCliente = false
                            if (ok) {
                                recargar()
                                snackbarHostState.showSnackbar("Cliente guardado")
                            } else {
                                snackbarHostState.showSnackbar("Error al crear cliente")
                            }
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