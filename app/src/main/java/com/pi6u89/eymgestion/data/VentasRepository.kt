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

    suspend fun obtenerVentasPorRango(desde: String, hasta: String): List<Venta> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("VentasRepo", "Consultando ventas: $desde -> $hasta")

                val resultado = SupabaseClient.client.postgrest["ventas"]
                    .select {
                        filter {
                            and {
                                gte("fecha", desde)
                                lte("fecha", hasta)
                            }
                        }
                        order("fecha", Order.DESCENDING)
                    }
                    .decodeList<Venta>()

                Log.d("VentasRepo", "Ventas recibidas: ${resultado.size}")
                resultado
            } catch (e: Exception) {
                Log.e("VentasRepo", "Error al obtener ventas [$desde - $hasta]: ${e.message}", e)
                emptyList()
            }
        }
    }

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