package com.tesis.appmovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.appmovil.models.UserRole
import com.tesis.appmovil.data.remote.request.LoginRequest
import com.tesis.appmovil.data.remote.RetrofitClient
import com.tesis.appmovil.data.remote.dto.UsuarioCreate
import com.tesis.appmovil.models.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: String? = null,
    val role: UserRole? = null
)

class AuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun onUpdateName(v: String) { _uiState.value = _uiState.value.copy(name = v) }
    fun onEmailChange(v: String) { _uiState.value = _uiState.value.copy(email = v) }
    fun onPasswordChange(v: String) { _uiState.value = _uiState.value.copy(password = v) }
    fun chooseRole(role: UserRole) { _uiState.value = _uiState.value.copy(role = role) }

    // --- REGISTER ---
    fun register(
        nombre: String,
        email: String,
        password: String,
        apellidoPaterno: String = "",    // valores por defecto
        apellidoMaterno: String = "",
        fechaNacimiento: String = "2000-01-01",
        fotoPerfil: String? = null
    ) {
        /* tu impl de register contra createUsuario() */
    }

    // --- LOGIN ---
    fun login() {
        // 🔥 Simulación de login exitoso (ignora backend)
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            user = "usuario_falso@ejemplo.com", // cualquier string
            error = null
        )
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
//                val response = RetrofitClient.api
//                    .login(LoginRequest(state.email, state.password))
//                if (response.isSuccessful && response.body()?.success == true) {
//                    val userData = response.body()!!.data!!.usuario
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        user = userData.correo
//                    )
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
}
