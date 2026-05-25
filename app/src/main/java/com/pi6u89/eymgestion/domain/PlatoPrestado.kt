package com.pi6u89.eymgestion.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlatoPrestado(
    val id: Int = 0,
    @SerialName("cliente_id") val clienteId: Int,
    @SerialName("cantidad_platos") val cantidadPlatos: Int,
    @SerialName("fecha_prestamo") val fechaPrestamo: String,
    val devuelto: Boolean = false
)