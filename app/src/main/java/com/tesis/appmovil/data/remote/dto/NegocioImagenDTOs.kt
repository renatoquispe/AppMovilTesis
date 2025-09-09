package com.tesis.appmovil.data.remote.dto

// Crear pasando una URL (cuando ya subiste la imagen a algún storage)
data class NegocioImagenCreate(
    val id_negocio: Int,
    val url_imagen: String,
    val descripcion: String? = null
)

// Actualizar campos (parcial)
data class NegocioImagenUpdate(
    val url_imagen: String? = null,
    val descripcion: String? = null,
    val estado: Boolean? = null
)
