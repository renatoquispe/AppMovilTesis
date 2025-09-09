package com.tesis.appmovil.data.remote.dto

data class UbicacionCreate(
    val ciudad: String,
    val distrito: String
)

data class UbicacionUpdate(
    val ciudad: String? = null,
    val distrito: String? = null
)
