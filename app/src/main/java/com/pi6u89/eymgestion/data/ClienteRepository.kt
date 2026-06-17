package com.pi6u89.eymgestion.data

import android.util.Log
import com.pi6u89.eymgestion.domain.Cliente
import com.pi6u89.eymgestion.domain.Fiado
import com.pi6u89.eymgestion.domain.PlatoPrestado
import com.pi6u89.eymgestion.domain.Venta
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ClienteRepository {

    suspend fun obtenerClientes(): List<Cliente> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client.postgrest["clientes"].select().decodeList<Cliente>()
        } catch (e: Exception) {
            Log.e("ClienteRepo", "Error al obtener clientes: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun agregarCliente(cliente: Cliente): Boolean = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client.postgrest["clientes"].insert(cliente)
            true
        } catch (e: Exception) {
            Log.e("ClienteRepo", "Error al agregar cliente: ${e.message}", e)
            false
        }
    }

    suspend fun obtenerDeudasActivas(): List<Fiado> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client.postgrest["fiados"]
                .select { filter { eq("pagado", false) } }
                .decodeList<Fiado>()
        } catch (e: Exception) {
            Log.e("ClienteRepo", "Error al obtener deudas: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun obtenerPlatosSinDevolver(): List<PlatoPrestado> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client.postgrest["platos_prestados"]
                .select { filter { eq("devuelto", false) } }
                .decodeList<PlatoPrestado>()
        } catch (e: Exception) {
            Log.e("ClienteRepo", "Error al obtener platos: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun registrarDevolucionPlatos(clienteId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client.postgrest["platos_prestados"].update({
                set("devuelto", true)
            }) { filter { eq("cliente_id", clienteId); eq("devuelto", false) } }
            true
        } catch (e: Exception) {
            Log.e("ClienteRepo", "Error al registrar devolución: ${e.message}", e)
            false
        }
    }

    suspend fun registrarAbonoDeuda(
        clienteId: Int,
        montoPagado: Double,
        metodoPago: String,
        fecha: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val fiadosPendientes = SupabaseClient.client.postgrest["fiados"]
                .select { filter { eq("cliente_id", clienteId); eq("pagado", false) } }
                .decodeList<Fiado>()
                .sortedBy { it.id }

            if (fiadosPendientes.isEmpty()) {
                Log.w("ClienteRepo", "No hay deudas pendientes para clienteId=$clienteId")
                return@withContext false
            }

            var saldoAbono = montoPagado

            for (fiado in fiadosPendientes) {
                if (saldoAbono <= 0.0) break

                if (saldoAbono >= fiado.monto) {
                    saldoAbono -= fiado.monto
                    SupabaseClient.client.postgrest["fiados"].update({
                        set("pagado", true)
                    }) { filter { eq("id", fiado.id) } }
                } else {
                    val nuevoMonto = fiado.monto - saldoAbono
                    saldoAbono = 0.0
                    SupabaseClient.client.postgrest["fiados"].update({
                        set("monto", nuevoMonto)
                    }) { filter { eq("id", fiado.id) } }
                }
            }

            SupabaseClient.client.postgrest["ventas"].insert(
                Venta(
                    montoTotal = montoPagado,
                    metodoPago = metodoPago,
                    estadoPago = "PAGADO",
                    clienteId = clienteId,
                    fecha = fecha,
                    detalles = "Abono de deuda fiada"
                )
            )

            true
        } catch (e: Exception) {
            Log.e("ClienteRepo", "Error al abonar deuda: ${e.message}", e)
            false
        }
    }
}