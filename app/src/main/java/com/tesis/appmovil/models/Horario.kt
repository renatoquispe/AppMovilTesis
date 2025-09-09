package com.tesis.appmovil.models

data class Horario(
    val id_horario: Int,
    val id_negocio: Int,
    val dia_semana: String,
    val hora_apertura: String,   // mejor manejar TIME como String "HH:mm"
    val hora_cierre: String,
    val estado_auditoria: Boolean
)
