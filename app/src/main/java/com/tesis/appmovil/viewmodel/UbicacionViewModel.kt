// UbicacionViewModel.kt
package com.tesis.appmovil.viewmodel

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

    private val _ui = MutableStateFlow(UbicacionUiState())
    val ui: StateFlow<UbicacionUiState> = _ui

    /** Listar todas */
    fun cargarUbicaciones() {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            runCatching { repo.listar() }
                .onSuccess { lista ->
                    _ui.update { it.copy(isLoading = false, ubicaciones = lista) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(isLoading = false, error = e.message ?: "Error al cargar ubicaciones") }
                }
        }
    }

    /** Obtener detalle */
    fun obtenerUbicacion(id: Int) {
        viewModelScope.launch {
            runCatching { repo.obtener(id) }
                .onSuccess { u ->
                    _ui.update { it.copy(seleccionado = u) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(error = e.message ?: "No se pudo obtener la ubicación") }
                }
        }
    }

    /** Crear */
    fun crearUbicacion(body: UbicacionCreate) {
        viewModelScope.launch {
            _ui.update { it.copy(mutando = true, error = null) }
            runCatching { repo.crear(body) }
                .onSuccess { creada ->
                    val lista = _ui.value.ubicaciones.toMutableList().apply { add(0, creada) }
                    _ui.update { it.copy(mutando = false, ubicaciones = lista, seleccionado = creada) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(mutando = false, error = e.message ?: "No se pudo crear la ubicación") }
                }
        }
    }

    /** Actualizar */
    fun actualizarUbicacion(id: Int, cambios: UbicacionUpdate) {
        viewModelScope.launch {
            _ui.update { it.copy(mutando = true, error = null) }
            runCatching { repo.actualizar(id, cambios) }
                .onSuccess { actualizada ->
                    val lista = _ui.value.ubicaciones.map { if (it.id_ubicacion == id) actualizada else it }
                    _ui.update { it.copy(mutando = false, ubicaciones = lista, seleccionado = actualizada) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(mutando = false, error = e.message ?: "No se pudo actualizar la ubicación") }
                }
        }
    }

    /** Eliminar */
    fun eliminarUbicacion(id: Int) {
        viewModelScope.launch {
            _ui.update { it.copy(mutando = true, error = null) }
            runCatching { repo.eliminar(id) }
                .onSuccess {
                    val lista = _ui.value.ubicaciones.filterNot { it.id_ubicacion == id }
                    _ui.update { it.copy(mutando = false, ubicaciones = lista, seleccionado = null) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(mutando = false, error = e.message ?: "No se pudo eliminar la ubicación") }
                }
        }
    }

    fun limpiarError() = _ui.update { it.copy(error = null) }
}
