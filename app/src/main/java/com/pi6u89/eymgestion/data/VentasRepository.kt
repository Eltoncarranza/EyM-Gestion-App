package com.pi6u89.eymgestion.data

import android.util.Log
import com.pi6u89.eymgestion.domain.Fiado
import com.pi6u89.eymgestion.domain.PlatoPrestado
import com.pi6u89.eymgestion.domain.Venta
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VentasRepository {
    suspend fun registrarVenta(venta: Venta): Boolean {
        return withContext(Dispatchers.IO) {
            try { SupabaseClient.client.postgrest["ventas"].insert(venta); true } catch (e: Exception) { false }
        }
    }

    suspend fun registrarFiado(fiado: Fiado): Boolean {
        return withContext(Dispatchers.IO) {
            try { SupabaseClient.client.postgrest["fiados"].insert(fiado); true } catch (e: Exception) { false }
        }
    }

    suspend fun registrarPlatoPrestado(plato: PlatoPrestado): Boolean {
        return withContext(Dispatchers.IO) {
            try { SupabaseClient.client.postgrest["platos_prestados"].insert(plato); true } catch (e: Exception) { false }
        }
    }

    // 👈 NUEVA FUNCIÓN PARA EL DASHBOARD FINANCIERO
    suspend fun obtenerTodasLasVentas(): List<Venta> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["ventas"].select().decodeList<Venta>()
            } catch (e: Exception) { emptyList() }
        }
    }
}