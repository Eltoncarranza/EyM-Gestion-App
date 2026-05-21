package com.pi6u89.eymgestion.data

import com.pi6u89.eymgestion.domain.ItemCompra
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ComprasRepository {

    // Descarga toda la lista de compras del día o histórico
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

    // Agrega un insumo que falta para el día siguiente
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

    // Registra el costo final y cambia el estado a comprado = true
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

    // Elimina un producto de la lista
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

    // Descarga solo los gastos confirmados el día de hoy
    suspend fun obtenerGastosDeHoy(): List<ItemCompra> {
        val hoy = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["lista_compras"]
                    .select {
                        filter {
                            eq("fecha", hoy)
                            eq("comprado", true) // Solo lo que sí se compró y gastó
                        }
                    }
                    .decodeList<ItemCompra>()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
}