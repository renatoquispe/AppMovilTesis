// NegocioRepository (ajustado)
package com.tesis.appmovil.repository


// NegocioRepository (ajustado con ApiResponse)

import com.tesis.appmovil.data.remote.ApiResponse
import com.tesis.appmovil.data.remote.ApiService
import com.tesis.appmovil.data.remote.RetrofitClient
import com.tesis.appmovil.data.remote.dto.NegocioCreate
import com.tesis.appmovil.data.remote.dto.NegocioUpdate
import com.tesis.appmovil.data.remote.request.NegocioResponse
import com.tesis.appmovil.models.Negocio
import retrofit2.HttpException
import retrofit2.Response

class NegocioRepository(
    private val api: ApiService = RetrofitClient.api
) {
    suspend fun listar(
        idCategoria: Int? = null,
        idUbicacion: Int? = null,
        q: String? = null
    ): List<Negocio> =
        api.getNegocios(idCategoria, idUbicacion, q).bodyOrThrow().data
            ?: emptyList()

    suspend fun obtener(id: Int): Negocio =
        api.getNegocio(id).bodyOrThrow().data
            ?: throw IllegalStateException("Negocio no encontrado")

    // En NegocioRepository
    suspend fun obtenerDetalle(id: Int): NegocioResponse =
        api.getNegocioDetalle(id).bodyOrThrow().data
            ?: throw IllegalStateException("Negocio no encontrado")


    suspend fun crear(body: NegocioCreate): Negocio =
        api.createNegocio(body).bodyOrThrow().data
            ?: throw IllegalStateException("Error al crear negocio")

    suspend fun actualizar(id: Int, body: NegocioUpdate): Negocio =
        api.updateNegocio(id, body).bodyOrThrow().data
            ?: throw IllegalStateException("Error al actualizar negocio")

    suspend fun eliminar(id: Int) {
        val resp = api.deleteNegocio(id)
        if (!resp.isSuccessful) throw HttpException(resp)
    }
//    suspend fun obtenerDetalle(id: Int): Negocio =
//        api.getNegocio(id).bodyOrThrow().data
//            ?: throw IllegalStateException("Negocio no encontrado")


}

/* Helper compartido */
private fun <T> Response<ApiResponse<T>>.bodyOrThrow(): ApiResponse<T> {
    if (isSuccessful) {
        val b = body()
        if (b != null) return b
    }
    throw HttpException(this)
}

//
//import com.tesis.appmovil.data.remote.ApiService
//import com.tesis.appmovil.data.remote.RetrofitClient
//import com.tesis.appmovil.data.remote.dto.NegocioCreate
//import com.tesis.appmovil.data.remote.dto.NegocioUpdate
//import com.tesis.appmovil.models.Negocio
//import retrofit2.HttpException
//import retrofit2.Response
//
//class NegocioRepository(
//    private val api: ApiService = RetrofitClient.api
//) {
//    suspend fun listar(
//        idCategoria: Int? = null,
//        idUbicacion: Int? = null,
//        q: String? = null
//    ): List<Negocio> = api.getNegocios(idCategoria, idUbicacion, q).bodyOrThrow()
//
//    suspend fun obtener(id: Int): Negocio = api.getNegocio(id).bodyOrThrow()
//
//    suspend fun crear(body: NegocioCreate): Negocio =
//        api.createNegocio(body).bodyOrThrow()
//
//    suspend fun actualizar(id: Int, body: NegocioUpdate): Negocio =
//        api.updateNegocio(id, body).bodyOrThrow()
//
//    suspend fun eliminar(id: Int) {
//        val resp = api.deleteNegocio(id)
//        if (!resp.isSuccessful) throw HttpException(resp)
//    }
//}
//
///* Helper compartido con los demás repos */
//private fun <T> Response<T>.bodyOrThrow(): T {
//    if (isSuccessful) {
//        val b = body()
//        if (b != null) return b
//    }
//    throw HttpException(this)
//}
