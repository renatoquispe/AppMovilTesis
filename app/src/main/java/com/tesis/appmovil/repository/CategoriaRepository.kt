//package com.tesis.appmovil.repository
//
//import com.tesis.appmovil.data.remote.ApiService
//import com.tesis.appmovil.data.remote.RetrofitClient
//import com.tesis.appmovil.data.remote.dto.CategoriaCreate
//import com.tesis.appmovil.data.remote.dto.CategoriaUpdate
//import com.tesis.appmovil.models.Categoria
//import retrofit2.HttpException
//import retrofit2.Response
//
//class CategoriaRepository(
//    private val api: ApiService = RetrofitClient.api
//) {
//    /** Lista todas las categorías */
//    suspend fun listar(): List<Categoria> = api.getCategorias().bodyOrThrow()
//
//    /** Obtiene una categoría por id */
//    suspend fun obtener(id: Int): Categoria = api.getCategoria(id).bodyOrThrow()
//
//    /** Crea una categoría y devuelve la creada */
//    suspend fun crear(body: CategoriaCreate): Categoria = api.createCategoria(body).bodyOrThrow()
//
//    /** Actualiza y devuelve la categoría resultante */
//    suspend fun actualizar(id: Int, body: CategoriaUpdate): Categoria =
//        api.updateCategoria(id, body).bodyOrThrow()
//
//    /** Elimina una categoría (lanza excepción si falla) */
//    suspend fun eliminar(id: Int) {
//        val resp = api.deleteCategoria(id)
//        if (!resp.isSuccessful) throw HttpException(resp)
//    }
//}
//
///* ------------------ Helper ------------------ */
//
///** Devuelve el body si es exitoso; si no, lanza HttpException */
//private fun <T> Response<T>.bodyOrThrow(): T {
//    if (isSuccessful) {
//        val b = body()
//        if (b != null) return b
//    }
//    throw HttpException(this)
//}
// CategoriaRepository.kt - Versión corregida
package com.tesis.appmovil.repository

import com.tesis.appmovil.data.remote.ApiService
import com.tesis.appmovil.data.remote.RetrofitClient
import com.tesis.appmovil.data.remote.ApiResponse // ← IMPORT DIRECTO
import com.tesis.appmovil.data.remote.dto.CategoriaCreate
import com.tesis.appmovil.data.remote.dto.CategoriaUpdate
import com.tesis.appmovil.models.Categoria
import retrofit2.HttpException
import retrofit2.Response

class CategoriaRepository(
    private val api: ApiService = RetrofitClient.api
) {
    /** Lista todas las categorías */
    suspend fun listar(): List<Categoria> {
        val response = api.getCategorias()
        if (response.isSuccessful) {
            val apiResponse = response.body()
            if (apiResponse != null && apiResponse.success) {
                return apiResponse.data ?: emptyList()
            } else {
                throw Exception(apiResponse?.message ?: "Error desconocido al obtener categorías")
            }
        } else {
            throw HttpException(response)
        }
    }

    /** Obtiene una categoría por id */
    suspend fun obtener(id: Int): Categoria {
        val response = api.getCategoria(id)
        if (response.isSuccessful) {
            val apiResponse = response.body()
            if (apiResponse != null && apiResponse.success) {
                return apiResponse.data ?: throw Exception("Categoría no encontrada")
            } else {
                throw Exception(apiResponse?.message ?: "Error al obtener la categoría")
            }
        } else {
            throw HttpException(response)
        }
    }

    /** Crea una categoría y devuelve la creada */
    suspend fun crear(body: CategoriaCreate): Categoria {
        val response = api.createCategoria(body)
        if (response.isSuccessful) {
            val apiResponse = response.body()
            if (apiResponse != null && apiResponse.success) {
                return apiResponse.data ?: throw Exception("No se recibió la categoría creada")
            } else {
                throw Exception(apiResponse?.message ?: "Error al crear la categoría")
            }
        } else {
            throw HttpException(response)
        }
    }

    /** Actualiza y devuelve la categoría resultante */
    suspend fun actualizar(id: Int, body: CategoriaUpdate): Categoria {
        val response = api.updateCategoria(id, body)
        if (response.isSuccessful) {
            val apiResponse = response.body()
            if (apiResponse != null && apiResponse.success) {
                return apiResponse.data ?: throw Exception("No se recibió la categoría actualizada")
            } else {
                throw Exception(apiResponse?.message ?: "Error al actualizar la categoría")
            }
        } else {
            throw HttpException(response)
        }
    }

    /** Elimina una categoría (lanza excepción si falla) */
    suspend fun eliminar(id: Int) {
        val response = api.deleteCategoria(id)
        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        val apiResponse = response.body()
        if (apiResponse != null && !apiResponse.success) {
            throw Exception(apiResponse.message ?: "Error al eliminar la categoría")
        }

    }
}