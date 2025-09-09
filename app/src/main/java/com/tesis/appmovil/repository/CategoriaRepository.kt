package com.tesis.appmovil.repository

import com.tesis.appmovil.data.remote.ApiService
import com.tesis.appmovil.data.remote.RetrofitClient
import com.tesis.appmovil.data.remote.dto.CategoriaCreate
import com.tesis.appmovil.data.remote.dto.CategoriaUpdate
import com.tesis.appmovil.models.Categoria
import retrofit2.HttpException
import retrofit2.Response

class CategoriaRepository(
    private val api: ApiService = RetrofitClient.api
) {
    /** Lista todas las categorías */
    suspend fun listar(): List<Categoria> = api.getCategorias().bodyOrThrow()

    /** Obtiene una categoría por id */
    suspend fun obtener(id: Int): Categoria = api.getCategoria(id).bodyOrThrow()

    /** Crea una categoría y devuelve la creada */
    suspend fun crear(body: CategoriaCreate): Categoria = api.createCategoria(body).bodyOrThrow()

    /** Actualiza y devuelve la categoría resultante */
    suspend fun actualizar(id: Int, body: CategoriaUpdate): Categoria =
        api.updateCategoria(id, body).bodyOrThrow()

    /** Elimina una categoría (lanza excepción si falla) */
    suspend fun eliminar(id: Int) {
        val resp = api.deleteCategoria(id)
        if (!resp.isSuccessful) throw HttpException(resp)
    }
}

/* ------------------ Helper ------------------ */

/** Devuelve el body si es exitoso; si no, lanza HttpException */
private fun <T> Response<T>.bodyOrThrow(): T {
    if (isSuccessful) {
        val b = body()
        if (b != null) return b
    }
    throw HttpException(this)
}
