package com.tesis.appmovil.models
import java.util.Date

data class ImagenesResena(
    val id_imagen_reseña: Int,
    val id_reseña: Int,
    val url_imagen: String,
    val fecha_subida: Date
)
