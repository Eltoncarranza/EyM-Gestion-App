package com.pi6u89.eymgestion.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

object SupabaseClient {
    private const val SUPABASE_URL = "https://spymvrmjlueitydnwkgt.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNweW12cm1qbHVlaXR5ZG53a2d0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzY2MTY1NTIsImV4cCI6MjA5MjE5MjU1Mn0.dX8LS8nvPB-eANAb61MU1LidTv429rdX-VNeSqXZrV0"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        defaultSerializer = KotlinXSerializer(Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
        })
        install(Postgrest)
        install(Realtime)
        install(Auth)
    }
}
