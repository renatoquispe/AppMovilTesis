
//PRIMER VERSION
package com.tesis.appmovil.models

import com.tesis.appmovil.data.remote.request.NegocioResponse

//data class EstadoAuditoria(
//    val type: String,
//    val data: List<Int>
//)
data class Servicio(
    val idServicio: Int,
    val nombre: String,
    val descuento: String?,        // nuevo campo
    val imagenUrl: String?,
    val precio: String,
    val duracionMinutos: Int,
    val estadoAuditoria: Int,
    val idNegocio: Int,
    val negocio: NegocioResponse
) {
    // 👇 AGREGA ESTA FUNCIÓN PARA CONVERTIR A PORCENTAJE
    fun getDescuentoPorcentaje(): Int {
        return if (!descuento.isNullOrEmpty()) {
            (descuento.toDouble() * 100).toInt()
        } else {
            0
        }
    }
}
