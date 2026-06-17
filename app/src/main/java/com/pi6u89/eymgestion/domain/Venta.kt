package com.pi6u89.eymgestion.domain

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Venta(
    @EncodeDefault(EncodeDefault.Mode.NEVER) val id: Int = 0, // 👈 ¡ESTO EVITA EL ERROR EN SUPABASE!
    @SerialName("monto_total") val montoTotal: Double,
    @SerialName("costo_total") val costoTotal: Double? = 0.0,
    @SerialName("metodo_pago") val metodoPago: String,
    @SerialName("estado_pago") val estadoPago: String? = "PAGADO",
    @SerialName("presta_plato") val prestaPlato: Boolean? = false,
    @SerialName("cliente_id") val clienteId: Int? = null,
    val fecha: String? = null,
    val detalles: String? = ""
)