package com.pi6u89.eymgestion.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Fiado(
    val id: Int = 0,
    @SerialName("cliente_id")
    val clienteId: Int,
    val monto: Double,
    val fecha: String, // Guardaremos la fecha como texto "2026-05-21"
    val pagado: Boolean = false // Por defecto, cuando se fía, no está pagado
)