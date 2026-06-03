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
                SupabaseClient.client.postgrest["lista_compras"].select().decodeList<ItemCompra>()
            } catch (e: Exception) { emptyList() }
        }
    }

    // 👈 AHORA RECIBE LA FECHA DE HOY
    suspend fun marcarComoComprado(id: Int, nuevoCosto: Double, fechaHoy: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["lista_compras"]
                    .update({
                        set("comprado", true)
                        set("costo", nuevoCosto)
                        set("fecha", fechaHoy) // 👈 SE GUARDA LA FECHA EN BASE DE DATOS
                    }) { filter { eq("id", id) } }
                true
            } catch (e: Exception) { false }
        }
    }

    suspend fun agregarItem(item: ItemCompra): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["lista_compras"].insert(item)
                true
            } catch (e: Exception) { false }
        }
    }

    suspend fun eliminarItem(id: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["lista_compras"].delete { filter { eq("id", id) } }
                true
            } catch (e: Exception) { false }
        }
    }
}