package com.tesis.appmovil.data.remote.dto

data class ImagenResenaCreate(
    val id_resena: Int,
    val url_imagen: String
)

data class ImagenResenaUpdate(
    val id_resena: Int? = null,
    val url_imagen: String? = null
)
