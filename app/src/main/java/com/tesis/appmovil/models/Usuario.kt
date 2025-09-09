package com.tesis.appmovil.models
import java.util.Date

data class Usuario(
    val id_usuario: Int,
    val nombre: String,
    val apellido_paterno: String,
    val apellido_materno: String,
    val correo: String,
    val contrasena: String,
    val fecha_nacimiento: Date,
    val foto_perfil: String?,
    val fecha_creacion: Date,
    val estado_auditoria: Boolean
)