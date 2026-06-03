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
        try { SupabaseClient.client.postgrest["clientes"].select().decodeList<Cliente>() } catch (e: Exception) { emptyList() }
    }

    suspend fun agregarCliente(cliente: Cliente): Boolean = withContext(Dispatchers.IO) {
        try { SupabaseClient.client.postgrest["clientes"].insert(cliente); true } catch (e: Exception) { false }
    }

    suspend fun obtenerDeudasActivas(): List<Fiado> = withContext(Dispatchers.IO) {
        try { SupabaseClient.client.postgrest["fiados"].select { filter { eq("pagado", false) } }.decodeList<Fiado>() } catch (e: Exception) { emptyList() }
    }

    suspend fun obtenerPlatosSinDevolver(): List<PlatoPrestado> = withContext(Dispatchers.IO) {
        try { SupabaseClient.client.postgrest["platos_prestados"].select { filter { eq("devuelto", false) } }.decodeList<PlatoPrestado>() } catch (e: Exception) { emptyList() }
    }

    suspend fun registrarDevolucionPlatos(clienteId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client.postgrest["platos_prestados"].update({ set("devuelto", true) }) { filter { eq("cliente_id", clienteId); eq("devuelto", false) } }
            true
        } catch (e: Exception) { false }
    }

    // 👈 ESTA ES LA FUNCIÓN ESTRELLA QUE MANEJA LOS PAGOS PARCIALES
    suspend fun registrarAbonoDeuda(clienteId: Int, montoPagado: Double, metodoPago: String, fecha: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Obtener los fiados pendientes del cliente ordenados del más antiguo al más nuevo
                val fiadosPendientes = SupabaseClient.client.postgrest["fiados"]
                    .select { filter { eq("cliente_id", clienteId); eq("pagado", false) } }
                    .decodeList<Fiado>()
                    .sortedBy { it.id }

                var saldoAbono = montoPagado

                // 2. Repartir el pago entre las deudas existentes
                for (fiado in fiadosPendientes) {
                    if (saldoAbono <= 0) break // Si ya se gastó todo el abono, salimos

                    if (saldoAbono >= fiado.monto) {
                        // El abono alcanza para pagar ESTA deuda completa
                        saldoAbono -= fiado.monto
                        SupabaseClient.client.postgrest["fiados"].update({
                            set("pagado", true)
                        }) { filter { eq("id", fiado.id) } }
                    } else {
                        // El abono NO alcanza para pagarla toda. Hacemos un pago parcial.
                        val nuevoMonto = fiado.monto - saldoAbono
                        saldoAbono = 0.0
                        SupabaseClient.client.postgrest["fiados"].update({
                            set("monto", nuevoMonto)
                        }) { filter { eq("id", fiado.id) } }
                    }
                }

                // 3. Registrar el ingreso de dinero como una Venta para que cuadre en "Reportes"
                val ingresoVenta = Venta(
                    montoTotal = montoPagado,
                    metodoPago = metodoPago,
                    estadoPago = "PAGADO",
                    clienteId = clienteId,
                    fecha = fecha,
                    detalles = "Abono de deuda fiada"
                )
                SupabaseClient.client.postgrest["ventas"].insert(ingresoVenta)

                true
            } catch (e: Exception) {
                Log.e("CLIENTE_REPO", "Error al abonar: ${e.message}")
                false
            }
        }
    }
}