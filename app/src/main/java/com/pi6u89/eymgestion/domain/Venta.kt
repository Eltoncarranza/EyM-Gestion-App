package com.pi6u89.eymgestion.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Venta(
    val id: Int = 0, // Lo genera automáticamente Supabase

    @SerialName("monto_total")
    val montoTotal: Double,

    @SerialName("metodo_pago")
    val metodoPago: String, // "Efectivo", "Yape/Plin", "Fiado"

    @SerialName("presta_plato")
    val prestaPlato: Boolean,

    val fecha: String = "", // Supabase asignará la fecha actual por defecto

    @SerialName("cliente_id")
    val clienteId: Int? = null // El signo '?' significa que puede ser NULL (vacío)
)