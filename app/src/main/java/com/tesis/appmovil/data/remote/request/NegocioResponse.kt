package com.tesis.appmovil.data.remote.request

data class NegocioResponse(
    val idNegocio: Int,
    val nombre: String,
    val descripcion: String?,
    val direccion: String,
    val latitud: String?,
    val longitud: String?,
    val telefono: String?,
    val correoContacto: String?,
    val fechaCreacion: String?, // lo devolvió como String ISO
    val estado: Any?, // antes lo quitaste, ahora lo soporta
    // AÑADIR ESTOS CAMPOS:
    val categoria: CategoriaResponse,
    val imagenes: List<NegocioImagenResponse> = emptyList(), // ← Valor por defecto
    val servicios: List<ServicioResponse> = emptyList(),
    val horarios: List<HorarioResponse> = emptyList(),
    val resenas: List<ResenaResponse> = emptyList(),
    val ubicacion: UbicacionesResponse? = null
)
// Y crea los data classes para los nuevos campos
data class UbicacionesResponse(
    val idUbicacion: Int,
    val ciudad: String,
    val distrito: String
)
data class ServicioResponse(
    val idServicio: Int,
    val nombre: String,
    val descuento: Double,
    val imagenUrl: String,
    val precio: Double,
    val duracionMinutos: Int
)

data class HorarioResponse(
    val idHorario: Int,
    val diaSemana: String,
    val horaApertura: String,
    val horaCierre: String
)

data class ResenaResponse(
    val idResena: Int,
    val calificacion: Int,
    val comentario: String,
    val fechaCreacion: String,
    val usuario: UsuarioResponse // Relaciona la reseña con el usuario que la creó
)

data class UsuarioResponse(
    val idUsuario: Int,
    val nombre: String
    // u otros campos que necesites
)

data class CategoriaResponse(
    val idCategoria: Int,
    val nombre: String,
    val descripcion: String?,
    val fechaCreacion: String?,
    val estado: Any?
)

data class NegocioImagenResponse(
    val idImagen: Int,
    val urlImagen: String,
    val descripcion: String?,
    val fechaSubida: String?,
    val estado: Any?
)