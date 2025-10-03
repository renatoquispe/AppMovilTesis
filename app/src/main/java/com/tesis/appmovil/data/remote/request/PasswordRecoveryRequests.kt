package com.tesis.appmovil.data.remote.request

data class SolicitarCodigoRequest(val correo: String)

data class VerificarCodigoRequest(val correo: String, val codigo: String)

data class RestablecerContrasenaRequest(
    val correo: String,
    val codigo: String,
    val nuevaContrasena: String
)