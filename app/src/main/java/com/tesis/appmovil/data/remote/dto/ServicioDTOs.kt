package com.tesis.appmovil.data.remote.dto

data class ServicioCreate(
    val idNegocio: Int,
    val nombre: String,
    val precio: Double,
    val duracionMinutos: Int? = null,
    val descuento: Double? = null,
    val imagenUrl: String? = null
)

data class ServicioUpdate(
    val nombre: String? = null,
    val precio: Double? = null,
    val duracionMinutos: Int? = null,
    val descuento: Double? = null,
    val imagenUrl: String? = null,
    val estadoAuditoria: Boolean? = null
)