// NegocioImagenRepository.kt
package com.tesis.appmovil.repository

import com.tesis.appmovil.data.remote.ApiService
import com.tesis.appmovil.data.remote.RetrofitClient
import com.tesis.appmovil.data.remote.dto.NegocioImagenCreate
import com.tesis.appmovil.data.remote.dto.NegocioImagenUpdate
import com.tesis.appmovil.models.NegocioImagen
import retrofit2.HttpException
import retrofit2.Response

class NegocioImagenRepository(
    private val api: ApiService = RetrofitClient.api
) {
    /** Lista imágenes; opcionalmente filtra por id_negocio */
    suspend fun listar(idNegocio: Int? = null): List<NegocioImagen> =
        api.getNegocioImagenes(idNegocio).bodyOrThrow()

    /** Obtiene una imagen por id */
    suspend fun obtener(id: Int): NegocioImagen =
        api.getNegocioImagen(id).bodyOrThrow()

    /** Crea y devuelve la imagen creada */
    suspend fun crear(body: NegocioImagenCreate): NegocioImagen =
        api.createNegocioImagen(body).bodyOrThrow()

    /** Actualiza y devuelve la imagen */
    suspend fun actualizar(id: Int, body: NegocioImagenUpdate): NegocioImagen =
        api.updateNegocioImagen(id, body).bodyOrThrow()

    /** Elimina la imagen (lanza excepción si falla) */
    suspend fun eliminar(id: Int) {
        val resp = api.deleteNegocioImagen(id)
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
