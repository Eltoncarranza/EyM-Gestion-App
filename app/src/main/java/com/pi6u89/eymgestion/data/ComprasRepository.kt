package com.pi6u89.eymgestion.data

import com.pi6u89.eymgestion.domain.ItemCompra
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ComprasRepository {

    suspend fun obtenerListaCompras(): List<ItemCompra> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["lista_compras"]
                    .select()
                    .decodeList<ItemCompra>()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    suspend fun agregarItem(item: ItemCompra): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["lista_compras"].insert(item)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun marcarComoComprado(id: Int, costoFinal: Double): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["lista_compras"]
                    .update(mapOf("comprado" to true, "costo" to costoFinal)) {
                        filter { eq("id", id) }
                    }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun eliminarItem(id: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["lista_compras"]
                    .delete { filter { eq("id", id) } }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun obtenerGastosDeHoy(): List<ItemCompra> {
        val hoy = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["lista_compras"]
                    .select {
                        filter {
                            eq("fecha", hoy)
                            eq("comprado", true) // Solo lo que ya se pagó en el mercado
                        }
                    }
                    .decodeList<ItemCompra>()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
