package com.pi6u89.eymgestion.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseClient {
    // Reemplazaremos estos valores con los reales cuando creemos el proyecto en la web de Supabase
    private const val SUPABASE_URL = "https://tu-proyecto.supabase.co"
    private const val SUPABASE_KEY = "tu-anon-key"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest) // Para leer y escribir en las tablas
        install(Realtime)  // Para cambios instantáneos
    }
}