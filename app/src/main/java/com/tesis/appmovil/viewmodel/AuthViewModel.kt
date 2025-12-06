package com.tesis.appmovil.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.appmovil.data.remote.request.GoogleLoginRequest
import com.tesis.appmovil.data.remote.request.LoginRequest
import com.tesis.appmovil.data.remote.RetrofitClient
import com.tesis.appmovil.data.remote.dto.UsuarioCreate
import com.tesis.appmovil.data.remote.request.RegisterRequest
import com.tesis.appmovil.models.UserRole
import com.tesis.appmovil.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.tesis.appmovil.repository.NegocioRepository
import kotlinx.coroutines.flow.update

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
    val negocioId: Int? = null,   // 👈 NUEVO
    val expiry: Long? = null  // <-- NUEVO



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
    // En AuthViewModel.kt - REEMPLAZA la función register actual por esta:
    // En AuthViewModel.kt - ACTUALIZA la función register
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
                // 1. Registrar usuario
                val response = RetrofitClient.api.register(
                    RegisterRequest(
                        nombre = nombre,
                        apellidoPaterno = apellidoPaterno,
                        apellidoMaterno = apellidoMaterno,
                        correo = email,
                        contrasena = password,
                        fechaNacimiento = fechaNacimiento,
                        fotoPerfil = fotoPerfil
                    )
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val body = response.body()!!
                    val userData = body.data

                    if (userData != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            user = userData.correo,
                            userId = userData.idUsuario,
                            // Estos campos no vienen en la respuesta de registro
                            token = null,
                            hasBusiness = false,
                            negocioId = null
                        )

                        println("✅ REGISTRO EXITOSO - Usuario: ${userData.correo}, ID: ${userData.idUsuario}")
                        println("📧 Mensaje del backend: ${body.message}")

                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Error: Datos del usuario no recibidos"
                        )
                    }

                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = response.body()?.message ?: "Error en el registro"
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

    fun login(context: Context) {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Correo y contraseña requeridos")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            try {
                val resp = RetrofitClient.api.login(LoginRequest(state.email, state.password))
                if (resp.isSuccessful && resp.body()?.success == true) {
                    val usuario = resp.body()!!.data!!.usuario
                    val token = resp.body()!!.data!!.token
//                    val expiry = resp.body()!!.data!!.expiry
                    val expiry = System.currentTimeMillis() + 7*24*60*60*1000 // 7 días en ms

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = usuario.correo,
                        userId = usuario.idUsuario,
                        token = token,
                        hasBusiness = usuario.negocioId != null,
                        negocioId = usuario.negocioId,
                        expiry = expiry
                    )

                    // Guardar token en SharedPreferences si quieres persistir sesión
                    saveToken(context, token, expiry)

                    // Configurar Retrofit para usar el token
                    RetrofitClient.setTokenProvider { _uiState.value.token }
                }

//                if (response.isSuccessful && response.body()?.success == true) {
//                    val body = response.body()!!
//                    val userData = body.data!!.usuario
//                    val token = body.data!!.token
//                    // DEBUG CRÍTICO
//                    println("🔍 DEBUG AuthViewModel - UserData completo:")
//                    println("   - idUsuario: ${userData.idUsuario}")
//                    println("   - correo: ${userData.correo}")
//                    println("   - negocioId: ${userData.negocioId}")
//                    println("   - tiene negocio: ${userData.negocioId != null}")
//
//                    // 👇 YA NO LLAMES A usuarioTieneNegocio()
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        user = userData.correo,
//                        userId = userData.idUsuario,
//                        token = token,
//                        hasBusiness = userData.negocioId != null,
//                        negocioId = userData.negocioId
//                    )
//
//                    // Configurar Retrofit con el token
//                    RetrofitClient.setTokenProvider { _uiState.value.token }
//                }
                else {
                    _uiState.value = state.copy(
                        isLoading = false,
                        error = resp.body()?.message ?: "Error al iniciar sesión"
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

    // En tu AuthViewModel.kt
    fun actualizarNegocioId(negocioId: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                negocioId = negocioId,
                hasBusiness = true
            )
        }
    }
    fun saveToken(context: Context, token: String, expiry: Long) {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("token", token)
            .putLong("expiry", expiry)
            .apply()
    }

    fun loadToken(context: Context) {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)
        val expiry = prefs.getLong("expiry", 0L)

        if (token != null && System.currentTimeMillis() < expiry) {
            _uiState.value = _uiState.value.copy(token = token)
            RetrofitClient.setTokenProvider { token }
        } else {
            logout(context)
        }
    }
    fun logout(context: Context) {
        _uiState.value = AuthUiState()
        RetrofitClient.setTokenProvider { null }
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .edit().clear().apply()
    }
//    fun logout() {
//        // Limpia los datos del usuario en el estado
//        _uiState.value = AuthUiState()
//
//        // Si usas Firebase (opcional, si tu login usa Firebase)
//        // FirebaseAuth.getInstance().signOut()
//
//        // Si usas token en Retrofit, puedes limpiarlo también
//        RetrofitClient.setTokenProvider { null }
//
//        println("👋 Sesión cerrada correctamente")
//    }

}
