package com.tesis.appmovil.models

data class Service(
    val id: String,
    val title: String,          // "Corte fade"
    val businessName: String,   // "Barbería Cúspide"
    val duration: String,       // "30min"
    val price: Double,
    val description: String,
    val imageUrl: String,
    val rating: Double? = null,
    val tag: String? = null,
    val location: String = "Lima, Perú"
)
