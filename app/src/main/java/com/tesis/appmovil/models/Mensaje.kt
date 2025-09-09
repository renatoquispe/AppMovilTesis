package com.tesis.appmovil.models

data class Mensaje(
    val id_mensaje: Int,
    val id_usuario: Int,
    val mensaje_usuario: String,
    val respuesta_bot: String?,     // puede venir null si aún no hay respuesta
    val fecha_envio: String         // maneja DATETIME como String "YYYY-MM-DDTHH:mm:ss"
)
