package com.pi6u89.eymgestion.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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
    }

    // Flujo (Flow) que verifica en tiempo real si la caja está abierta HOY
    val cajaAbiertaFlow: Flow<Boolean> = context.dataStore.data.map { preferencias ->
        val estaAbierta = preferencias[CAJA_ABIERTA_KEY] ?: false
        val fechaGuardada = preferencias[FECHA_APERTURA_KEY] ?: ""
        val fechaHoy = obtenerFechaHoy()

        // La caja está verdaderamente abierta si el interruptor es true Y corresponde a la fecha de hoy
        estaAbierta && fechaGuardada == fechaHoy
    }

    // Guarda en el almacenamiento que la caja se abrió el día de hoy
    suspend fun abrirCaja() {
        context.dataStore.edit { preferencias ->
            preferencias[CAJA_ABIERTA_KEY] = true
            preferencias[FECHA_APERTURA_KEY] = obtenerFechaHoy()
        }
    }

    // Cierra la caja (se usará en el módulo de reportes más adelante)
    suspend fun cerrarCaja() {
        context.dataStore.edit { preferencias ->
            preferencias[CAJA_ABIERTA_KEY] = false
            preferencias[FECHA_APERTURA_KEY] = ""
        }
    }

    // Función auxiliar para obtener la fecha actual en texto (Ej: "2026-05-21")
    private fun obtenerFechaHoy(): String {
        val formateador = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formateador.format(Date())
    }
}