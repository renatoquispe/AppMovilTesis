package com.tesis.appmovil.data.remote.request

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: Data?
)

data class Data(
    val token: String,
    val usuario: UsuarioData
)

data class UsuarioData(
    val idUsuario: Int,
    val correo: String
)