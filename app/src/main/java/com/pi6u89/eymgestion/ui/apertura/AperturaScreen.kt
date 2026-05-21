package com.pi6u89.eymgestion.ui.apertura

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
import androidx.navigation.NavController
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AperturaScreen(navController: NavController) {
    // Variable para el dinero de caja
    var efectivoInicial by remember { mutableStateOf("") }

    // Estados de TODOS los platos (Falso por defecto = apagado hasta que lo marques)
    var tallarin by remember { mutableStateOf(false) }
    var caldoPollo by remember { mutableStateOf(false) }
    var salchipollo by remember { mutableStateOf(false) }
    var trigoPapa by remember { mutableStateOf(false) }
    var patita by remember { mutableStateOf(false) }
    var orejita by remember { mutableStateOf(false) }
    var padrastro by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Apertura de Caja - E&M", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // 1. SECCIÓN DE DINERO INICIAL
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

            // 2. SECCIÓN DEL MENÚ DEL DÍA (Todos opcionales)
            Text(text = "¿Qué se cocina hoy?", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Text(text = "🍲 Activa los platos disponibles", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)

            FilaPlato("Tallarín (S/. 5.00)", tallarin) { tallarin = it }
            FilaPlato("Caldo de Pollo (S/. 5.00)", caldoPollo) { caldoPollo = it }
            FilaPlato("Salchipollo (S/. 5.00)", salchipollo) { salchipollo = it }
            FilaPlato("Trigo con papa (S/. 5.00)", trigoPapa) { trigoPapa = it }
            FilaPlato("Patita (S/. 7.00)", patita) { patita = it }
            FilaPlato("Orejita (S/. 7.00)", orejita) { orejita = it }
            FilaPlato("Padrastro (S/. 7.00)", padrastro) { padrastro = it }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. BOTÓN PARA INICIAR
            Button(
                onClick = {
                    println("Iniciando con S/. $efectivoInicial")
                    // Viaja a la pantalla de venta y elimina la de apertura del historial
                    // para que si el usuario le da "Atrás", no vuelva a abrir la caja.
                    navController.navigate("venta") {
                        popUpTo("apertura") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Iniciar Jornada", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Componente reutilizable para las filas de platos
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