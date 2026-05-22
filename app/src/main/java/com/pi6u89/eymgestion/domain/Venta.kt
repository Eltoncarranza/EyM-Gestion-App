package com.pi6u89.eymgestion.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Venta(
    val id: Int = 0,

    @SerialName("monto_total")
    val montoTotal: Double,

    @SerialName("costo_total")
    val costoTotal: Double? = 0.0, // <-- Cambiado a Double? y valor por defecto

    @SerialName("metodo_pago")
    val metodoPago: String,

    @SerialName("estado_pago")
    val estadoPago: String? = "PAGADO", // <-- Cambiado a String? y valor por defecto

    @SerialName("presta_plato")
    val prestaPlato: Boolean = false,

    @SerialName("cliente_id")
    val clienteId: Int? = null,

    val fecha: String
)