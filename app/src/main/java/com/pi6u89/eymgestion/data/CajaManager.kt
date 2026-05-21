package com.pi6u89.eymgestion.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Creamos la extensión de la base de datos local de configuración
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "caja_preferencias")

class CajaManager(private val context: Context) {

    companion object {
        // Llaves para almacenar los datos
        private val CAJA_ABIERTA_KEY = booleanPreferencesKey("caja_abierta")
        private val FECHA_APERTURA_KEY = stringPreferencesKey("fecha_apertura")
        // 👇 NUEVA LLAVE: Para recordar los platos que se cocinaron hoy
        private val PLATOS_ACTIVOS_KEY = stringSetPreferencesKey("platos_activos")
    }

    // Flujo (Flow) que verifica en tiempo real si la caja está abierta HOY
    val cajaAbiertaFlow: Flow<Boolean> = context.dataStore.data.map { preferencias ->
        val estaAbierta = preferencias[CAJA_ABIERTA_KEY] ?: false
        val fechaGuardada = preferencias[FECHA_APERTURA_KEY] ?: ""
        val fechaHoy = obtenerFechaHoy()

        estaAbierta && fechaGuardada == fechaHoy
    }

    // 👇 NUEVO: Flujo que devuelve la lista de platos marcados en la mañana
    val platosActivosFlow: Flow<Set<String>> = context.dataStore.data.map { preferencias ->
        preferencias[PLATOS_ACTIVOS_KEY] ?: emptySet()
    }

    // Guarda en el almacenamiento que la caja se abrió el día de hoy Y qué platos hay
    suspend fun abrirCaja(platosSeleccionados: Set<String>) {
        context.dataStore.edit { preferencias ->
            preferencias[CAJA_ABIERTA_KEY] = true
            preferencias[FECHA_APERTURA_KEY] = obtenerFechaHoy()
            preferencias[PLATOS_ACTIVOS_KEY] = platosSeleccionados // Guardamos los platos
        }
    }

    // Cierra la caja y limpia los platos de hoy
    suspend fun cerrarCaja() {
        context.dataStore.edit { preferencias ->
            preferencias[CAJA_ABIERTA_KEY] = false
            preferencias[FECHA_APERTURA_KEY] = ""
            preferencias[PLATOS_ACTIVOS_KEY] = emptySet() // Limpiamos la lista
        }
    }

    // Función auxiliar para obtener la fecha actual en texto (Ej: "2026-05-21")
    private fun obtenerFechaHoy(): String {
        val formateador = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formateador.format(Date())
    }
}