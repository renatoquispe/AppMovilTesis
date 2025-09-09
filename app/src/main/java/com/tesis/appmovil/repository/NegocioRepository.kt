// NegocioRepository (ajustado)
package com.tesis.appmovil.repository

import com.tesis.appmovil.data.remote.ApiService
import com.tesis.appmovil.data.remote.RetrofitClient
import com.tesis.appmovil.data.remote.dto.NegocioCreate
import com.tesis.appmovil.data.remote.dto.NegocioUpdate
import com.tesis.appmovil.models.Negocio
import retrofit2.HttpException
import retrofit2.Response

class NegocioRepository(
    private val api: ApiService = RetrofitClient.api
) {
    suspend fun listar(
        idCategoria: Int? = null,
        idUbicacion: Int? = null,
        q: String? = null
    ): List<Negocio> = api.getNegocios(idCategoria, idUbicacion, q).bodyOrThrow()

    suspend fun obtener(id: Int): Negocio = api.getNegocio(id).bodyOrThrow()

    suspend fun crear(body: NegocioCreate): Negocio =
        api.createNegocio(body).bodyOrThrow()

    suspend fun actualizar(id: Int, body: NegocioUpdate): Negocio =
        api.updateNegocio(id, body).bodyOrThrow()

    suspend fun eliminar(id: Int) {
        val resp = api.deleteNegocio(id)
        if (!resp.isSuccessful) throw HttpException(resp)
    }
}

/* Helper compartido con los demás repos */
private fun <T> Response<T>.bodyOrThrow(): T {
    if (isSuccessful) {
        val b = body()
        if (b != null) return b
    }
    throw HttpException(this)
}
