package com.pi6u89.eymgestion.ui.venta

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.pi6u89.eymgestion.data.CajaManager
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.mutableIntStateOf
import com.pi6u89.eymgestion.data.VentasRepository
import com.pi6u89.eymgestion.domain.Venta
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.pi6u89.eymgestion.data.ClienteRepository
import com.pi6u89.eymgestion.domain.Cliente
import com.pi6u89.eymgestion.domain.Fiado
import com.pi6u89.eymgestion.domain.PlatoPrestado

// Representa un producto que ya fue agregado al pedido actual
data class ItemCarrito(
    val nombre: String,
    val cantidad: Int,
    val precioUnitario: Double
) {
    val total: Double get() = cantidad * precioUnitario
}

@Composable
fun VentaScreen() {
    val contexto = LocalContext.current
    // Inicializamos el administrador de la caja
    val cajaManager = remember { CajaManager(contexto) }

    // Recolectamos el estado asíncrono de DataStore. Por defecto arranca en false.
    val cajaAbierta by cajaManager.cajaAbiertaFlow.collectAsState(initial = false)

    // Scope necesario para lanzar funciones de guardado en segundo plano
    val coroutineScope = rememberCoroutineScope()
    val clienteRepository = remember { ClienteRepository() }
    var listaClientes by remember { mutableStateOf<List<Cliente>>(emptyList()) }

    // Descarga los clientes de Supabase apenas se abre la pantalla de ventas
    LaunchedEffect(Unit) {
        listaClientes = clienteRepository.obtenerClientes()
    }
    if (!cajaAbierta) {
        VistaApertura(
            alAbrirCaja = {
                // Ejecutamos la escritura en disco de forma segura dentro de una corrutina
                coroutineScope.launch {
                    cajaManager.abrirCaja()
                }
            }
        )
    } else {
        VistaPuntoDeVenta()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaApertura(alAbrirCaja: () -> Unit) {
    var efectivoInicial by remember { mutableStateOf("") }

    // Estados de los platos
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

        Text(text = "🍲 ¿Qué se cocina hoy?", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        FilaPlato("Tallarín (S/. 5.00)", tallarin) { tallarin = it }
        FilaPlato("Caldo de Pollo (S/. 5.00)", caldoPollo) { caldoPollo = it }
        FilaPlato("Salchipollo (S/. 5.00)", salchipollo) { salchipollo = it }
        FilaPlato("Trigo con papa (S/. 5.00)", trigoPapa) { trigoPapa = it }
        FilaPlato("Patita (S/. 7.00)", patita) { patita = it }
        FilaPlato("Orejita (S/. 7.00)", orejita) { orejita = it }
        FilaPlato("Padrastro (S/. 7.00)", padrastro) { padrastro = it }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = alAbrirCaja, // Aquí cambia el estado de la pantalla
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Abrir Caja e Iniciar Día", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// Aquí diseñaremos las pestañas de Comida, Cafetería y Bebidas en el próximo paso
@Composable
fun VistaPuntoDeVenta() {
    var tabSeleccionado by remember { mutableIntStateOf(0) }
    val tabs = listOf("Comida", "Cafetería", "Bebidas")

    // Lista del carrito de compras
    var carrito by remember { mutableStateOf(listOf<ItemCarrito>()) }
    val totalMonto = carrito.sumOf { it.total }

    val ventasRepository = remember { VentasRepository() }
    val coroutineScope = rememberCoroutineScope()
    var mostrarDialogoPago by remember { mutableStateOf(false) }

    // 👇 ESTAS DOS LÍNEAS DEBEN ESTAR AQUÍ ADENTRO
    val clienteRepository = remember { ClienteRepository() }
    var listaClientes by remember { mutableStateOf<List<Cliente>>(emptyList()) }

    Column(modifier = Modifier.fillMaxSize()) {
        // ... el resto de tu código continúa igual
        // 1. Barra de pestañas superior
        TabRow(selectedTabIndex = tabSeleccionado) {
            tabs.forEachIndexed { index, titulo ->
                Tab(
                    selected = tabSeleccionado == index,
                    onClick = { tabSeleccionado = index },
                    text = { Text(titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                )
            }
        }

        // 2. Cuadrícula de productos (Ocupa el espacio disponible)
        Box(modifier = Modifier.weight(1f).padding(8.dp)) {
            when (tabSeleccionado) {
                0 -> MenuComida { item -> carrito = carrito + item }
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
                    Text(
                        text = "Pedido Actual",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Lista deslizable de los productos agregados
                    // Limitamos su altura máxima a 140dp para que no tape toda la pantalla
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Usamos itemsIndexed para saber la posición exacta de cada producto
                        itemsIndexed(carrito) { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Muestra: "2x Tallarín"
                                Text(
                                    text = "${item.cantidad}x ${item.nombre}",
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                // Muestra el subtotal de ese producto (Ej: "S/. 10.0")
                                Text(
                                    text = "S/. ${item.total}",
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                // BOTÓN DE ELIMINACIÓN INDIVIDUAL
                                IconButton(
                                    onClick = {
                                        // Filtramos la lista para quitar SOLAMENTE el elemento de este índice
                                        carrito = carrito.filterIndexed { i, _ -> i != index }
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Eliminar producto",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))
// Fila final con el total y el botón de cobrar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Total: S/. $totalMonto", fontSize = 22.sp, fontWeight = FontWeight.Bold)

                        // 👇 ESTE ES EL BOTÓN QUE SE MODIFICA
                        Button(
                            onClick = {
                                // Lanzamos la corrutina para traer los clientes antes de abrir la ventana
                                coroutineScope.launch {
                                    listaClientes = clienteRepository.obtenerClientes()
                                    mostrarDialogoPago = true
                                }
                            },
                            modifier = Modifier.height(50.dp)
                        ) {
                            Text("COBRAR", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    TextButton(
                        onClick = { carrito = emptyList() },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Vaciar todo el pedido", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
    // Lógica para mostrar la ventana de pago final
    if (mostrarDialogoPago) {
        DialogoPago(
            totalMonto = totalMonto,
            clientes = listaClientes, // <--- Pasamos la lista nueva
            alDescartar = { mostrarDialogoPago = false },
            alConfirmar = { metodo, prestaPlato, clienteId ->
                val nuevaVenta = Venta(
                    montoTotal = totalMonto,
                    metodoPago = metodo,
                    prestaPlato = prestaPlato,
                    clienteId = clienteId
                )

                coroutineScope.launch {
                    // 1. Guardamos la venta principal
                    val exitoVenta = ventasRepository.registrarVenta(nuevaVenta)

                    if (exitoVenta) {
                        println("¡Venta general guardada en Supabase!")

                        // 2. Si el método es Fiado, registramos la deuda en su tabla
                        if (metodo == "Fiado" && clienteId != null) {
                            val nuevoFiado = Fiado(
                                clienteId = clienteId,
                                monto = totalMonto,
                                fecha = ventasRepository.obtenerFechaHoy()
                            )
                            ventasRepository.registrarFiado(nuevoFiado)
                            println("¡Deuda de S/. $totalMonto asignada al cliente!")
                        }

                        // 3. Si se prestó plato, registramos la cantidad de platos llevados
                        if (prestaPlato && clienteId != null) {
                            val nuevoPlato = PlatoPrestado(
                                clienteId = clienteId,
                                // Sumamos todas las cantidades del carrito para saber cuántos platos se llevó
                                cantidadPlatos = carrito.sumOf { it.cantidad },
                                fechaPrestamo = ventasRepository.obtenerFechaHoy()
                            )
                            ventasRepository.registrarPlatoPrestado(nuevoPlato)
                            println("¡Vajilla registrada al cliente!")
                        }
                    }
                }

                // Limpiamos la pantalla
                carrito = emptyList()
                mostrarDialogoPago = false
            }
        )
    }
}

@Composable
fun MenuComida(alAgregarAlCarrito: (ItemCarrito) -> Unit) {
    val platos = listOf("Tallarín" to 5.0, "Caldo de Pollo" to 5.0, "Patita" to 7.0)
    GridProductos(platos, alAgregarAlCarrito)
}

@Composable
fun MenuCafeteria(alAgregarAlCarrito: (ItemCarrito) -> Unit) {
    val cafes = listOf("Americano" to 3.0, "Capuchino" to 5.0, "Latte" to 5.0, "Frapuchino" to 7.0, "Espresso Martini" to 10.0)
    GridProductos(cafes, alAgregarAlCarrito)
}

@Composable
fun MenuBebidas(alAgregarAlCarrito: (ItemCarrito) -> Unit) {
    val bebidas = listOf("Gordita Inca Kola" to 3.5, "Inca 1L" to 5.0, "Pepsi 2L" to 6.0, "Chicha Morada" to 2.5)
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
                    .fillMaxWidth().height(100.dp)
                    .clickable { productoSeleccionado = producto },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
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
                // AQUÍ ES LA MAGIA: Creamos el item y lo mandamos al carrito
                val nuevoItem = ItemCarrito(producto.first, cantidad, precioFinal)
                alAgregarAlCarrito(nuevoItem)

                productoSeleccionado = null
            }
        )
    }
}
@Composable
fun DialogoCobroProducto(
    producto: Pair<String, Double>,
    alDescartar: () -> Unit,
    alConfirmar: (Int, Double) -> Unit // Devuelve la cantidad y el precio final modificado
) {
    var cantidad by remember { mutableIntStateOf(1) }
    // Iniciamos el campo de texto con el precio sugerido del producto
    var precioEditado by remember { mutableStateOf(producto.second.toString()) }

    AlertDialog(
        onDismissRequest = alDescartar,
        title = { Text(text = producto.first, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Fila para controlar la cantidad
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

                // Campo de texto para modificar el precio
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
            Button(onClick = {
                // Convertimos el texto a número. Si el usuario dejó vacío, usamos el precio original.
                val precioFinal = precioEditado.toDoubleOrNull() ?: producto.second
                alConfirmar(cantidad, precioFinal)
            }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = alDescartar) {
                Text("Cancelar")
            }
        }
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
        Switch(
            checked = activo,
            onCheckedChange = onCambio
        )
    }
}
@Composable
fun DialogoPago(
    totalMonto: Double,
    clientes: List<Cliente>, // Recibe la lista
    alDescartar: () -> Unit,
    alConfirmar: (metodo: String, prestaPlato: Boolean, clienteId: Int?) -> Unit // Devuelve el ID
) {
    var metodoSeleccionado by remember { mutableStateOf("Efectivo") }
    var prestaPlato by remember { mutableStateOf(false) }

    // Variables para el menú de clientes
    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }
    var menuExpandido by remember { mutableStateOf(false) }

    // ¿Se necesita elegir un cliente obligatoriamente?
    val requiereCliente = metodoSeleccionado == "Fiado" || prestaPlato

    // ¿El botón de confirmar debe estar habilitado?
    val puedeConfirmar = if (requiereCliente) clienteSeleccionado != null else true

    AlertDialog(
        onDismissRequest = alDescartar,
        title = { Text(text = "Finalizar Venta", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Monto a cobrar: S/. $totalMonto",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(text = "Método de Pago:", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BotonMetodoPago("Efectivo", metodoSeleccionado) { metodoSeleccionado = it }
                    BotonMetodoPago("Yape/Plin", metodoSeleccionado) { metodoSeleccionado = it }
                    BotonMetodoPago("Fiado", metodoSeleccionado) { metodoSeleccionado = it }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { prestaPlato = !prestaPlato }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(checked = prestaPlato, onCheckedChange = { prestaPlato = it })
                    Text(text = "Se presta vajilla al cliente", fontSize = 16.sp)
                }

                // MAGIA: El selector aparece solo si es Fiado o si se presta vajilla
                if (requiereCliente) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Selecciona al Caserito:", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)

                    Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        OutlinedButton(
                            onClick = { menuExpandido = true },
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
                                    onClick = {
                                        clienteSeleccionado = cliente
                                        menuExpandido = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    alConfirmar(metodoSeleccionado, prestaPlato, clienteSeleccionado?.id)
                },
                enabled = puedeConfirmar // Se bloquea si falta elegir cliente
            ) {
                Text("Confirmar Venta")
            }
        },
        dismissButton = {
            TextButton(onClick = alDescartar) { Text("Cancelar") }
        }
    )
}

// Sub-componente para hacer que los botones de pago cambien de color al tocarlos
@Composable
fun RowScope.BotonMetodoPago(
    metodo: String,
    metodoActual: String,
    alSeleccionar: (String) -> Unit
) {
    val estaSeleccionado = metodo == metodoActual

    OutlinedButton(
        onClick = { alSeleccionar(metodo) },
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (estaSeleccionado) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            contentColor = if (estaSeleccionado) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(text = metodo, fontSize = 12.sp, maxLines = 1)
    }
}