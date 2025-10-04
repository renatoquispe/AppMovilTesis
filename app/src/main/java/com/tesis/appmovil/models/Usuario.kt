package com.tesis.appmovil.models

data class Usuario(
    val idUsuario: Int,
    val nombre: String?,
    val apellidoPaterno: String?,
    val apellidoMaterno: String?,
    val correo: String?,
    val contrasena: String? = null,
    val fechaNacimiento: String?,   // 👈 string en formato yyyy-MM-dd
    val fotoPerfil: String?,
    val fechaCreacion: String?,     // también llega como string ISO
    val estadoAuditoria: Int        // llega como 1 o 0
)
