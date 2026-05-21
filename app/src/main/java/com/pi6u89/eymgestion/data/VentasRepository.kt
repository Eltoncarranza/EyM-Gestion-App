package com.pi6u89.eymgestion.data

import com.pi6u89.eymgestion.domain.Fiado
import com.pi6u89.eymgestion.domain.PlatoPrestado
import com.pi6u89.eymgestion.domain.Venta
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VentasRepository {

    suspend fun registrarVenta(venta: Venta): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["ventas"].insert(venta)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    // NUEVO: Función para registrar la deuda
    suspend fun registrarFiado(fiado: Fiado): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["fiados"].insert(fiado)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    // NUEVO: Función para registrar la vajilla prestada
    suspend fun registrarPlatoPrestado(plato: PlatoPrestado): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["platos_prestados"].insert(plato)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    // Herramienta auxiliar para la fecha (Ej: "2026-05-21")
    fun obtenerFechaHoy(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}