//package com.tesis.appmovil.models
//import java.util.Date
//
//data class Categoria(
//    val id_categoria: Int,
//    val nombre: String,
//    val descripcion: String?,
//    val fecha_creacion: Date,
//    val estado: Boolean
//)
// Categoria.kt - Versión corregida (si tu backend usa camelCase)
package com.tesis.appmovil.models

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Categoria(
    @SerializedName("idCategoria")
    val id_categoria: Int,
    val nombre: String,
    val descripcion: String?,
    @SerializedName("fechaCreacion")
    val fecha_creacion: Date,
//    val estado: Boolean
    val estado: Any? = null
)