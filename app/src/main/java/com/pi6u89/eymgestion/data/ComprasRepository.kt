package com.pi6u89.eymgestion.data

import android.util.Log
import com.pi6u89.eymgestion.domain.ItemCompra
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ComprasRepository {
    private val TAG = "DEBUG_COMPRAS"

    suspend fun obtenerListaCompras(): List<ItemCompra> {
        return withContext(Dispatchers.IO) {
            try {
                // Asegúrate de que el nombre aquí "lista_compras" coincida EXACTAMENTE con tu tabla en Supabase
                SupabaseClient.client.postgrest["lista_compras"]
                    .select()
                    .decodeList<ItemCompra>()
            } catch (e: Exception) {
                Log.e(TAG, "Error en SELECT: ${e.message}")
                emptyList()
            }
        }
    }

    suspend fun marcarComoComprado(id: Int, nuevoCosto: Double): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Intentando actualizar ID: $id con Costo: $nuevoCosto")

                // Ejecutamos el update
                val result = SupabaseClient.client.postgrest["lista_compras"]
                    .update({
                        set("comprado", true)
                        set("costo", nuevoCosto)
                    }) {
                        filter { eq("id", id) }
                    }

                Log.d(TAG, "Resultado de actualización: Exitoso")
                true
            } catch (e: Exception) {
                Log.e(TAG, "ERROR FATAL EN UPDATE: ${e.message}")
                false
            }
        }
    }

    suspend fun agregarItem(item: ItemCompra): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["lista_compras"].insert(item)
                true
            } catch (e: Exception) {
                Log.e(TAG, "ERROR FATAL EN INSERT: ${e.message}")
                false
            }
        }
    }

    fun eliminarItem(id: Int) {}
}