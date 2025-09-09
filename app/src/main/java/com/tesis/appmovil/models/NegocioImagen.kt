package com.tesis.appmovil.models

data class NegocioImagen(
    val id_imagen: Int,
    val id_negocio: Int,
    val url_imagen: String,
    val descripcion: String?,
    val fecha_subida: String,   // "YYYY-MM-DDTHH:mm:ss" como String es lo más seguro
    val estado: Boolean
)
