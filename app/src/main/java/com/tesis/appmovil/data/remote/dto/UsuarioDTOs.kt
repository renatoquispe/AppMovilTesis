package com.tesis.appmovil.data.remote.dto




data class UsuarioCreate(
    val nombre: String,
    val apellidoPaterno: String,
    val apellidoMaterno: String,
    val correo: String,
    val contrasena: String,
    val fechaNacimiento: String, // formato "yyyy-MM-dd"
    val fotoPerfil: String? = null
)

data class UsuarioUpdate(
    val nombre: String? = null,
    val apellidoPaterno: String? = null,
    val apellidoMaterno: String? = null,
    val correo: String? = null,
    val contrasena: String? = null,
    val fechaNacimiento: String? = null,
    val fotoPerfil: String? = null,
    val estadoAuditoria: Boolean? = null
)
