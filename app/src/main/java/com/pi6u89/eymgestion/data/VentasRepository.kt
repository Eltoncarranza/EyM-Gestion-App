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

    suspend fun obtenerVentasPorFecha(fecha: String): List<Venta> {
        return withContext(Dispatchers.IO) {
            try {
                // Buscamos coincidencia exacta por el string de fecha (ej: "2026-05-21")
                val ventas = SupabaseClient.client.postgrest["ventas"]
                    .select {
                        filter { eq("fecha", fecha) }
                    }
                    .decodeList<Venta>()

                android.util.Log.d("DEBUG_REPO", "Ventas encontradas para $fecha: ${ventas.size}")
                ventas
            } catch (e: Exception) {
                android.util.Log.e("VENTAS_REPO", "Error al traer ventas: ${e.message}")
                emptyList()
            }
        }
    }

    suspend fun obtenerVentasPorRango(fechaInicio: String, fechaFin: String): List<Venta> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["ventas"]
                    .select {
                        filter {
                            gte("fecha", fechaInicio)
                            lte("fecha", fechaFin)
                        }
                    }
                    .decodeList<Venta>()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun obtenerReporteCierreDeCaja(fecha: String): ReporteCierre {
        val ventas = obtenerVentasPorFecha(fecha)

        return ReporteCierre(
            ventasEfectivo = ventas.filter {
                it.metodoPago.trim().equals("Efectivo", true) &&
                        (it.estadoPago ?: "PAGADO").trim().equals("PAGADO", true)
            }.sumOf { it.montoTotal },

            ventasYape = ventas.filter {
                it.metodoPago.trim().equals("Yape", true) &&
                        (it.estadoPago ?: "PAGADO").trim().equals("PAGADO", true)
            }.sumOf { it.montoTotal },

            ventasPlin = ventas.filter {
                it.metodoPago.trim().equals("Plin", true) &&
                        (it.estadoPago ?: "PAGADO").trim().equals("PAGADO", true)
            }.sumOf { it.montoTotal },

            ventasFiadas = ventas.filter {
                (it.estadoPago ?: "").trim().equals("FIADO", true)
            }.sumOf { it.montoTotal }
        )
    }

    data class ReporteCierre(
        val ventasEfectivo: Double,
        val ventasYape: Double,
        val ventasPlin: Double,
        val ventasFiadas: Double
    ) {
        val totalRecaudado: Double get() = ventasEfectivo + ventasYape + ventasPlin
        val totalGeneral: Double get() = totalRecaudado + ventasFiadas
    }

    fun obtenerFechaHoy(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    suspend fun obtenerVentasDeHoy(): List<Venta> {
        return obtenerVentasPorFecha(obtenerFechaHoy())
    }

    suspend fun obtenerTodosLosFiados(): List<Fiado> {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // Trae todos los registros de la tabla fiados
                SupabaseClient.client.postgrest["fiados"]
                    .select()
                    .decodeList<Fiado>()
            } catch (e: Exception) {
                android.util.Log.e("VENTAS_REPO", "Error al obtener fiados: ${e.message}")
                emptyList()
            }
        }
    }
    suspend fun procesarPagoDeFiado(fiadoId: Int, ventaEquivalente: Venta): Boolean {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["ventas"].insert(ventaEquivalente)

                SupabaseClient.client.postgrest["fiados"].delete {
                    filter {
                        eq("id", fiadoId)
                    }
                }
                true
            } catch (e: Exception) {
                android.util.Log.e("VENTAS_REPO", "Error al procesar pago de fiado: ${e.message}")
                false
            }
        }
    }
}