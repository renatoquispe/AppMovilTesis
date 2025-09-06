package com.tesis.appmovil.models

data class Servicio(
    val id_servicio: Int,
    val id_negocio: Int,
    val nombre: String,
    val descripcion: String?,
    val precio: Double,
    val duracion_minutos: Int,
    val estado_auditoria: Boolean
)
