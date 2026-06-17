package com.pi6u89.eymgestion.data

import android.util.Log
import com.pi6u89.eymgestion.domain.ItemCompra
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ComprasRepository {

    suspend fun obtenerListaCompras(): List<ItemCompra> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client.postgrest["lista_compras"].select().decodeList<ItemCompra>()
        } catch (e: Exception) {
            Log.e("ComprasRepo", "Error al obtener compras: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun marcarComoComprado(id: Int, nuevoCosto: Double, fechaHoy: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["lista_compras"].update({
                    set("comprado", true)
                    set("costo", nuevoCosto)
                    set("fecha", fechaHoy)
                }) { filter { eq("id", id) } }
                true
            } catch (e: Exception) {
                Log.e("ComprasRepo", "Error al marcar comprado id=$id: ${e.message}", e)
                false
            }
        }

    suspend fun agregarItem(item: ItemCompra): Boolean = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client.postgrest["lista_compras"].insert(item)
            true
        } catch (e: Exception) {
            Log.e("ComprasRepo", "Error al agregar item: ${e.message}", e)
            false
        }
    }

    suspend fun eliminarItem(id: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client.postgrest["lista_compras"].delete { filter { eq("id", id) } }
            true
        } catch (e: Exception) {
            Log.e("ComprasRepo", "Error al eliminar id=$id: ${e.message}", e)
            false
        }
    }
}