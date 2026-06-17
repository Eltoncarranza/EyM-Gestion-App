package com.pi6u89.eymgestion.domain

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Cliente(
    @EncodeDefault(EncodeDefault.Mode.NEVER) val id: Int = 0,
    val nombre: String,
    val telefono: String = ""
)