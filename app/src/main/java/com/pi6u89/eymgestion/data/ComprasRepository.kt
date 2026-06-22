package com.pi6u89.eymgestion.data

import android.util.Log
import com.pi6u89.eymgestion.domain.ItemCompra
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class ResumenProducto(
    val nombre: String,
    val unidad: String,
    val vecesComprado: Int,
    val cantidadTotal: Double,
    val cantidadPromedio: Double,
    val costoTotal: Double,
    val costoPromedio: Double,
    val diasEntreCompras: Double?,
    val fechas: List<String>
)

data class CompraDelDia(
    val fecha: String,
    val productos: List<ItemCompra>,
    val totalGastado: Double
)

data class CompraDeLaSemana(
    val etiqueta: String,
    val fechaInicio: String,
    val fechaFin: String,
    val productos: List<ItemCompra>,
    val totalGastado: Double
)

class ComprasRepository {

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val sdfMostrar = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    suspend fun obtenerListaCompras(): List<ItemCompra> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["lista_compras"]
                    .select { order("id", Order.DESCENDING) }
                    .decodeList<ItemCompra>()
            } catch (e: Exception) {
                Log.e("ComprasRepo", "Error al obtener lista: ${e.message}", e)
                emptyList()
            }
        }
    }

    suspend fun marcarComoComprado(id: Int, nuevoCosto: Double, fechaHoy: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["lista_compras"]
                    .update({
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
    }

    suspend fun agregarItem(item: ItemCompra): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["lista_compras"].insert(item)
                true
            } catch (e: Exception) {
                Log.e("ComprasRepo", "Error al agregar item: ${e.message}", e)
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
                Log.e("ComprasRepo", "Error al eliminar id=$id: ${e.message}", e)
                false
            }
        }
    }

    suspend fun obtenerHistorialPorDia(): List<CompraDelDia> {
        return withContext(Dispatchers.IO) {
            try {
                val comprados = SupabaseClient.client.postgrest["lista_compras"]
                    .select {
                        filter { eq("comprado", true) }
                        order("fecha", Order.DESCENDING)
                    }
                    .decodeList<ItemCompra>()
                    .filter { it.fecha.isNotBlank() }

                comprados
                    .groupBy { it.fecha.take(10) }
                    .map { (fecha, items) ->
                        val fechaFormateada = try {
                            val d = sdf.parse(fecha)
                            if (d != null) sdfMostrar.format(d) else fecha
                        } catch (e: Exception) { fecha }

                        CompraDelDia(
                            fecha = fechaFormateada,
                            productos = items,
                            totalGastado = items.sumOf { it.costo }
                        )
                    }
                    .sortedByDescending { entry ->
                        try { sdf.parse(
                            comprados.first { sdfMostrar.format(sdf.parse(it.fecha.take(10))!!) == entry.fecha
                                    || it.fecha.take(10) == entry.fecha
                            }.fecha.take(10)
                        )?.time ?: 0L } catch (e: Exception) { 0L }
                    }
            } catch (e: Exception) {
                Log.e("ComprasRepo", "Error historial por dia: ${e.message}", e)
                emptyList()
            }
        }
    }

    suspend fun obtenerHistorialPorSemana(): List<CompraDeLaSemana> {
        return withContext(Dispatchers.IO) {
            try {
                val comprados = SupabaseClient.client.postgrest["lista_compras"]
                    .select {
                        filter { eq("comprado", true) }
                        order("fecha", Order.DESCENDING)
                    }
                    .decodeList<ItemCompra>()
                    .filter { it.fecha.isNotBlank() }

                comprados
                    .groupBy { item ->
                        val fecha = sdf.parse(item.fecha.take(10)) ?: return@groupBy "sin-fecha"
                        val cal = Calendar.getInstance()
                        cal.time = fecha
                        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                        sdf.format(cal.time)
                    }
                    .map { (inicioSemana, items) ->
                        val inicioDate = try { sdf.parse(inicioSemana) } catch (e: Exception) { null }
                        val finDate = if (inicioDate != null) {
                            val cal = Calendar.getInstance()
                            cal.time = inicioDate
                            cal.add(Calendar.DAY_OF_WEEK, 6)
                            cal.time
                        } else null

                        val inicioStr = if (inicioDate != null) sdfMostrar.format(inicioDate) else inicioSemana
                        val finStr = if (finDate != null) sdfMostrar.format(finDate) else ""

                        CompraDeLaSemana(
                            etiqueta = "$inicioStr — $finStr",
                            fechaInicio = inicioStr,
                            fechaFin = finStr,
                            productos = items.sortedByDescending { it.fecha },
                            totalGastado = items.sumOf { it.costo }
                        )
                    }
                    .sortedByDescending { semana ->
                        try { sdf.parse(
                            comprados.firstOrNull { item ->
                                val d = sdf.parse(item.fecha.take(10)) ?: return@firstOrNull false
                                val cal = Calendar.getInstance()
                                cal.time = d
                                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                                sdfMostrar.format(cal.time) == semana.fechaInicio
                            }?.fecha?.take(10) ?: ""
                        )?.time ?: 0L } catch (e: Exception) { 0L }
                    }
            } catch (e: Exception) {
                Log.e("ComprasRepo", "Error historial por semana: ${e.message}", e)
                emptyList()
            }
        }
    }

    suspend fun obtenerHistorialAgrupado(): List<ResumenProducto> {
        return withContext(Dispatchers.IO) {
            try {
                val comprados = SupabaseClient.client.postgrest["lista_compras"]
                    .select {
                        filter { eq("comprado", true) }
                        order("fecha", Order.ASCENDING)
                    }
                    .decodeList<ItemCompra>()
                    .filter { it.fecha.isNotBlank() }

                comprados
                    .groupBy { it.producto.trim().lowercase() }
                    .map { (_, items) ->
                        val nombre = items.last().producto.trim()
                        val unidad = items.last().unidad
                        val fechas = items.mapNotNull {
                            it.fecha.take(10).takeIf { f -> f.length == 10 }
                        }
                        val cantidades = items.map { it.cantidad }
                        val costos = items.map { it.costo }

                        val diasEntreCompras: Double? = if (fechas.size >= 2) {
                            val parsed = fechas.mapNotNull {
                                try { sdf.parse(it) } catch (e: Exception) { null }
                            }.sortedBy { it.time }
                            if (parsed.size >= 2) {
                                val intervalos = parsed.zipWithNext { a, b ->
                                    (b.time - a.time).toDouble() / (1000 * 60 * 60 * 24)
                                }
                                intervalos.average()
                            } else null
                        } else null

                        ResumenProducto(
                            nombre = nombre,
                            unidad = unidad,
                            vecesComprado = items.size,
                            cantidadTotal = cantidades.sum(),
                            cantidadPromedio = cantidades.average(),
                            costoTotal = costos.sum(),
                            costoPromedio = costos.average(),
                            diasEntreCompras = diasEntreCompras,
                            fechas = fechas
                        )
                    }
                    .sortedByDescending { it.vecesComprado }
            } catch (e: Exception) {
                Log.e("ComprasRepo", "Error al obtener historial: ${e.message}", e)
                emptyList()
            }
        }
    }
}