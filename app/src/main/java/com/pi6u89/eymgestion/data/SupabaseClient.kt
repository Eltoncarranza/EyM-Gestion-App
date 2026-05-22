package com.pi6u89.eymgestion.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseClient {
    // URL limpia, sin el /rest/v1/
    private const val SUPABASE_URL = "https://suuvslavttknedjfcdin.supabase.co"

    // Aquí pegas la llave larga que empieza con eyJ...
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InN1dXZzbGF2dHRrbmVkamZjZGluIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzkzODQ2NTksImV4cCI6MjA5NDk2MDY1OX0.3Mn3nKOKSG4IMRJAbFkYPo0jJJpzOlPNBMsNa5yn9JE"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest) {
            // Opcional: configurar la serialización aquí si es necesario
        }
        install(Realtime)
    }

    // Configuración adicional del serializador JSON
    val json = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
        coerceInputValues = true // <--- ESTO ES LO MÁS IMPORTANTE
    }

}