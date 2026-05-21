package com.pi6u89.eymgestion.ui.venta

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
import androidx.compose.runtime.mutableIntStateOf
@Composable
fun VentaScreen() {
    val contexto = LocalContext.current
    // Inicializamos el administrador de la caja
    val cajaManager = remember { CajaManager(contexto) }

    // Recolectamos el estado asíncrono de DataStore. Por defecto arranca en false.
    val cajaAbierta by cajaManager.cajaAbiertaFlow.collectAsState(initial = false)

    // Scope necesario para lanzar funciones de guardado en segundo plano
    val coroutineScope = rememberCoroutineScope()

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
    // Controla qué pestaña está activa (0=Comida, 1=Cafetería, 2=Bebidas)
    var tabSeleccionado by remember { mutableIntStateOf(0) }
    val tabs = listOf("Comida", "Cafetería", "Bebidas")

    Column(modifier = Modifier.fillMaxSize()) {
        // La barra de pestañas superior
        TabRow(selectedTabIndex = tabSeleccionado) {
            tabs.forEachIndexed { index, titulo ->
                Tab(
                    selected = tabSeleccionado == index,
                    onClick = { tabSeleccionado = index },
                    text = { Text(titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                )
            }
        }

        // El contenido que cambia según la pestaña
        Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            when (tabSeleccionado) {
                0 -> MenuComida()
                1 -> MenuCafeteria()
                2 -> MenuBebidas()
            }
        }
    }
}

@Composable
fun MenuComida() {
    // Por ahora pondremos datos estáticos. Luego los traeremos de Supabase según lo que marcaste en la mañana.
    val platos = listOf("Tallarín" to 5.0, "Caldo de Pollo" to 5.0, "Patita" to 7.0)
    GridProductos(platos)
}

@Composable
fun MenuCafeteria() {
    val cafes = listOf(
        "Americano" to 3.0,
        "Capuchino" to 5.0,
        "Latte" to 5.0,
        "Frapuchino" to 7.0,
        "Espresso Martini" to 10.0
    )
    GridProductos(cafes)
}

@Composable
fun MenuBebidas() {
    val bebidas = listOf(
        "Gordita Inca Kola" to 3.5,
        "Inca 1L" to 5.0,
        "Pepsi 2L" to 6.0,
        "Chicha Morada" to 2.5
    )
    GridProductos(bebidas)
}

// Componente reutilizable que dibuja los botones cuadrados de los productos
@Composable
fun GridProductos(productos: List<Pair<String, Double>>) {
    // Variable para saber qué producto se tocó. Si es null, la ventana se oculta.
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
                    .clickable {
                        // Al tocar la tarjeta, guardamos el producto y se abre el Pop-up
                        productoSeleccionado = producto
                    },
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

    // Si hay un producto seleccionado, dibujamos la ventana emergente en pantalla
    productoSeleccionado?.let { producto ->
        DialogoCobroProducto(
            producto = producto,
            alDescartar = { productoSeleccionado = null }, // Cierra la ventana
            alConfirmar = { cantidad, precioFinal ->
                // Aquí calcularemos el total y pasaremos a los métodos de pago (Efectivo, Yape, Fiado)
                val total = cantidad * precioFinal
                println("LISTO PARA COBRAR: ${producto.first} x$cantidad = S/. $total (Precio modificado: $precioFinal)")

                // Cerramos la ventana después de confirmar
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