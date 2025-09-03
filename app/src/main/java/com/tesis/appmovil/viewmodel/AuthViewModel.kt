package com.tesis.appmovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.appmovil.data.FakeAuthRepository
import com.tesis.appmovil.models.User
import com.tesis.appmovil.models.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val name: String? = null,
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: User? = null,
    val role: UserRole? = null
)

class AuthViewModel(
    private val repo: FakeAuthRepository = FakeAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun onUpdateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }
    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, error = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    fun login() {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.value = s.copy(isLoading = true, error = null)
            val result = repo.login(s.email.trim(), s.password)
            _uiState.value = result.fold(
                onSuccess = { user -> s.copy(isLoading = false, user = user) },
                onFailure = { e -> s.copy(isLoading = false, error = e.message ?: "Error") }
            )
        }
    }

    fun chooseRole(role: UserRole) {
        repo.setRole(role)
        _uiState.value = _uiState.value.copy(role = role)
    }
    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repo.register(name, email, password)
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(isLoading = false, user = result.getOrNull())
            } else {
                _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

}
