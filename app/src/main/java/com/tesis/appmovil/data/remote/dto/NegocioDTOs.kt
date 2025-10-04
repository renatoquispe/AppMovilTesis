package com.tesis.appmovil.data.remote.dto

import com.tesis.appmovil.data.remote.request.NegocioResponse
import com.tesis.appmovil.models.Negocio
// En tu data class NegocioCreate - CAMBIA A camelCase
data class NegocioCreate(
    val idCategoria: Int,        // ← CAMBIA de id_categoria
    val idUbicacion: Int,        // ← CAMBIA de id_ubicacion
    val idUsuario: Int? = null,  // ← CAMBIA de id_usuario
    val nombre: String,
    val descripcion: String? = null,
    val direccion: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val telefono: String? = null,
    val correoContacto: String? = null
)
//data class NegocioCreate(
//    val id_categoria: Int,
//    val id_ubicacion: Int,
//    val id_usuario: Int? = null,
//    val nombre: String,
//    val descripcion: String? = null,
//    val direccion: String? = null,
//    val latitud: Double? = null,     // DECIMAL(9,6) -> Double
//    val longitud: Double? = null,
//    val telefono: String? = null,
//    val correoContacto: String? = null
//)

// En tu data class NegocioUpdate - CAMBIA A camelCase
data class NegocioUpdate(
    val idCategoria: Int? = null,        // ← CAMBIA de id_categoria
    val idUbicacion: Int? = null,        // ← CAMBIA de id_ubicacion
    val idUsuario: Int? = null,
    val nombre: String? = null,
    val descripcion: String? = null,
    val direccion: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val telefono: String? = null,
    val correoContacto: String? = null,
    val estadoAuditoria: Boolean? = null // ← CAMBIA de estado_auditoria
)
//data class NegocioUpdate(
//    val id_categoria: Int? = null,
//    val id_ubicacion: Int? = null,
//    val id_usuario: Int? = null,  // ← VERIFICA SI TU API NECESITA ESTE CAMPO
//    val nombre: String? = null,
//    val descripcion: String? = null,
//    val direccion: String? = null,
//    val latitud: Double? = null,
//    val longitud: Double? = null,
//    val telefono: String? = null,
//    val correoContacto: String? = null,
//    val estado_auditoria: Boolean? = null // por si activas/desactivas (soft-delete)
//)

data class NegocioUiState(
    val isLoading: Boolean = false,      // flag de carga
    val mutando:   Boolean = false,      // flag de crear/editar/eliminar
    val negocios:  List<Negocio> = emptyList(),
    val negocio:   Negocio? = null,      // ← NUEVA propiedad para GET /mio
    val seleccionado: Negocio? = null,    // si implementas detalle individual
    val detalle:     NegocioResponse? = null,
    val error:       String? = null
)