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
    val estado: Any? // antes lo quitaste, ahora lo soporta
)