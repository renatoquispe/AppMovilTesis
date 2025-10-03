package com.tesis.appmovil.data.remote.request

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val status: Int,
    val data: RegisterData?
)

data class RegisterData(
    val idUsuario: Int,
    val correo: String
)