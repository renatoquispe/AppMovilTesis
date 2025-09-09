package com.tesis.appmovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.appmovil.models.Usuario
import com.tesis.appmovil.repository.UsuarioRepository
import com.tesis.appmovil.data.remote.dto.UsuarioCreate
import com.tesis.appmovil.data.remote.dto.UsuarioUpdate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UsuarioUiState(
    val isLoading: Boolean = false,
    val mutando: Boolean = false,
    val usuarios: List<Usuario> = emptyList(),
    val seleccionado: Usuario? = null,
    val error: String? = null
)

class UsuarioViewModel(
    private val repo: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsuarioUiState())
    val uiState: StateFlow<UsuarioUiState> = _uiState

    fun cargarUsuarios() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { repo.listar() }
                .onSuccess { lista ->
                    _uiState.update { it.copy(isLoading = false, usuarios = lista) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error al cargar") }
                }
        }
    }

    fun obtenerUsuario(id: Int) {
        viewModelScope.launch {
            runCatching { repo.obtener(id) }
                .onSuccess { user -> _uiState.update { it.copy(seleccionado = user) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message ?: "No se pudo obtener") } }
        }
    }

    fun crearUsuario(body: UsuarioCreate) {
        viewModelScope.launch {
            _uiState.update { it.copy(mutando = true, error = null) }
            runCatching { repo.crear(body) }
                .onSuccess { nuevo ->
                    val lista = _uiState.value.usuarios.toMutableList().apply { add(0, nuevo) }
                    _uiState.update { it.copy(mutando = false, usuarios = lista, seleccionado = nuevo) }
                }
                .onFailure { e -> _uiState.update { it.copy(mutando = false, error = e.message ?: "No se pudo crear") } }
        }
    }

    fun actualizarUsuario(id: Int, cambios: UsuarioUpdate) {
        viewModelScope.launch {
            _uiState.update { it.copy(mutando = true, error = null) }
            runCatching { repo.actualizar(id, cambios) }
                .onSuccess { actualizado ->
                    val lista = _uiState.value.usuarios.map { if (it.id_usuario == id) actualizado else it }
                    _uiState.update { it.copy(mutando = false, usuarios = lista, seleccionado = actualizado) }
                }
                .onFailure { e -> _uiState.update { it.copy(mutando = false, error = e.message ?: "No se pudo actualizar") } }
        }
    }

    fun eliminarUsuario(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(mutando = true, error = null) }
            runCatching { repo.eliminar(id) }
                .onSuccess {
                    val lista = _uiState.value.usuarios.filterNot { it.id_usuario == id }
                    _uiState.update { it.copy(mutando = false, usuarios = lista, seleccionado = null) }
                }
                .onFailure { e -> _uiState.update { it.copy(mutando = false, error = e.message ?: "No se pudo eliminar") } }
        }
    }

    fun limpiarError() = _uiState.update { it.copy(error = null) }
}
