//// UbicacionRepository.kt
//package com.tesis.appmovil.repository
//
//import com.tesis.appmovil.data.remote.ApiService
//import com.tesis.appmovil.data.remote.RetrofitClient
//import com.tesis.appmovil.data.remote.dto.UbicacionCreate
//import com.tesis.appmovil.data.remote.dto.UbicacionUpdate
//import com.tesis.appmovil.models.Ubicacion
//import retrofit2.HttpException
//import retrofit2.Response
//
//class UbicacionRepository(
//    private val api: ApiService = RetrofitClient.api
//) {
//    /** Lista todas las ubicaciones */
//    suspend fun listar(): List<Ubicacion> =
//        api.getUbicaciones().bodyOrThrow()
//
//    /** Obtiene una ubicación por id */
//    suspend fun obtener(id: Int): Ubicacion =
//        api.getUbicacion(id).bodyOrThrow()
//
//    /** Crea y devuelve la ubicación creada */
//    suspend fun crear(body: UbicacionCreate): Ubicacion =
//        api.createUbicacion(body).bodyOrThrow()
//
//    /** Actualiza y devuelve la ubicación actualizada */
//    suspend fun actualizar(id: Int, body: UbicacionUpdate): Ubicacion =
//        api.updateUbicacion(id, body).bodyOrThrow()
//
//    /** Elimina una ubicación (lanza excepción si falla) */
//    suspend fun eliminar(id: Int) {
//        val resp = api.deleteUbicacion(id)
//        if (!resp.isSuccessful) throw HttpException(resp)
//    }
//}
//
///** Helper para desempaquetar o lanzar HttpException */
//private fun <T> Response<T>.bodyOrThrow(): T {
//    if (isSuccessful) {
//        val b = body()
//        if (b != null) return b
//    }
//    throw HttpException(this)
//}
// UbicacionRepository.kt - Versión corregida
package com.tesis.appmovil.repository

import com.tesis.appmovil.data.remote.ApiService
import com.tesis.appmovil.data.remote.RetrofitClient
import com.tesis.appmovil.data.remote.dto.UbicacionCreate
import com.tesis.appmovil.data.remote.dto.UbicacionUpdate
import com.tesis.appmovil.models.Ubicacion
import retrofit2.HttpException
import retrofit2.Response

// IMPORTANTE: Agrega este import
import com.tesis.appmovil.data.remote.ApiResponse

class UbicacionRepository(
    private val api: ApiService = RetrofitClient.api
) {
    /** Lista todas las ubicaciones */
    suspend fun listar(): List<Ubicacion> {
        val response = api.getUbicaciones()
        if (response.isSuccessful) {
            val apiResponse = response.body()
            if (apiResponse != null && apiResponse.success) {
                return apiResponse.data ?: emptyList()
            } else {
                throw Exception(apiResponse?.message ?: "Error desconocido al obtener ubicaciones")
            }
        } else {
            throw HttpException(response)
        }
    }

    /** Obtiene una ubicación por id */
    suspend fun obtener(id: Int): Ubicacion {
        val response = api.getUbicacion(id)
        if (response.isSuccessful) {
            val apiResponse = response.body()
            if (apiResponse != null && apiResponse.success) {
                return apiResponse.data ?: throw Exception("Ubicación no encontrada")
            } else {
                throw Exception(apiResponse?.message ?: "Error al obtener la ubicación")
            }
        } else {
            throw HttpException(response)
        }
    }

    /** Crea y devuelve la ubicación creada */
    suspend fun crear(body: UbicacionCreate): Ubicacion {
        val response = api.createUbicacion(body)
        if (response.isSuccessful) {
            val apiResponse = response.body()
            if (apiResponse != null && apiResponse.success) {
                return apiResponse.data ?: throw Exception("No se recibió la ubicación creada")
            } else {
                throw Exception(apiResponse?.message ?: "Error al crear la ubicación")
            }
        } else {
            throw HttpException(response)
        }
    }

    /** Actualiza y devuelve la ubicación actualizada */
    suspend fun actualizar(id: Int, body: UbicacionUpdate): Ubicacion {
        val response = api.updateUbicacion(id, body)
        if (response.isSuccessful) {
            val apiResponse = response.body()
            if (apiResponse != null && apiResponse.success) {
                return apiResponse.data ?: throw Exception("No se recibió la ubicación actualizada")
            } else {
                throw Exception(apiResponse?.message ?: "Error al actualizar la ubicación")
            }
        } else {
            throw HttpException(response)
        }
    }

    /** Elimina una ubicación (lanza excepción si falla) */
    suspend fun eliminar(id: Int) {
        val response = api.deleteUbicacion(id)
        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        val apiResponse = response.body()
        if (apiResponse != null && !apiResponse.success) {
            throw Exception(apiResponse.message ?: "Error al eliminar la ubicación")
        }
    }
}