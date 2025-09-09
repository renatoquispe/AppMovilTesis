// ResenaViewModel.kt
package com.tesis.appmovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.appmovil.models.Resena
import com.tesis.appmovil.repository.ResenaRepository
import com.tesis.appmovil.data.remote.dto.ResenaCreate
import com.tesis.appmovil.data.remote.dto.ResenaUpdate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResenaUiState(
    val isLoading: Boolean = false,
    val mutando: Boolean = false,
    val reseñas: List<Resena> = emptyList(),
    val seleccionado: Resena? = null,
    val error: String? = null
)

class ResenaViewModel(
    private val repo: ResenaRepository = ResenaRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(ResenaUiState())
    val ui: StateFlow<ResenaUiState> = _ui

    /** Listar (filtros opcionales) */
    fun cargarResenas(idNegocio: Int? = null, idUsuario: Int? = null) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            runCatching { repo.listar(idNegocio, idUsuario) }
                .onSuccess { lista ->
                    _ui.update { it.copy(isLoading = false, reseñas = lista) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(isLoading = false, error = e.message ?: "Error al cargar reseñas") }
                }
        }
    }

    /** Obtener detalle */
    fun obtenerResena(id: Int) {
        viewModelScope.launch {
            runCatching { repo.obtener(id) }
                .onSuccess { r -> _ui.update { it.copy(seleccionado = r) } }
                .onFailure { e -> _ui.update { it.copy(error = e.message ?: "No se pudo obtener la reseña") } }
        }
    }

    /** Crear */
    fun crearResena(body: ResenaCreate) {
        viewModelScope.launch {
            _ui.update { it.copy(mutando = true, error = null) }
            runCatching { repo.crear(body) }
                .onSuccess { creada ->
                    val lista = _ui.value.reseñas.toMutableList().apply { add(0, creada) }
                    _ui.update { it.copy(mutando = false, reseñas = lista, seleccionado = creada) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(mutando = false, error = e.message ?: "No se pudo crear la reseña") }
                }
        }
    }

    /** Actualizar */
    fun actualizarResena(id: Int, cambios: ResenaUpdate) {
        viewModelScope.launch {
            _ui.update { it.copy(mutando = true, error = null) }
            runCatching { repo.actualizar(id, cambios) }
                .onSuccess { actualizada ->
                    val lista = _ui.value.reseñas.map { if (it.id_resena == id) actualizada else it }
                    _ui.update { it.copy(mutando = false, reseñas = lista, seleccionado = actualizada) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(mutando = false, error = e.message ?: "No se pudo actualizar la reseña") }
                }
        }
    }

    /** Eliminar */
    fun eliminarResena(id: Int) {
        viewModelScope.launch {
            _ui.update { it.copy(mutando = true, error = null) }
            runCatching { repo.eliminar(id) }
                .onSuccess {
                    val lista = _ui.value.reseñas.filterNot { it.id_resena == id }
                    _ui.update { it.copy(mutando = false, reseñas = lista, seleccionado = null) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(mutando = false, error = e.message ?: "No se pudo eliminar la reseña") }
                }
        }
    }

    fun limpiarError() = _ui.update { it.copy(error = null) }
}
