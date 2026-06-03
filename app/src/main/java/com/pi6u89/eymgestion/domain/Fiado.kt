package com.pi6u89.eymgestion.domain

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Fiado(
    @EncodeDefault(EncodeDefault.Mode.NEVER) val id: Int = 0, // 👈 EL TRUCO ESTÁ AQUÍ
    @SerialName("cliente_id") val clienteId: Int,
    val monto: Double,
    val fecha: String,
    val pagado: Boolean = false
)