package com.pi6u89.eymgestion.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Producto(
    val id: Int = 0, // El ID único que generará Supabase automáticamente
    val nombre: String,
    val categoria: String, // "Comida", "Cafetería", "Bebidas"

    // Usamos @SerialName para que Kotlin (camelCase) entienda
    // el formato de base de datos de Supabase (snake_case)
    @SerialName("costo_unitario")
    val costoUnitario: Double = 0.0,

    @SerialName("precio_sugerido")
    val precioSugerido: Double,

    @SerialName("disponible_hoy")
    val disponibleHoy: Boolean = true
)