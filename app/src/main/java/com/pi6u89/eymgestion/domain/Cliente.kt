package com.pi6u89.eymgestion.domain

import kotlinx.serialization.Serializable

@Serializable
data class Cliente(
    val id: Int = 0, // Generado por Supabase
    val nombre: String,
    val telefono: String = "" // Opcional, por si quieres mandarle WhatsApp
)