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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AperturaScreen() {
    // Variables para guardar lo que escribes y seleccionas en la pantalla
    var efectivoInicial by remember { mutableStateOf("") }

    // Estados de los platos especiales (Falso = No se cocinó hoy)
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

            // 2. SECCIÓN DEL MENÚ DEL DÍA
            Text(text = "¿Qué se cocina hoy?", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            // Platos fijos (Solo lectura, siempre están)
            Text(text = "🍲 Platos Fijos (Siempre activos)", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
            Text(text = "• Tallarín (S/. 5.00)\n• Caldo de Pollo (S/. 5.00)\n• Salchipollo (S/. 5.00)\n• Trigo con papa (S/. 5.00)", modifier = Modifier.padding(start = 8.dp))

            Divider()

            // Platos especiales (Interruptores)
            Text(text = "✨ Especiales del Día", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)

            FilaEspecial("Patita (S/. 7.00)", patita) { patita = it }
            FilaEspecial("Orejita (S/. 7.00)", orejita) { orejita = it }
            FilaEspecial("Padrastro (S/. 7.00)", padrastro) { padrastro = it }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. BOTÓN PARA INICIAR
            Button(
                onClick = {
                    // Aquí luego pondremos la lógica para guardar en Supabase
                    println("Iniciando con S/. $efectivoInicial, Patita: $patita, Orejita: $orejita, Padrastro: $padrastro")
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

// Un pequeño componente reutilizable para que el código quede limpio
@Composable
fun FilaEspecial(nombre: String, activo: Boolean, onCambio: (Boolean) -> Unit) {
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