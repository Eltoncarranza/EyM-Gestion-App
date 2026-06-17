package com.pi6u89.eymgestion.domain

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ItemCompra(
    @EncodeDefault(EncodeDefault.Mode.NEVER) val id: Int = 0,
    val producto: String,
    val cantidad: String,
    val comprado: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val costo: Double = 0.0,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val fecha: String? = null
)