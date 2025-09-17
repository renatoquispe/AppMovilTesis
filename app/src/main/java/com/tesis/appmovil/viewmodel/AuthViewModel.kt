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

data class AuthUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: String? = null,          // guardamos solo el correo
    val role: UserRole? = null,
    val token: String? = null //probando esto Susan
)

class AuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    private val usuarioRepo = UsuarioRepository()

    fun onUpdateName(v: String)    { _uiState.value = _uiState.value.copy(name = v,    error = null) }
    fun onEmailChange(v: String)   { _uiState.value = _uiState.value.copy(email = v,   error = null) }
    fun onPasswordChange(v: String){ _uiState.value = _uiState.value.copy(password = v,error = null) }
    fun chooseRole(role: UserRole)  { _uiState.value = _uiState.value.copy(role = role) }

    /** Limpia flags transitorios (user y error) para que el LaunchedEffect funcione */
    fun clearTransient() {
        _uiState.value = _uiState.value.copy(user = null, error = null)
    }

    /** Registro local */
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
            runCatching {
                usuarioRepo.crear(
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
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = email,    // flag de éxito
                    password = ""    // limpia campo
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "No se pudo registrar"
                )
            }
        }
    }

    /** Login tradicional */
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
//                    val userData = response.body()!!.data!!.usuario //cambios susan


                    val body = response.body()!!
                    val userData = body.data!!.usuario
                    val token = body.data!!.token  // 👉 aquí lo recibes


                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = userData.correo,
                        token = token //SUSANN
                    )
                    // 👇 Aquí conectas el token con Retrofit
                    RetrofitClient.setTokenProvider { _uiState.value.token }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = response.body()?.message ?: "Error al iniciar sesión"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error de red"
                )
            }
        }
    }

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
                        user = usuario.correo
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
}
