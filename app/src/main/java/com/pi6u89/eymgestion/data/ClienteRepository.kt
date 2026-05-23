package com.pi6u89.eymgestion.data

import android.util.Log
import com.pi6u89.eymgestion.domain.Cliente
import com.pi6u89.eymgestion.domain.Fiado
import com.pi6u89.eymgestion.domain.PlatoPrestado
import com.pi6u89.eymgestion.domain.Venta // <-- IMPORTANTE: Traemos tu clase Venta
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- CLASES AUXILIARES PARA ACTUALIZAR SIN ERRORES DE JSON ---
@Serializable
data class EstadoPagoUpdate(val pagado: Boolean)
@Serializable
data class LiquidarVentaUpdate(
    @SerialName("metodo_pago") val metodoPago: String,
    @SerialName("estado_pago") val estadoPago: String,
    val fecha: String // Movemos la venta al día de hoy para que sume al efectivo real en caja
)
@Serializable
data class EstadoPlatoUpdate(val devuelto: Boolean)
// -----------------------------------------------------------

class ClienteRepository {

    suspend fun obtenerClientes(): List<Cliente> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["clientes"]
                    .select()
                    .decodeList<Cliente>()
            } catch (e: Exception) {
                Log.e("SUPABASE_ERROR", "Error al obtener clientes: ${e.message}")
                emptyList()
            }
        }
    }

    suspend fun agregarCliente(cliente: Cliente): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["clientes"].insert(cliente)
                true
            } catch (e: Exception) {
                Log.e("SUPABASE_ERROR", "Error al agregar cliente: ${e.message}")
                false
            }
        }
    }

    suspend fun obtenerDeudasActivas(): List<Fiado> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["fiados"]
                    .select { filter { eq("pagado", false) } }
                    .decodeList<Fiado>()
            } catch (e: Exception) {
                Log.e("SUPABASE_ERROR", "Error al obtener fiados: ${e.message}")
                emptyList()
            }
        }
    }

    suspend fun obtenerPlatosSinDevolver(): List<PlatoPrestado> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["platos_prestados"]
                    .select { filter { eq("devuelto", false) } }
                    .decodeList<PlatoPrestado>()
            } catch (e: Exception) {
                Log.e("SUPABASE_ERROR", "Error al obtener platos: ${e.message}")
                emptyList()
            }
        }
    }

    suspend fun liquidarDeudaConPago(clienteId: Int, metodoPago: String, fechaHoy: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 1. ACTUALIZAMOS las ventas existentes que estaban pendientes de pago
                // Esto evita duplicados y limpia los reportes antiguos de fiados
                SupabaseClient.client.postgrest["ventas"]
                    .update(LiquidarVentaUpdate(metodoPago = metodoPago, estadoPago = "PAGADO", fecha = fechaHoy)) {
                        filter {
                            eq("cliente_id", clienteId)
                            eq("estado_pago", "FIADO")
                        }
                    }

                // 2. Marcamos los fiados de este cliente como pagados en la tabla fiados
                SupabaseClient.client.postgrest["fiados"]
                    .update(EstadoPagoUpdate(pagado = true)) {
                        filter { eq("cliente_id", clienteId) }
                    }
                true
            } catch (e: Exception) {
                Log.e("SUPABASE_ERROR", "Error al liquidar deudas en ventas y fiados: ${e.message}")
                e.printStackTrace()
                false
            }
        }
    }

    // --- FUNCIÓN CORREGIDA ---
    suspend fun registrarDevolucionPlatos(clienteId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Actualizamos la vajilla usando nuestro objeto serializable
                SupabaseClient.client.postgrest["platos_prestados"]
                    .update(EstadoPlatoUpdate(devuelto = true)) {
                        filter { eq("cliente_id", clienteId) }
                    }
                true
            } catch (e: Exception) {
                Log.e("SUPABASE_ERROR", "Error al devolver platos: ${e.message}")
                false
            }
        }
    }
}