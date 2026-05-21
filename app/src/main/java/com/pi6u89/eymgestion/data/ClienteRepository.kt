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
}