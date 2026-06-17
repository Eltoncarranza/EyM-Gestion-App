package com.pi6u89.eymgestion.data

import android.util.Log
import com.pi6u89.eymgestion.domain.Fiado
import com.pi6u89.eymgestion.domain.PlatoPrestado
import com.pi6u89.eymgestion.domain.Venta
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VentasRepository {

    suspend fun registrarVenta(venta: Venta): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["ventas"].insert(venta)
                true
            } catch (e: Exception) {
                // Log útil para depurar en Logcat: filtra por "VentasRepo"
                Log.e("VentasRepo", "Error al registrar venta: ${e.message}", e)
                false
            }
        }
    }

    suspend fun registrarFiado(fiado: Fiado): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["fiados"].insert(fiado)
                true
            } catch (e: Exception) {
                Log.e("VentasRepo", "Error al registrar fiado: ${e.message}", e)
                false
            }
        }
    }

    suspend fun registrarPlatoPrestado(plato: PlatoPrestado): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["platos_prestados"].insert(plato)
                true
            } catch (e: Exception) {
                Log.e("VentasRepo", "Error al registrar plato prestado: ${e.message}", e)
                false
            }
        }
    }

    /**
     * Obtiene ventas dentro de un rango de fechas (inclusive).
     * Filtra en Supabase para no descargar toda la tabla al celular.
     *
     * @param desde fecha en formato "yyyy-MM-dd"
     * @param hasta fecha en formato "yyyy-MM-dd"
     */
    suspend fun obtenerVentasPorRango(desde: String, hasta: String): List<Venta> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["ventas"]
                    .select {
                        filter {
                            // Filtra: fecha >= desde AND fecha <= hasta
                            // Supabase acepta comparaciones de texto ISO yyyy-MM-dd correctamente
                            gte("fecha", desde)
                            lte("fecha", hasta)
                        }
                        order("fecha", Order.DESCENDING)
                    }
                    .decodeList<Venta>()
            } catch (e: Exception) {
                Log.e("VentasRepo", "Error al obtener ventas [$desde - $hasta]: ${e.message}", e)
                emptyList()
            }
        }
    }

    /**
     * Mantén este método solo si necesitas exportar o hacer un respaldo completo.
     * Para los reportes del día/semana/mes usa obtenerVentasPorRango().
     */
    suspend fun obtenerTodasLasVentas(): List<Venta> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["ventas"]
                    .select { order("fecha", Order.DESCENDING) }
                    .decodeList<Venta>()
            } catch (e: Exception) {
                Log.e("VentasRepo", "Error al obtener todas las ventas: ${e.message}", e)
                emptyList()
            }
        }
    }
}