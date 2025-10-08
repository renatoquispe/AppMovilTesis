package com.tesis.appmovil.data.remote.request

import com.google.gson.annotations.SerializedName

/** DTOs para /negocios/{id} (detalle) */

data class NegocioResponse(
    val idNegocio: Int,
    val idUsuario: Int,
    val nombre: String,
    val descripcion: String? = null,
    val direccion: String? = null,
    val latitud: String? = null,     // tu backend envía string
    val longitud: String? = null,    // tu backend envía string
    val telefono: String? = null,
    val correoContacto: String? = null,
    val fechaCreacion: String? = null,
    val estadoAuditoria: Int?,
    val estado: Any? = null,         // puede venir 1 o Buffer
    val categoria: CategoriaDto,
    val ubicacion: UbicacionDto? = null,
    val imagenes: List<ImagenDto>? = null,
    val servicios: List<ServicioDto>? = null,
    val horarios: List<HorarioDto>? = null
)

data class CategoriaDto(
    val idCategoria: Int,
    val nombre: String,
    val descripcion: String? = null,
    val fechaCreacion: String? = null,
    val estado: Any? = null
)

data class UbicacionDto(
    val idUbicacion: Int,
    val ciudad: String? = null,
    val distrito: String? = null
)

data class ImagenDto(
    val idImagen: Int,
    val urlImagen: String,
    val descripcion: String? = null,
    val fechaSubida: String? = null,
    val estado: Any? = null
)

data class ServicioDto(
    val idServicio: Int,
    val nombre: String,
    val descuento: String? = null,   // en tus respuestas viene "20.00"
    val imagenUrl: String? = null,
    val precio: String,              // viene como string "25.00"
    val duracionMinutos: Int,
    val estadoAuditoria: Int,
    val idNegocio: Int
)
data class HorarioDto(
    val idHorario: Int? = null,
    val diaSemana: String,
    val horaApertura: String,
    val horaCierre: String,
    val estado: Int
) {
    // Función para formatear "10:00:00" → "10:00 AM"
    fun getHoraFormateada(hora: String): String {
        return try {
            val partes = hora.split(":")
            val horas = partes[0].toInt()
            val minutos = partes[1]

            when {
                horas == 0 -> "12:$minutos AM"
                horas < 12 -> "$horas:$minutos AM"
                horas == 12 -> "12:$minutos PM"
                else -> "${horas - 12}:$minutos PM"
            }
        } catch (e: Exception) {
            hora // Si hay error, devolver la hora original
        }
    }

    fun getHorarioCompleto(): String {
        return "${getHoraFormateada(horaApertura)} - ${getHoraFormateada(horaCierre)}"
    }
}
