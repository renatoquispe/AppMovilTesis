package com.tesis.appmovil.data.remote.request

data class RegisterRequest(
    val nombre: String,
    val apellidoPaterno: String = "",
    val apellidoMaterno: String = "",
    val correo: String,
    val contrasena: String,
    val fechaNacimiento: String = "2000-01-01",
    val fotoPerfil: String? = null
)