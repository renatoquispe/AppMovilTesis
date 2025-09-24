package com.tesis.appmovil.data.remote

data class ServicioFilterRequest(
    val servicio: String? = null,
    val ubicacion: String? = null,
    val precioMin: Double? = null,
    val precioMax: Double? = null,
    val categoryId: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radiusKm: Double? = null
)