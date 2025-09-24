// models/Horario.kt
package com.tesis.appmovil.models

data class Horario(
    val idHorario: Int,        // ← camelCase
    val idNegocio: Int,        // ← camelCase  
    val diaSemana: String,     // ← camelCase
    val horaApertura: String,  // ← camelCase
    val horaCierre: String,    // ← camelCase
    val estado: Int            // ← En Postman viene como "estado", no "estado_auditoria"
)