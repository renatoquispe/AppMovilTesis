// ServicioRepository.kt
package com.tesis.appmovil.repository

import com.tesis.appmovil.data.remote.ApiService
import com.tesis.appmovil.data.remote.RetrofitClient
import com.tesis.appmovil.data.remote.dto.ServicioCreate
import com.tesis.appmovil.data.remote.dto.ServicioUpdate
import com.tesis.appmovil.data.remote.ApiResponse
import com.tesis.appmovil.models.Servicio
import retrofit2.HttpException
import retrofit2.Response

class ServicioRepository(
    private val api: ApiService = RetrofitClient.api
) {
    /** Lista servicios; opcionalmente por negocio */
    suspend fun listar(idNegocio: Int? = null): List<Servicio> =
        api.getServicios(idNegocio).unwrap()

    /** Obtiene un servicio por id */
    suspend fun obtener(id: Int): Servicio =
        api.getServicio(id).unwrap()

    /** Crea y devuelve el servicio creado */
    suspend fun crear(body: ServicioCreate): Servicio =
        api.createServicio(body).unwrap()

    /** Actualiza y devuelve el servicio actualizado */
    suspend fun actualizar(id: Int, body: ServicioUpdate): Servicio =
        api.updateServicio(id, body).unwrap()

    /** Elimina un servicio (lanza excepción si falla) */
    suspend fun eliminar(id: Int) {
        val resp = api.deleteServicio(id)
        if (!resp.isSuccessful) throw HttpException(resp)
    }
}

/** 🔹 Helper para desempaquetar ApiResponse */
/** 🔹 Helper para desempaquetar ApiResponse */
private fun <T> Response<ApiResponse<T>>.unwrap(): T {
    if (isSuccessful) {
        val apiResp = body()
        if (apiResp != null && apiResp.success && apiResp.data != null) {
            return apiResp.data
        }
    }
    throw HttpException(this)
}
