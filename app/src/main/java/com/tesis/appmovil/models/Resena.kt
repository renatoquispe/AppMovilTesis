package com.tesis.appmovil.models

data class Resena(
    val id_resena: Int,
    val id_usuario: Int,
    val id_negocio: Int,
    val calificacion: Int,
    val comentario: String?,
    val fecha_creacion: String,   // mejor como String ISO si tu back lo envía así
    val estado_auditoria: Boolean
)
