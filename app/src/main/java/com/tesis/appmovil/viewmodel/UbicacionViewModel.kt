package com.tesis.appmovil.viewmodel

// UbicacionViewModel.kt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.appmovil.models.Ubicacion
import com.tesis.appmovil.repository.UbicacionRepository
import com.tesis.appmovil.data.remote.dto.UbicacionCreate
import com.tesis.appmovil.data.remote.dto.UbicacionUpdate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UbicacionUiState(
    val isLoading: Boolean = false,
    val mutando: Boolean = false,
    val ubicaciones: List<Ubicacion> = emptyList(),
    val seleccionado: Ubicacion? = null,
    val error: String? = null
)

class UbicacionViewModel(
    private val repo: UbicacionRepository = UbicacionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UbicacionUiState())
    val uiState: StateFlow<UbicacionUiState> = _uiState

    /** Listar todas las ubicaciones */
    fun cargarUbicaciones() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { repo.listar() }
                .onSuccess { lista ->
                    _uiState.update { it.copy(isLoading = false, ubicaciones = lista) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error al cargar ubicaciones") }
                }
        }
    }

    /** Obtener detalle */
    fun obtenerUbicacion(id: Int) {
        viewModelScope.launch {
            runCatching { repo.obtener(id) }
                .onSuccess { ubicacion ->
                    _uiState.update { it.copy(seleccionado = ubicacion) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "No se pudo obtener la ubicación") }
                }
        }
    }

    fun limpiarError() = _uiState.update { it.copy(error = null) }
}