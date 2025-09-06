package com.tesis.appmovil.models
import java.util.Date

data class NegocioImagen(
    val id_imagen: Int,
    val id_negocio: Int,
    val url_imagen: String,
    val descripcion: String?,
    val fecha_subida: Date,
    val estado: Boolean
)
