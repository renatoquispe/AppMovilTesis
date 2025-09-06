package com.tesis.appmovil.models
import java.util.Date

data class Reseña(
    val id_resena: Int,
    val id_usuario: Int,
    val id_negocio: Int,
    val calificacion: Int,
    val comentario: String?,
    val fecha_creacion: Date,
    val estado_auditoria: Boolean
)
