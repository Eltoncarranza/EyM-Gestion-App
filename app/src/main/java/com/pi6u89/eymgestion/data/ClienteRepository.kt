package com.pi6u89.eymgestion.data

import com.pi6u89.eymgestion.domain.Cliente
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ClienteRepository {

    // Función para descargar toda la lista de clientes (Caseritos) de la nube
    suspend fun obtenerClientes(): List<Cliente> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["clientes"]
                    .select()
                    .decodeList<Cliente>()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList() // Si no hay internet, devuelve una lista vacía para no crashear
            }
        }
    }

    // Función para guardar un nuevo caserito en la base de datos
    suspend fun agregarCliente(cliente: Cliente): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["clientes"].insert(cliente)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
    // Descarga todos los fiados que AÚN NO han sido pagados
    suspend fun obtenerDeudasActivas(): List<com.pi6u89.eymgestion.domain.Fiado> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["fiados"]
                    .select { filter { eq("pagado", false) } }
                    .decodeList<com.pi6u89.eymgestion.domain.Fiado>()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    // Descarga toda la vajilla que AÚN NO ha sido devuelta
    suspend fun obtenerPlatosSinDevolver(): List<com.pi6u89.eymgestion.domain.PlatoPrestado> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["platos_prestados"]
                    .select { filter { eq("devuelto", false) } }
                    .decodeList<com.pi6u89.eymgestion.domain.PlatoPrestado>()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    // Cambia el estado de todas las deudas de un cliente a "pagado = true"
    suspend fun liquidarDeudaCliente(clienteId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["fiados"]
                    .update(mapOf("pagado" to true)) {
                        filter { eq("cliente_id", clienteId) }
                    }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    // Cambia el estado de todos los platos prestados de un cliente a "devuelto = true"
    suspend fun registrarDevolucionPlatos(clienteId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["platos_prestados"]
                    .update(mapOf("devuelto" to true)) {
                        filter { eq("cliente_id", clienteId) }
                    }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}