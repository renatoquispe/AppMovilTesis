package com.tesis.appmovil.repository

import com.tesis.appmovil.data.remote.RetrofitClient
import com.tesis.appmovil.data.remote.request.*

class PasswordRecoveryRepository {

    suspend fun solicitarCodigoRecuperacion(email: String) =
        RetrofitClient.api.solicitarCodigoRecuperacion(
            SolicitarCodigoRequest(email)
        )

    suspend fun verificarCodigo(email: String, codigo: String) =
        RetrofitClient.api.verificarCodigoRecuperacion(
            VerificarCodigoRequest(email, codigo)
        )

    suspend fun restablecerContrasena(email: String, codigo: String, nuevaContrasena: String) =
        RetrofitClient.api.restablecerContrasena(
            RestablecerContrasenaRequest(email, codigo, nuevaContrasena)
        )



}