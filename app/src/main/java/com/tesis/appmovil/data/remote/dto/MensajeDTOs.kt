package com.tesis.appmovil.data.remote.dto

// Para crear un mensaje (el bot puede responder luego en el backend)
data class MensajeCreate(
    val id_usuario: Int,
    val mensaje_usuario: String
)

// Para actualizar (por ejemplo, agregar/cambiar respuesta del bot)
data class MensajeUpdate(
    val mensaje_usuario: String? = null,
    val respuesta_bot: String? = null
)
