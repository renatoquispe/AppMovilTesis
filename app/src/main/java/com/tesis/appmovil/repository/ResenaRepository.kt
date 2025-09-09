// ResenaRepository.kt
package com.tesis.appmovil.repository

import com.tesis.appmovil.data.remote.ApiService
import com.tesis.appmovil.data.remote.RetrofitClient
import com.tesis.appmovil.data.remote.dto.ResenaCreate
import com.tesis.appmovil.data.remote.dto.ResenaUpdate
import com.tesis.appmovil.models.Resena
import retrofit2.HttpException
import retrofit2.Response

class ResenaRepository(
    private val api: ApiService = RetrofitClient.api
) {
    /** Lista reseñas; opcional por negocio o usuario */
    suspend fun listar(idNegocio: Int? = null, idUsuario: Int? = null): List<Resena> =
        api.getResenas(idNegocio, idUsuario).bodyOrThrow()

    /** Obtiene una reseña por id */
    suspend fun obtener(id: Int): Resena =
        api.getResena(id).bodyOrThrow()

    /** Crea y devuelve la reseña creada */
    suspend fun crear(body: ResenaCreate): Resena =
        api.createResena(body).bodyOrThrow()

    /** Actualiza y devuelve la reseña */
    suspend fun actualizar(id: Int, body: ResenaUpdate): Resena =
        api.updateResena(id, body).bodyOrThrow()

    /** Elimina (lanza excepción si falla) */
    suspend fun eliminar(id: Int) {
        val resp = api.deleteResena(id)
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
