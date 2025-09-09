package com.tesis.appmovil.repository

import com.tesis.appmovil.data.remote.ApiService
import com.tesis.appmovil.data.remote.RetrofitClient
import com.tesis.appmovil.data.remote.dto.UsuarioCreate
import com.tesis.appmovil.data.remote.dto.UsuarioUpdate
import com.tesis.appmovil.models.Usuario
import retrofit2.HttpException
import retrofit2.Response

class UsuarioRepository(
    private val api: ApiService = RetrofitClient.api
) {
    /** Lista todos los usuarios */
    suspend fun listar(): List<Usuario> = api.getUsuarios().bodyOrThrow()

    /** Obtiene un usuario por id */
    suspend fun obtener(id: Int): Usuario = api.getUsuario(id).bodyOrThrow()

    /** Crea un usuario y devuelve el creado */
    suspend fun crear(body: UsuarioCreate): Usuario = api.createUsuario(body).bodyOrThrow()

    /** Actualiza un usuario y devuelve el actualizado */
    suspend fun actualizar(id: Int, body: UsuarioUpdate): Usuario =
        api.updateUsuario(id, body).bodyOrThrow()

    /** Elimina un usuario (lanza excepción si falla) */
    suspend fun eliminar(id: Int) {
        val resp = api.deleteUsuario(id)
        if (!resp.isSuccessful) throw HttpException(resp)
    }
}

/* ------------------ Helpers ------------------ */

/** Devuelve body si es exitoso; si no, lanza HttpException */
private fun <T> Response<T>.bodyOrThrow(): T {
    if (isSuccessful) {
        val b = body()
        if (b != null) return b
    }
    throw HttpException(this)
}
