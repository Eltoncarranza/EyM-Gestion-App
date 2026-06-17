package com.pi6u89.eymgestion.ui.venta

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pi6u89.eymgestion.data.CajaManager
import com.pi6u89.eymgestion.data.ClienteRepository
import com.pi6u89.eymgestion.data.VentasRepository
import com.pi6u89.eymgestion.domain.Cliente
import com.pi6u89.eymgestion.domain.Fiado
import com.pi6u89.eymgestion.domain.PlatoPrestado
import com.pi6u89.eymgestion.domain.Venta
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.util.UUID

@Serializable
data class ItemCarrito(
    val nombre: String,
    val cantidad: Int,
    val precioUnitario: Double
) {
    val total: Double get() = cantidad * precioUnitario
}

@Serializable
data class PedidoGuardado(
    val id: String,
    val nombreReferencia: String,
    val items: List<ItemCarrito>
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun VentaScreen() {
    val contexto = LocalContext.current
    val cajaManager = remember { CajaManager(contexto) }
    val cajaAbierta by cajaManager.cajaAbiertaFlow.collectAsState(initial = false)
    val fechaJornada by cajaManager.fechaJornadaFlow.collectAsState(initial = null)
    val platosActivos by cajaManager.platosActivosFlow.collectAsState(initial = emptySet<String>())
    val coroutineScope = rememberCoroutineScope()
    val clienteRepository = remember { ClienteRepository() }
    var listaClientes by remember { mutableStateOf<List<Cliente>>(emptyList()) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        listaClientes = clienteRepository.obtenerClientes()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (!cajaAbierta) {
                VistaApertura(
                    alAbrirCaja = { platosSeleccionados ->
                        coroutineScope.launch {
                            val fechaHoy = LocalDate.now().toString()
                            cajaManager.abrirCaja(platosSeleccionados, fechaHoy)
                        }
                    }
                )
            } else {
                VistaPuntoDeVenta(
                    listaClientes = listaClientes,
                    platosActivos = platosActivos,
                    cajaAbierta = cajaAbierta,
                    fechaJornada = fechaJornada,
                    snackbarHostState = snackbarHostState,
                    cajaManager = cajaManager,
                    alCerrarCaja = {
                        coroutineScope.launch {
                            cajaManager.cerrarCaja()
                            snackbarHostState.showSnackbar("Caja cerrada correctamente.")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaApertura(alAbrirCaja: (Set<String>) -> Unit) {
    var efectivoInicial by remember { mutableStateOf("") }
    var tallarin by remember { mutableStateOf(false) }
    var caldoPollo by remember { mutableStateOf(false) }
    var salchipollo by remember { mutableStateOf(false) }
    var trigoPapa by remember { mutableStateOf(false) }
    var patita by remember { mutableStateOf(false) }
    var orejita by remember { mutableStateOf(false) }
    var padrastro by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(text = "Caja Cerrada", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Sencillo en Caja (Efectivo)", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = efectivoInicial,
                    onValueChange = { efectivoInicial = it },
                    label = { Text("Monto en Soles (S/.)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Text(
            text = "¿Qué se cocina hoy?",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        FilaPlato("Tallarín (S/. 5.00)", tallarin) { tallarin = it }
        FilaPlato("Caldo de Pollo (S/. 5.00)", caldoPollo) { caldoPollo = it }
        FilaPlato("Salchipollo (S/. 10.00)", salchipollo) { salchipollo = it }
        FilaPlato("Trigo con papa (S/. 5.00)", trigoPapa) { trigoPapa = it }
        FilaPlato("Patita (S/. 7.00)", patita) { patita = it }
        FilaPlato("Orejita (S/. 7.00)", orejita) { orejita = it }
        FilaPlato("Padrastro (S/. 7.00)", padrastro) { padrastro = it }

        Button(
            onClick = {
                val activos = mutableSetOf<String>()
                if (tallarin) activos.add("Tallarín")
                if (caldoPollo) activos.add("Caldo de Pollo")
                if (salchipollo) activos.add("Salchipollo")
                if (trigoPapa) activos.add("Trigo con papa")
                if (patita) activos.add("Patita")
                if (orejita) activos.add("Orejita")
                if (padrastro) activos.add("Padrastro")
                alAbrirCaja(activos)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Abrir Caja e Iniciar Día", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun VistaPuntoDeVenta(
    listaClientes: List<Cliente>,
    platosActivos: Set<String>,
    cajaAbierta: Boolean,
    fechaJornada: String?,
    snackbarHostState: SnackbarHostState,
    cajaManager: CajaManager,
    alCerrarCaja: () -> Unit
) {
    var tabSeleccionado by remember { mutableIntStateOf(0) }
    val tabs = listOf("Comida", "Cafetería", "Bebidas")
    var carrito by remember { mutableStateOf(listOf<ItemCarrito>()) }
    val totalMonto = carrito.sumOf { it.total }

    val pedidosJson by cajaManager.pedidosGuardadosFlow.collectAsState(initial = "[]")
    val pedidosGuardados = remember(pedidosJson) {
        try { Json.decodeFromString<List<PedidoGuardado>>(pedidosJson) }
        catch (e: Exception) { emptyList() }
    }

    var mostrarDialogoGuardarPedido by remember { mutableStateOf(false) }
    val ventasRepository = remember { VentasRepository() }
    val coroutineScope = rememberCoroutineScope()
    var mostrarDialogoPago by remember { mutableStateOf(false) }
    var procesandoVenta by remember { mutableStateOf(false) }
    var mostrarDialogoCerrarCaja by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Jornada: ${fechaJornada ?: LocalDate.now().toString()}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            OutlinedButton(
                onClick = { mostrarDialogoCerrarCaja = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) { Text("Cerrar Caja") }
        }

        if (pedidosGuardados.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text("Mesas / Pedidos en espera:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    items(pedidosGuardados) { pedido ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            ),
                            modifier = Modifier.clickable {
                                carrito = pedido.items
                                val nuevaLista = pedidosGuardados.filter { it.id != pedido.id }
                                coroutineScope.launch {
                                    cajaManager.actualizarPedidosGuardados(Json.encodeToString(nuevaLista))
                                }
                            }
                        ) {
                            Text(
                                text = pedido.nombreReferencia,
                                modifier = Modifier.padding(12.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        TabRow(selectedTabIndex = tabSeleccionado) {
            tabs.forEachIndexed { index, titulo ->
                Tab(
                    selected = tabSeleccionado == index,
                    onClick = { tabSeleccionado = index },
                    text = { Text(titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                )
            }
        }

        Box(modifier = Modifier.weight(1f).padding(8.dp)) {
            when (tabSeleccionado) {
                0 -> MenuComida(platosActivos) { item -> carrito = carrito + item }
                1 -> MenuCafeteria { item -> carrito = carrito + item }
                2 -> MenuBebidas { item -> carrito = carrito + item }
            }
        }

        if (carrito.isNotEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pedido Actual",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        OutlinedButton(
                            onClick = { mostrarDialogoGuardarPedido = true },
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Guardar Mesa")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(carrito) { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${item.cantidad}x ${item.nombre}",
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "S/. ${"%.2f".format(item.total)}",
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                IconButton(
                                    onClick = { carrito = carrito.filterIndexed { i, _ -> i != index } },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total: S/. ${"%.2f".format(totalMonto)}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = { mostrarDialogoPago = true },
                            modifier = Modifier.height(50.dp)
                        ) { Text("COBRAR", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                    }
                    TextButton(
                        onClick = { carrito = emptyList() },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) { Text("Vaciar todo el pedido", color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }

    if (mostrarDialogoGuardarPedido) {
        var nombreMesa by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { mostrarDialogoGuardarPedido = false },
            title = { Text("Guardar Pedido en Espera") },
            text = {
                OutlinedTextField(
                    value = nombreMesa,
                    onValueChange = { nombreMesa = it },
                    label = { Text("Nombre o Mesa (Ej: Mesa 4)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    enabled = nombreMesa.isNotBlank(),
                    onClick = {
                        val nuevoPedido = PedidoGuardado(
                            id = UUID.randomUUID().toString(),
                            nombreReferencia = nombreMesa,
                            items = carrito
                        )
                        val nuevaLista = pedidosGuardados + nuevoPedido
                        coroutineScope.launch {
                            cajaManager.actualizarPedidosGuardados(Json.encodeToString(nuevaLista))
                            carrito = emptyList()
                            mostrarDialogoGuardarPedido = false
                            snackbarHostState.showSnackbar("Pedido guardado. Seguro contra cierres.")
                        }
                    }
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoGuardarPedido = false }) { Text("Cancelar") }
            }
        )
    }

    if (mostrarDialogoPago) {
        DialogoPago(
            totalMonto = totalMonto,
            clientes = listaClientes,
            procesandoVenta = procesandoVenta,
            alDescartar = { if (!procesandoVenta) mostrarDialogoPago = false },
            alConfirmar = { metodo, prestaPlato, clienteId ->
                coroutineScope.launch {
                    procesandoVenta = true

                    // ✅ Garantiza que siempre haya una fecha válida
                    val fechaParaRegistro = when {
                        !fechaJornada.isNullOrBlank() -> fechaJornada
                        else -> LocalDate.now().toString()
                    }

                    val estado = if (metodo == "Fiado") "FIADO" else "PAGADO"
                    val textoDetalles = carrito.joinToString(separator = ", ") {
                        "${it.cantidad}x ${it.nombre}"
                    }

                    val nuevaVenta = Venta(
                        montoTotal = totalMonto,
                        costoTotal = 0.0,
                        metodoPago = metodo,
                        estadoPago = estado,
                        prestaPlato = prestaPlato,
                        clienteId = clienteId,
                        fecha = fechaParaRegistro,
                        detalles = textoDetalles
                    )

                    val exitoVenta = ventasRepository.registrarVenta(nuevaVenta)

                    if (exitoVenta) {
                        if (metodo == "Fiado" && clienteId != null) {
                            ventasRepository.registrarFiado(
                                Fiado(clienteId = clienteId, monto = totalMonto, fecha = fechaParaRegistro)
                            )
                        }
                        if (prestaPlato && clienteId != null) {
                            ventasRepository.registrarPlatoPrestado(
                                PlatoPrestado(
                                    clienteId = clienteId,
                                    cantidadPlatos = carrito.sumOf { it.cantidad },
                                    fechaPrestamo = fechaParaRegistro
                                )
                            )
                        }
                        carrito = emptyList()
                        mostrarDialogoPago = false
                        snackbarHostState.showSnackbar("Venta registrada con éxito")
                    } else {
                        snackbarHostState.showSnackbar("Error al guardar. Revisa tu conexión.")
                    }
                    procesandoVenta = false
                }
            }
        )
    }

    if (mostrarDialogoCerrarCaja) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCerrarCaja = false },
            title = { Text("¿Cerrar Caja?") },
            text = {
                Text("¿Seguro que deseas cerrar la jornada de hoy? Se limpiarán los pedidos en espera no cobrados.")
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = { alCerrarCaja(); mostrarDialogoCerrarCaja = false }
                ) { Text("Sí, Cerrar Caja") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCerrarCaja = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun MenuComida(platosActivos: Set<String>, alAgregarAlCarrito: (ItemCarrito) -> Unit) {
    val platosMaestros = listOf(
        "Tallarín" to 5.0, "Caldo de Pollo" to 5.0, "Salchipollo" to 10.0,
        "Trigo con papa" to 5.0, "Patita" to 7.0, "Orejita" to 7.0, "Padrastro" to 7.0
    )
    val platosDeHoy = platosMaestros.filter { platosActivos.contains(it.first) }
    if (platosDeHoy.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay platos seleccionados.", color = MaterialTheme.colorScheme.secondary)
        }
    } else {
        GridProductos(platosDeHoy, alAgregarAlCarrito)
    }
}

@Composable
fun MenuCafeteria(alAgregarAlCarrito: (ItemCarrito) -> Unit) {
    val cafes = listOf(
        "Americano" to 3.0, "Capuchino" to 5.0, "Latte" to 5.0,
        "Frapuchino" to 7.0, "Moccachino" to 5.0, "Caramel Macchiato" to 5.0, "Café Bombón" to 5.0
    )
    GridProductos(cafes, alAgregarAlCarrito)
}

@Composable
fun MenuBebidas(alAgregarAlCarrito: (ItemCarrito) -> Unit) {
    val bebidas = listOf(
        "Maracuyá" to 2.0, "Chicha Morada" to 2.0, "Gordita" to 4.0,
        "Inca 1L" to 5.0, "Coca Cola" to 5.0, "Pepsi 2L" to 6.0
    )
    GridProductos(bebidas, alAgregarAlCarrito)
}

@Composable
fun GridProductos(productos: List<Pair<String, Double>>, alAgregarAlCarrito: (ItemCarrito) -> Unit) {
    var productoSeleccionado by remember { mutableStateOf<Pair<String, Double>?>(null) }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(productos) { producto ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clickable { productoSeleccionado = producto },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = producto.first, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "S/. ${producto.second}", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
    productoSeleccionado?.let { producto ->
        DialogoCobroProducto(
            producto = producto,
            alDescartar = { productoSeleccionado = null },
            alConfirmar = { cantidad, precioFinal ->
                alAgregarAlCarrito(ItemCarrito(producto.first, cantidad, precioFinal))
                productoSeleccionado = null
            }
        )
    }
}

@Composable
fun DialogoCobroProducto(
    producto: Pair<String, Double>,
    alDescartar: () -> Unit,
    alConfirmar: (Int, Double) -> Unit
) {
    var cantidad by remember { mutableIntStateOf(1) }
    var precioEditado by remember { mutableStateOf(producto.second.toString()) }

    AlertDialog(
        onDismissRequest = alDescartar,
        title = { Text(text = producto.first, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cantidad:", fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (cantidad > 1) cantidad-- }) {
                            Icon(Icons.Default.Remove, contentDescription = "Menos")
                        }
                        Text(
                            text = cantidad.toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = { cantidad++ }) {
                            Icon(Icons.Default.Add, contentDescription = "Más")
                        }
                    }
                }
                OutlinedTextField(
                    value = precioEditado,
                    onValueChange = { precioEditado = it },
                    label = { Text("Precio Unitario (S/.)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { alConfirmar(cantidad, precioEditado.toDoubleOrNull() ?: producto.second) }) {
                Text("Confirmar")
            }
        },
        dismissButton = { TextButton(onClick = alDescartar) { Text("Cancelar") } }
    )
}

@Composable
fun FilaPlato(nombre: String, activo: Boolean, onCambio: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = nombre, fontSize = 16.sp)
        Switch(checked = activo, onCheckedChange = onCambio)
    }
}

// ─── DialogoPago con botón Plin agregado ────────────────────────────────────
@Composable
fun DialogoPago(
    totalMonto: Double,
    clientes: List<Cliente>,
    procesandoVenta: Boolean,
    alDescartar: () -> Unit,
    alConfirmar: (metodo: String, prestaPlato: Boolean, clienteId: Int?) -> Unit
) {
    var metodoSeleccionado by remember { mutableStateOf("Efectivo") }
    var prestaPlato by remember { mutableStateOf(false) }
    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }
    var menuExpandido by remember { mutableStateOf(false) }
    val requiereCliente = metodoSeleccionado == "Fiado" || prestaPlato
    val puedeConfirmar = if (requiereCliente) clienteSeleccionado != null else true

    AlertDialog(
        onDismissRequest = alDescartar,
        title = { Text(text = "Finalizar Venta", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Monto a cobrar: S/. ${"%.2f".format(totalMonto)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(text = "Método de Pago:", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                // ✅ Primera fila: Efectivo, Yape, Plin
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BotonMetodoPago("Efectivo", metodoSeleccionado) { if (!procesandoVenta) metodoSeleccionado = it }
                    BotonMetodoPago("Yape", metodoSeleccionado) { if (!procesandoVenta) metodoSeleccionado = it }
                    BotonMetodoPago("Plin", metodoSeleccionado) { if (!procesandoVenta) metodoSeleccionado = it }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ✅ Segunda fila: Fiado (separado para resaltar que es crédito)
                Row(modifier = Modifier.fillMaxWidth()) {
                    BotonMetodoPago("Fiado", metodoSeleccionado) { if (!procesandoVenta) metodoSeleccionado = it }
                    Spacer(Modifier.weight(2f))  // Empuja el botón fiado a la izquierda
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { if (!procesandoVenta) prestaPlato = !prestaPlato }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = prestaPlato,
                        onCheckedChange = { if (!procesandoVenta) prestaPlato = it }
                    )
                    Text(text = "Se presta vajilla al cliente", fontSize = 16.sp)
                }

                if (requiereCliente) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Selecciona al Caserito:",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        OutlinedButton(
                            onClick = { if (!procesandoVenta) menuExpandido = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = clienteSeleccionado?.nombre ?: "Elegir Cliente...")
                        }
                        DropdownMenu(
                            expanded = menuExpandido,
                            onDismissRequest = { menuExpandido = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            clientes.forEach { cliente ->
                                DropdownMenuItem(
                                    text = { Text(cliente.nombre) },
                                    onClick = { clienteSeleccionado = cliente; menuExpandido = false }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { alConfirmar(metodoSeleccionado, prestaPlato, clienteSeleccionado?.id) },
                enabled = puedeConfirmar && !procesandoVenta
            ) {
                if (procesandoVenta) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Confirmar Venta")
                }
            }
        },
        dismissButton = {
            if (!procesandoVenta) TextButton(onClick = alDescartar) { Text("Cancelar") }
        }
    )
}

@Composable
fun RowScope.BotonMetodoPago(metodo: String, metodoActual: String, alSeleccionar: (String) -> Unit) {
    val estaSeleccionado = metodo == metodoActual
    OutlinedButton(
        onClick = { alSeleccionar(metodo) },
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (estaSeleccionado) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface,
            contentColor = if (estaSeleccionado) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(text = metodo, fontSize = 12.sp, maxLines = 1)
    }
}