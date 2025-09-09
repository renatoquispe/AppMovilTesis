package com.tesis.appmovil.data.remote.dto

data class NegocioCreate(
    val id_categoria: Int,
    val id_ubicacion: Int,
    val nombre: String,
    val descripcion: String? = null,
    val direccion: String? = null,
    val latitud: Double? = null,     // DECIMAL(9,6) -> Double
    val longitud: Double? = null,
    val telefono: String? = null,
    val correo_contacto: String? = null
)

data class NegocioUpdate(
    val id_categoria: Int? = null,
    val id_ubicacion: Int? = null,
    val nombre: String? = null,
    val descripcion: String? = null,
    val direccion: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val telefono: String? = null,
    val correo_contacto: String? = null,
    val estado_auditoria: Boolean? = null // por si activas/desactivas (soft-delete)
)
