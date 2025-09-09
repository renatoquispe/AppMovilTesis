package com.tesis.appmovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import retrofit2.HttpException
import java.io.IOException
import com.tesis.appmovil.data.remote.request.LoginRequest
import com.tesis.appmovil.data.remote.request.LoginResponse
import com.tesis.appmovil.data.remote.RetrofitClient
import retrofit2.awaitResponse

class LoginViewModel : ViewModel() {

    fun login(loginRequest: LoginRequest) = liveData<Result<LoginResponse>>(Dispatchers.IO) {
        try {
            // 1. Llamada directa, sin awaitResponse()
            val resp = RetrofitClient.api.login(loginRequest)

            // 2. Procesar la respuesta
            if (resp.isSuccessful) {
                val body = resp.body() ?: throw IOException("Respuesta vacía")
                emit(Result.success(body))
            } else {
                emit(Result.failure(IOException("HTTP ${resp.code()} - ${resp.errorBody()?.string()}")))
            }
        } catch (e: HttpException) {
            emit(Result.failure(e))
        } catch (e: IOException) {
            emit(Result.failure(e))
        }
    }
}