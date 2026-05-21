package com.pi6u89.eymgestion.data

import com.pi6u89.eymgestion.domain.Venta
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VentasRepository {

    // Suspend fun permite que la función corra en segundo plano sin congelar la pantalla
    suspend fun registrarVenta(venta: Venta): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Accedemos al cliente de Supabase, entramos a la tabla "ventas" e insertamos el objeto
                SupabaseClient.client.postgrest["ventas"].insert(venta)
                true // Si todo sale bien, devuelve verdadero
            } catch (e: Exception) {
                e.printStackTrace()
                false // Si hay un error (ej. sin internet), devuelve falso
            }
        }
    }
}