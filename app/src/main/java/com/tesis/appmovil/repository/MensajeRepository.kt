package com.tesis.appmovil.repository

import com.tesis.appmovil.data.remote.ApiService
import com.tesis.appmovil.data.remote.RetrofitClient
import com.tesis.appmovil.data.remote.dto.MensajeCreate
import com.tesis.appmovil.data.remote.dto.MensajeUpdate
import com.tesis.appmovil.models.Mensaje
import retrofit2.HttpException
import retrofit2.Response

class MensajeRepository(
    private val api: ApiService = RetrofitClient.api
) {
    /** Lista mensajes; opcionalmente por usuario */
    suspend fun listar(idUsuario: Int? = null): List<Mensaje> =
        api.getMensajes(idUsuario).bodyOrThrow()

    /** Obtiene un mensaje por id */
    suspend fun obtener(id: Int): Mensaje =
        api.getMensaje(id).bodyOrThrow()

    /** Crea y devuelve el mensaje creado */
    suspend fun crear(body: MensajeCreate): Mensaje =
        api.createMensaje(body).bodyOrThrow()

    /** Actualiza y devuelve el mensaje actualizado */
    suspend fun actualizar(id: Int, body: MensajeUpdate): Mensaje =
        api.updateMensaje(id, body).bodyOrThrow()

    /** Elimina (lanza excepción si falla) */
    suspend fun eliminar(id: Int) {
        val resp = api.deleteMensaje(id)
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
