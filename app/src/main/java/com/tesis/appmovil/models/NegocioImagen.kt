package com.tesis.appmovil.models

import com.google.gson.annotations.SerializedName
import java.util.Date

data class NegocioImagen(
    @SerializedName("idImagen") val id_imagen: Int,
    @SerializedName("urlImagen") val url_imagen: String,
    @SerializedName("idNegocio") val id_negocio: Int,
    val descripcion: String?,
    @SerializedName("fechaSubida") val fecha_subida: Date?,
    val estado: Int
)
