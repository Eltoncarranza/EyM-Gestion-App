package com.pi6u89.eymgestion.data

import com.pi6u89.eymgestion.domain.Fiado
import com.pi6u89.eymgestion.domain.PlatoPrestado
import com.pi6u89.eymgestion.domain.Venta
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VentasRepository {

    // 1. Guarda la venta en la base de datos
    suspend fun registrarVenta(venta: Venta): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["ventas"].insert(venta)
                true
            } catch (e: Exception) {
                android.util.Log.e("VENTAS_REPO", "Error al registrar venta: ${e.message}")
                false
            }
        }
    }

    // 2. Registra la deuda si el cliente pidió fiado
    suspend fun registrarFiado(fiado: Fiado): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["fiados"].insert(fiado)
                true
            } catch (e: Exception) {
                android.util.Log.e("VENTAS_REPO", "Error al registrar fiado: ${e.message}")
                false
            }
        }
    }

    // 3. Registra la vajilla prestada si aplica
    suspend fun registrarPlatoPrestado(plato: PlatoPrestado): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["platos_prestados"].insert(plato)
                true
            } catch (e: Exception) {
                android.util.Log.e("VENTAS_REPO", "Error al registrar plato: ${e.message}")
                false
            }
        }
    }

    // 4. Descarga las ventas de un día específico para los Reportes
    suspend fun obtenerVentasPorFecha(fecha: String): List<Venta> {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // 1. Pedimos a Supabase TODAS las ventas (sin filtros) para burlar errores de formato
                val todasLasVentas = SupabaseClient.client.postgrest["ventas"]
                    .select()
                    .decodeList<Venta>()

                android.util.Log.d("DEBUG_REPO", "TOTAL de ventas en Supabase: ${todasLasVentas.size}")

                // 2. Filtramos la fecha directamente aquí en la aplicación
                val ventasDelDia = todasLasVentas.filter { it.fecha.startsWith(fecha) }

                android.util.Log.d("DEBUG_REPO", "Ventas que coinciden con $fecha: ${ventasDelDia.size}")

                ventasDelDia
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    android.util.Log.e("SUPABASE_ERROR", "Error al traer ventas: ${e.message}")
                }
                emptyList()
            }
        }
    }
}