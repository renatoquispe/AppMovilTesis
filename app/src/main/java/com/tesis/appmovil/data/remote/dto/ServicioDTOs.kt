package com.tesis.appmovil.data.remote.dto

data class ServicioCreate(
    val id_negocio: Int,
    val nombre: String,
    val descripcion: String? = null,
    val precio: Double,          // DECIMAL(10,2) -> Double
    val duracion_minutos: Int
)

data class ServicioUpdate(
    val id_negocio: Int? = null,
    val nombre: String? = null,
    val descripcion: String? = null,
    val precio: Double? = null,
    val duracion_minutos: Int? = null,
    val estado_auditoria: Boolean? = null
)
