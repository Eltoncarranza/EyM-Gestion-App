package com.pi6u89.eymgestion.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Venta(
    val id: Int = 0,
    @SerialName("monto_total") val montoTotal: Double,
    @SerialName("costo_total") val costoTotal: Double? = 0.0, // 👈 Ahora acepta nulos
    @SerialName("metodo_pago") val metodoPago: String,
    @SerialName("estado_pago") val estadoPago: String? = "PAGADO", // 👈 Ahora acepta nulos
    @SerialName("presta_plato") val prestaPlato: Boolean? = false, // 👈 Ahora acepta nulos
    @SerialName("cliente_id") val clienteId: Int? = null,
    val fecha: String,
    val detalles: String? = "" // 👈 ¡EL SECRETO! Ahora acepta nulos y ya no crasheará
)