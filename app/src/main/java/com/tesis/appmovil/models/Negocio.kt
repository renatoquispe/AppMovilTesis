package com.tesis.appmovil.models
import com.google.gson.annotations.SerializedName
import java.util.Date

//data class Negocio(
//    val id_negocio: Int,
//    val id_categoria: Int,
//    val id_ubicacion: Int,
//    val nombre: String,
//    val descripcion: String?,
//    val direccion: String?,
//    val latitud: Double?,
//    val longitud: Double?,
//    val telefono: String?,
//    val correo_contacto: String?,
//    val fecha_creacion: Date,
//    val estado_auditoria: Boolean
//)
data class Negocio(
    @SerializedName("idNegocio") val id_negocio: Int,
    @SerializedName("idUsuario") val id_usuario: Int,
    @SerializedName("idCategoria") val id_categoria: Int,
    @SerializedName("idUbicacion") val id_ubicacion: Int,
    val nombre: String,
    val descripcion: String?,
    val direccion: String?,
    val latitud: Double?,
    val longitud: Double?,
    val telefono: String?,
    @SerializedName("correoContacto") val correo_contacto: String?,
    @SerializedName("fechaCreacion") val fecha_creacion: Date?,
    @SerializedName("estadoAuditoria") val estado_auditoria: Int
)
