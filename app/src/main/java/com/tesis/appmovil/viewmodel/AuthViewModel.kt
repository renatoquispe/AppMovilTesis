package com.tesis.appmovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.appmovil.data.remote.request.GoogleLoginRequest
import com.tesis.appmovil.data.remote.request.LoginRequest
import com.tesis.appmovil.data.remote.RetrofitClient
import com.tesis.appmovil.data.remote.dto.UsuarioCreate
import com.tesis.appmovil.models.UserRole
import com.tesis.appmovil.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.tesis.appmovil.repository.NegocioRepository

data class AuthUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: String? = null,
    val userId: Int? = null,           // ← NUEVO: ID del usuario
    val role: UserRole? = null,
    val token: String? = null, //probando esto Susan
    val hasBusiness: Boolean = false,   // 👈 NUEVO
    val negocioId: Int? = null   // 👈 NUEVO


)

class AuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    private val usuarioRepo = UsuarioRepository()
    private val negocioRepo = NegocioRepository() // ← Agregar el repositorio de negocios


    fun onUpdateName(v: String)    { _uiState.value = _uiState.value.copy(name = v,    error = null) }
    fun onEmailChange(v: String)   { _uiState.value = _uiState.value.copy(email = v,   error = null) }
    fun onPasswordChange(v: String){ _uiState.value = _uiState.value.copy(password = v,error = null) }
    fun chooseRole(role: UserRole)  { _uiState.value = _uiState.value.copy(role = role) }

    /** Limpia flags transitorios (user y error) para que el LaunchedEffect funcione */
    fun clearTransient() {
        _uiState.value = _uiState.value.copy(user = null, error = null)
    }

    /** Registro local */

    /** Registro con login automático - VERSIÓN CORRECTA */
    fun register(
        nombre: String,
        email: String,
        password: String,
        apellidoPaterno: String = "",
        apellidoMaterno: String = "",
        fechaNacimiento: String = "2000-01-01",
        fotoPerfil: String? = null
    ) {
        val state = _uiState.value
        if (nombre.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = state.copy(error = "Completa nombre, correo y contraseña")
            return
        }
        _uiState.value = state.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                // 1. Registrar usuario - Si falla, lanzará excepción automáticamente
                val usuarioCreado = usuarioRepo.crear(
                    UsuarioCreate(
                        nombre = nombre,
                        apellidoPaterno = apellidoPaterno,
                        apellidoMaterno = apellidoMaterno,
                        correo = email,
                        contrasena = password,
                        fechaNacimiento = fechaNacimiento,
                        fotoPerfil = fotoPerfil
                    )
                )

                println("✅ Usuario registrado: ${usuarioCreado.correo}")

                // 2. Hacer login automático después del registro exitoso
                val loginResponse = RetrofitClient.api.login(LoginRequest(email, password))

                if (loginResponse.isSuccessful && loginResponse.body()?.success == true) {
                    val body = loginResponse.body()!!
                    val userData = body.data!!.usuario
                    val token = body.data!!.token

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = userData.correo,
                        userId = userData.idUsuario,
                        token = token
                    )

                    // Configurar Retrofit con el token
                    RetrofitClient.setTokenProvider { _uiState.value.token }

                    println("✅ REGISTRO + LOGIN EXITOSO - Token: $token")

                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Registro exitoso, pero error en login: ${loginResponse.body()?.message}"
                    )
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error: ${e.message ?: "No se pudo registrar el usuario"}"
                )
            }
        }
    }


    /** Login tradicional */
    // En tu AuthViewModel, modifica la función login() para verificar si tiene negocio

    fun login() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Correo y contraseña requeridos")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            try {
                val response = RetrofitClient.api.login(LoginRequest(state.email, state.password))
                if (response.isSuccessful && response.body()?.success == true) {
                    val body = response.body()!!
                    val userData = body.data!!.usuario
                    val token = body.data!!.token

                    // 👇 YA NO LLAMES A usuarioTieneNegocio()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = userData.correo,
                        userId = userData.idUsuario,
                        token = token,
                        hasBusiness = userData.negocioId != null,
                        negocioId = userData.negocioId
                    )

                    // Configurar Retrofit con el token
                    RetrofitClient.setTokenProvider { _uiState.value.token }
                } else {
                    _uiState.value = state.copy(
                        isLoading = false,
                        error = response.body()?.message ?: "Error al iniciar sesión"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = e.message ?: "Error de red"
                )
            }
        }
    }


//    fun login() {
//        val state = _uiState.value
//        if (state.email.isBlank() || state.password.isBlank()) {
//            _uiState.value = state.copy(error = "Correo y contraseña requeridos")
//            return
//        }
//        viewModelScope.launch {
//            _uiState.value = state.copy(isLoading = true, error = null)
//            try {
//                val response = RetrofitClient.api.login(LoginRequest(state.email, state.password))
//                if (response.isSuccessful && response.body()?.success == true) {
////                    val userData = response.body()!!.data!!.usuario //cambios susan
//
//
//                    val body = response.body()!!
//                    val userData = body.data!!.usuario
//                    val token = body.data!!.token  // 👉 aquí lo recibes
//
//
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        user = userData.correo,
//                        userId = userData.idUsuario,
//                        token = token
//
//                    )
//                    // 👇 Aquí conectas el token con Retrofit
//                    RetrofitClient.setTokenProvider { _uiState.value.token }
//                } else {
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        error = response.body()?.message ?: "Error al iniciar sesión"
//                    )
//                }
//            } catch (e: Exception) {
//                _uiState.value = _uiState.value.copy(
//                    isLoading = false,
//                    error = e.message ?: "Error de red"
//                )
//            }
//        }
//    }

    /** Login con Google */
    fun loginWithGoogle(idToken: String) {
        val state = _uiState.value
        _uiState.value = state.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val resp = RetrofitClient.api.loginWithGoogle(GoogleLoginRequest(idToken))
                if (resp.isSuccessful && resp.body()?.success == true) {
                    val usuario = resp.body()!!.data!!.usuario
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = usuario.correo,
                        userId = usuario.idUsuario
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = resp.body()?.message ?: "No se pudo iniciar sesión con Google"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error de red en Google Sign-In"
                )
            }
        }
    }

//    suspend fun usuarioTieneNegocio(idUsuario: Int): Boolean {
//        return try {
//            val negocios = negocioRepo.obtenerNegociosPorUsuario(idUsuario)
//            negocios.isNotEmpty()
//        } catch (e: Exception) {
//            println("❌ Error verificando negocios del usuario: ${e.message}")
//            false
//        }
//    }
}
