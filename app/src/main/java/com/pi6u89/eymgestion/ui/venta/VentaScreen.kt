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

@Composable
fun VentaScreen() {
    // Esta variable es el "switch" maestro.
    // Si es false, muestra la configuración. Si es true, muestra el menú para vender.
    var cajaAbierta by remember { mutableStateOf(false) }

    if (!cajaAbierta) {
        VistaApertura(alAbrirCaja = { cajaAbierta = true })
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
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("🛒 Caja Abierta. ¡A vender!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
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