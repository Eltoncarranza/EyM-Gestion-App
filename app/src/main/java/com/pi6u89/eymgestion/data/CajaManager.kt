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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "caja_preferencias")

class CajaManager(private val context: Context) {

    companion object {
        private val CAJA_ABIERTA_KEY = booleanPreferencesKey("caja_abierta")
        private val FECHA_APERTURA_KEY = stringPreferencesKey("fecha_apertura")
        private val PLATOS_ACTIVOS_KEY = stringSetPreferencesKey("platos_activos")
    }

    val cajaAbiertaFlow: Flow<Boolean> = context.dataStore.data.map { preferencias ->
        val estaAbierta = preferencias[CAJA_ABIERTA_KEY] ?: false
        val fechaGuardada = preferencias[FECHA_APERTURA_KEY] ?: ""
        val fechaHoy = obtenerFechaHoy()

        estaAbierta && fechaGuardada == fechaHoy
    }

    val platosActivosFlow: Flow<Set<String>> = context.dataStore.data.map { preferencias ->
        preferencias[PLATOS_ACTIVOS_KEY] ?: emptySet()
    }

    suspend fun abrirCaja(platosSeleccionados: Set<String>) {
        context.dataStore.edit { preferencias ->
            preferencias[CAJA_ABIERTA_KEY] = true
            preferencias[FECHA_APERTURA_KEY] = obtenerFechaHoy()
            preferencias[PLATOS_ACTIVOS_KEY] = platosSeleccionados
        }
    }

    suspend fun cerrarCaja() {
        context.dataStore.edit { preferencias ->
            preferencias[CAJA_ABIERTA_KEY] = false
            preferencias[FECHA_APERTURA_KEY] = ""
            preferencias[PLATOS_ACTIVOS_KEY] = emptySet()
        }
    }

    private fun obtenerFechaHoy(): String {
        val formateador = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formateador.format(Date())
    }
}