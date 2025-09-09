package com.tesis.appmovil.data.remote.dto

data class ResenaCreate(
    val id_usuario: Int,
    val id_negocio: Int,
    val calificacion: Int,         // 1..5 (el back valida)
    val comentario: String? = null
)

data class ResenaUpdate(
    val calificacion: Int? = null, // opcional para partial update
    val comentario: String? = null
    // estado_auditoria no en Create; en Update solo si tu back lo usa
)
