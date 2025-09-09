package com.tesis.appmovil.data.remote.dto

data class HorarioCreate(
    val id_negocio: Int,
    val dia_semana: String,     // ej. "Lunes" (como en tu BD)
    val hora_apertura: String,  // "HH:mm" -> ej. "09:00"
    val hora_cierre: String     // "HH:mm" -> ej. "18:00"
)

data class HorarioUpdate(
    val dia_semana: String? = null,
    val hora_apertura: String? = null,
    val hora_cierre: String? = null
    // estado_auditoria no va en Create; en Update solo si tu back lo usa
)
