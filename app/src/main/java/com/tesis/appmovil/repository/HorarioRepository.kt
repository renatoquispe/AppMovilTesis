package com.tesis.appmovil.repository

import com.tesis.appmovil.data.remote.ApiService
import com.tesis.appmovil.data.remote.RetrofitClient
import com.tesis.appmovil.data.remote.dto.HorarioCreate
import com.tesis.appmovil.data.remote.dto.HorarioUpdate
import com.tesis.appmovil.models.Horario
import retrofit2.HttpException
import retrofit2.Response

class HorarioRepository(
    private val api: ApiService = RetrofitClient.api
) {
    suspend fun listar(idNegocio: Int? = null): List<Horario> =
        api.getHorarios(idNegocio).bodyOrThrow()

    suspend fun obtener(id: Int): Horario =
        api.getHorario(id).bodyOrThrow()

    suspend fun crear(body: HorarioCreate): Horario =
        api.createHorario(body).bodyOrThrow()

    suspend fun actualizar(id: Int, body: HorarioUpdate): Horario =
        api.updateHorario(id, body).bodyOrThrow()

    suspend fun eliminar(id: Int) {
        val resp = api.deleteHorario(id)
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
