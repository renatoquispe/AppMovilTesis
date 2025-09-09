package com.tesis.appmovil.repository

import com.tesis.appmovil.data.remote.ApiService
import com.tesis.appmovil.data.remote.RetrofitClient
import com.tesis.appmovil.data.remote.dto.ImagenResenaCreate
import com.tesis.appmovil.data.remote.dto.ImagenResenaUpdate
import com.tesis.appmovil.models.ImagenResena
import retrofit2.HttpException
import retrofit2.Response

class ImagenResenaRepository(
    private val api: ApiService = RetrofitClient.api
) {
    /** Lista imágenes; opcionalmente filtra por id_resena */
    suspend fun listar(idResena: Int? = null): List<ImagenResena> =
        api.getImagenesResena(idResena).bodyOrThrow()

    /** Obtiene imagen por id */
    suspend fun obtener(id: Int): ImagenResena =
        api.getImagenResena(id).bodyOrThrow()

    /** Crea y devuelve la imagen creada */
    suspend fun crear(body: ImagenResenaCreate): ImagenResena =
        api.createImagenResena(body).bodyOrThrow()

    /** Actualiza y devuelve la imagen */
    suspend fun actualizar(id: Int, body: ImagenResenaUpdate): ImagenResena =
        api.updateImagenResena(id, body).bodyOrThrow()

    /** Elimina (lanza excepción si falla) */
    suspend fun eliminar(id: Int) {
        val resp = api.deleteImagenResena(id)
        if (!resp.isSuccessful) throw HttpException(resp)
    }
}

/* Helper compartido */
private fun <T> Response<T>.bodyOrThrow(): T {
    if (isSuccessful) {
        val b = body()
        if (b != null) return b
    }
    throw HttpException(this)
}
