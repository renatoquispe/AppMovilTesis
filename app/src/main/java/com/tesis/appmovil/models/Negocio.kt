package com.tesis.appmovil.models
import java.util.Date

data class Negocio(
    val id_negocio: Int,
    val id_categoria: Int,
    val id_ubicacion: Int,
    val nombre: String,
    val descripcion: String?,
    val direccion: String?,
    val latitud: Double?,
    val longitud: Double?,
    val telefono: String?,
    val correo_contacto: String?,
    val fecha_creacion: Date,
    val estado_auditoria: Boolean
)
