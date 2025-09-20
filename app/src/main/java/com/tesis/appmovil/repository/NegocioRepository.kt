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
        page: Int = 1,
        limit: Int = 20
    ): List<Negocio> =
        api.getNegocios(idCategoria, idUbicacion, q, page, limit)
            .bodyOrThrow()
            .data?.items ?: emptyList()

    /** Toma los primeros N (si quieres 1 para “abajo”) */
    suspend fun listarDestacados(limit: Int = 1): List<Negocio> =
        listar(page = 1, limit = limit)

    /** Detalle (tu backend envía ApiResponse<Negocio>) */
    suspend fun obtener(id: Int): Negocio =
        api.getNegocio(id).bodyOrThrow().data
            ?: throw IllegalStateException("Negocio no encontrado")

    /** Detalle “extendido” (DTO específico) */
    suspend fun obtenerDetalle(id: Int): NegocioResponse =
        api.getNegocioDetalle(id).bodyOrThrow().data
            ?: throw IllegalStateException("Negocio no encontrado")

    /** CRUD básico */
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
}

/* ==== Helper para desempaquetar ApiResponse<T> ==== */
private fun <T> Response<ApiResponse<T>>.bodyOrThrow(): ApiResponse<T> {
    if (isSuccessful) body()?.let { return it }
    throw HttpException(this)
}
