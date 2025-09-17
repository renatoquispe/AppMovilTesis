package com.tesis.appmovil.data.remote.request

data class NegocioResponse(
    val idNegocio: Int,
    val nombre: String,
    val descripcion: String?,
    val direccion: String,
    val latitud: String?,
    val longitud: String?,
    val telefono: String?,
    val correoContacto: String?,
    val fechaCreacion: String?, // lo devolvió como String ISO
    val estado: Any?, // antes lo quitaste, ahora lo soporta
    // AÑADIR ESTOS CAMPOS:
    val categoria: CategoriaResponse,
    val imagenes: List<NegocioImagenResponse> = emptyList() // ← Valor por defecto
)

data class CategoriaResponse(
    val idCategoria: Int,
    val nombre: String,
    val descripcion: String?,
    val fechaCreacion: String?,
    val estado: Any?
)

data class NegocioImagenResponse(
    val idImagen: Int,
    val urlImagen: String,
    val descripcion: String?,
    val fechaSubida: String?,
    val estado: Any?
)