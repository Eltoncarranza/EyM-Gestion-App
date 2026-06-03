package com.pi6u89.eymgestion.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ItemCompra(
    val id: Int = 0,
    val producto: String,
    val cantidad: String,
    val comprado: Boolean = false,
    val costo: Double = 0.0,
    @Transient val fecha: String = "" 
)