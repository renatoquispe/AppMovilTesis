// UbicacionRepository.kt
package com.tesis.appmovil.repository

import com.tesis.appmovil.data.remote.ApiService
import com.tesis.appmovil.data.remote.RetrofitClient
import com.tesis.appmovil.data.remote.dto.UbicacionCreate
import com.tesis.appmovil.data.remote.dto.UbicacionUpdate
import com.tesis.appmovil.models.Ubicacion
import retrofit2.HttpException
import retrofit2.Response

class UbicacionRepository(
    private val api: ApiService = RetrofitClient.api
) {
    /** Lista todas las ubicaciones */
    suspend fun listar(): List<Ubicacion> =
        api.getUbicaciones().bodyOrThrow()

    /** Obtiene una ubicación por id */
    suspend fun obtener(id: Int): Ubicacion =
        api.getUbicacion(id).bodyOrThrow()

    /** Crea y devuelve la ubicación creada */
    suspend fun crear(body: UbicacionCreate): Ubicacion =
        api.createUbicacion(body).bodyOrThrow()

    /** Actualiza y devuelve la ubicación actualizada */
    suspend fun actualizar(id: Int, body: UbicacionUpdate): Ubicacion =
        api.updateUbicacion(id, body).bodyOrThrow()

    /** Elimina una ubicación (lanza excepción si falla) */
    suspend fun eliminar(id: Int) {
        val resp = api.deleteUbicacion(id)
        if (!resp.isSuccessful) throw HttpException(resp)
    }
}

/** Helper para desempaquetar o lanzar HttpException */
private fun <T> Response<T>.bodyOrThrow(): T {
    if (isSuccessful) {
        val b = body()
        if (b != null) return b
    }
    throw HttpException(this)
}
