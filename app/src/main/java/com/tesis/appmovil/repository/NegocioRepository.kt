package com.tesis.appmovil.repository

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
    /** Lista de negocios con filtros opcionales (paginated) */
    suspend fun listar(
        idCategoria: Int? = null,
        idUbicacion: Int? = null,
        q: String? = null,
        distrito: String? = null,
        ciudad: String? = null,
        activos: Boolean? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): List<Negocio> =
        api.getNegocios(
            idCategoria = idCategoria,
            idUbicacion = idUbicacion,
            q = q,
            distrito = distrito,
            ciudad = ciudad,
            activos = activos,
            page = page,
            pageSize = pageSize
        ).bodyOrThrow().data?.items ?: emptyList()

    /** Primeros N (para destacados) */
    suspend fun listarDestacados(limit: Int = 1): List<Negocio> =
        listar(page = 1, pageSize = limit)

    /** Detalle simple */
    suspend fun obtener(id: Int): Negocio =
        api.getNegocio(id).bodyOrThrow().data
            ?: throw IllegalStateException("Negocio no encontrado")

    /** Detalle extendido (DTO) */
    suspend fun obtenerDetalle(id: Int): NegocioResponse =
        api.getNegocioDetalle(id).bodyOrThrow().data
            ?: throw IllegalStateException("Negocio no encontrado")

    /** CRUD */
    suspend fun crear(body: NegocioCreate): Negocio =
        api.createNegocio(body).bodyOrThrow().data
            ?: throw IllegalStateException("Error al crear negocio")

    suspend fun actualizar(id: Int, body: NegocioUpdate): Negocio {
        val response = api.updateNegocio(id, body).bodyOrThrow()
        return response.data ?: obtener(id)
    }

    suspend fun eliminar(id: Int) {
        val resp = api.deleteNegocio(id)
        if (!resp.isSuccessful) throw HttpException(resp)
    }

    suspend fun obtenerNegociosPorUsuario(idUsuario: Int): List<Negocio> {
        val response = api.getNegociosPorUsuario(idUsuario)
        return if (response.isSuccessful) {
            response.body()?.data ?: emptyList()
        } else {
            emptyList()
        }
    }
}

/* ==== Helper para desempaquetar ApiResponse<T> ==== */
private fun <T> Response<ApiResponse<T>>.bodyOrThrow(): ApiResponse<T> {
    if (!isSuccessful) throw HttpException(this)
    val body = body() ?: throw IllegalStateException("Respuesta vacía del servidor")
    return body
}
