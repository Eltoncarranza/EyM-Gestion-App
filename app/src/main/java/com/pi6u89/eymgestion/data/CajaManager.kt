package com.pi6u89.eymgestion.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "caja_prefs")

class CajaManager(private val context: Context) {
    companion object {
        val CAJA_ABIERTA_KEY = booleanPreferencesKey("caja_abierta")
        val FECHA_JORNADA_KEY = stringPreferencesKey("fecha_jornada")
        val PLATOS_ACTIVOS_KEY = stringSetPreferencesKey("platos_activos") // ¡Restaurado!
    }

    val cajaAbiertaFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[CAJA_ABIERTA_KEY] ?: false
    }

    val fechaJornadaFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[FECHA_JORNADA_KEY]
    }

    // ¡Restaurado!
    val platosActivosFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PLATOS_ACTIVOS_KEY] ?: emptySet()
    }

    // Modificado: Ahora al abrir caja se guardan los platos y la fecha de la jornada
    suspend fun abrirCaja(platos: Set<String>, fecha: String) {
        context.dataStore.edit { preferences ->
            preferences[CAJA_ABIERTA_KEY] = true
            preferences[FECHA_JORNADA_KEY] = fecha
            preferences[PLATOS_ACTIVOS_KEY] = platos
        }
    }

    suspend fun cerrarCaja() {
        context.dataStore.edit { preferences ->
            preferences[CAJA_ABIERTA_KEY] = false
            preferences.remove(FECHA_JORNADA_KEY)
            preferences.remove(PLATOS_ACTIVOS_KEY) // Limpia los platos de ayer
        }
    }
}