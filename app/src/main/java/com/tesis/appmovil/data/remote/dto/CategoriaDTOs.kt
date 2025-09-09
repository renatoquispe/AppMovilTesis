package com.tesis.appmovil.data.remote.dto

data class CategoriaCreate(
    val nombre: String,
    val descripcion: String? = null,
    val estado: Boolean = true   // opcional: si tu back ya pone true por defecto, puedes quitarlo
)

data class CategoriaUpdate(
    val nombre: String? = null,
    val descripcion: String? = null,
    val estado: Boolean? = null
)