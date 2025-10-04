package com.tesis.appmovil.repository

import com.tesis.appmovil.data.remote.ApiService
import com.tesis.appmovil.data.remote.RetrofitClient
import com.tesis.appmovil.data.remote.dto.ChangePasswordRequest
import com.tesis.appmovil.data.remote.dto.UsuarioCreate
import com.tesis.appmovil.data.remote.dto.UsuarioUpdate
import com.tesis.appmovil.models.Usuario
import retrofit2.HttpException
import retrofit2.Response

class UsuarioRepository(
    private val api: ApiService = RetrofitClient.api
) {
    /** Lista todos los usuarios */
    suspend fun listar(): List<Usuario> {
        val resp = api.getUsuarios()
        if (resp.isSuccessful) {
            val body = resp.body()
            if (body != null && body.success && body.data != null) {
                return body.data
            }
        }
        throw HttpException(resp)
    }

    /** Obtiene un usuario por id */
    suspend fun obtener(id: Int): Usuario {
        val resp = api.getUsuario(id)
        if (resp.isSuccessful) {
            val body = resp.body()
            if (body != null && body.success && body.data != null) {
                return body.data
            }
        }
        throw HttpException(resp)
    }

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

    suspend fun cambiarContrasena(id: Int, current: String, nueva: String) {
        val resp = api.changePassword(id, ChangePasswordRequest(current, nueva))
        if (resp.isSuccessful && resp.body()?.success == true) {
            return
        } else {
            throw HttpException(resp)
        }
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
