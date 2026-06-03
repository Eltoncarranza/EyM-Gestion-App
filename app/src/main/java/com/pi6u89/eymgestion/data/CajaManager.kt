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
        val PLATOS_ACTIVOS_KEY = stringSetPreferencesKey("platos_activos")
        // 👈 NUEVA LLAVE PARA PROTEGER LOS PEDIDOS
        val PEDIDOS_GUARDADOS_KEY = stringPreferencesKey("pedidos_guardados")
    }

    val cajaAbiertaFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[CAJA_ABIERTA_KEY] ?: false
    }

    val fechaJornadaFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[FECHA_JORNADA_KEY]
    }

    val platosActivosFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PLATOS_ACTIVOS_KEY] ?: emptySet()
    }

    // 👈 NUEVO FLUJO PARA LEER LOS PEDIDOS GUARDADOS DESDE EL DISCO DURO
    val pedidosGuardadosFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PEDIDOS_GUARDADOS_KEY] ?: "[]"
    }

    suspend fun abrirCaja(platos: Set<String>, fecha: String) {
        context.dataStore.edit { preferences ->
            preferences[CAJA_ABIERTA_KEY] = true
            preferences[FECHA_JORNADA_KEY] = fecha
            preferences[PLATOS_ACTIVOS_KEY] = platos
        }
    }

    // 👈 NUEVA FUNCIÓN PARA ACTUALIZAR LOS PEDIDOS EN EL DISCO DURO
    suspend fun actualizarPedidosGuardados(pedidosJson: String) {
        context.dataStore.edit { preferences ->
            preferences[PEDIDOS_GUARDADOS_KEY] = pedidosJson
        }
    }

    suspend fun cerrarCaja() {
        context.dataStore.edit { preferences ->
            preferences[CAJA_ABIERTA_KEY] = false
            preferences.remove(FECHA_JORNADA_KEY)
            preferences.remove(PLATOS_ACTIVOS_KEY)
            preferences.remove(PEDIDOS_GUARDADOS_KEY) // 👈 SE LIMPIAN SOLO AL CERRAR CAJA
        }
    }
}