package com.tesis.appmovil.models
import com.google.gson.annotations.SerializedName

data class Ubicacion(
    @SerializedName("idUbicacion")
    val id_ubicacion: Int,
    val ciudad: String,
    val distrito: String
)
