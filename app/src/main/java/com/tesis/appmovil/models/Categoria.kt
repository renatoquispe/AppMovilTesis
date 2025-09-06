package com.tesis.appmovil.models
import java.util.Date

data class Categoria(
    val id_categoria: Int,
    val nombre: String,
    val descripcion: String?,
    val fecha_creacion: Date,
    val estado: Boolean
)
