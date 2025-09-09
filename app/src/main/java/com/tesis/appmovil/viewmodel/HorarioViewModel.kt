package com.tesis.appmovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.appmovil.models.Horario
import com.tesis.appmovil.repository.HorarioRepository
import com.tesis.appmovil.data.remote.dto.HorarioCreate
import com.tesis.appmovil.data.remote.dto.HorarioUpdate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HorarioUiState(
    val isLoading: Boolean = false,
    val mutando: Boolean = false,
    val horarios: List<Horario> = emptyList(),
    val seleccionado: Horario? = null,
    val error: String? = null
)

class HorarioViewModel(
    private val repo: HorarioRepository = HorarioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HorarioUiState())
    val uiState: StateFlow<HorarioUiState> = _uiState

    /** Listar (opcionalmente por negocio) */
    fun cargarHorarios(idNegocio: Int? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { repo.listar(idNegocio) }
                .onSuccess { lista ->
                    _uiState.update { it.copy(isLoading = false, horarios = lista) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error al cargar horarios") }
                }
        }
    }

    /** Detalle */
    fun obtenerHorario(id: Int) {
        viewModelScope.launch {
            runCatching { repo.obtener(id) }
                .onSuccess { h -> _uiState.update { it.copy(seleccionado = h) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message ?: "No se pudo obtener el horario") } }
        }
    }

    /** Crear */
    fun crearHorario(body: HorarioCreate) {
        viewModelScope.launch {
            _uiState.update { it.copy(mutando = true, error = null) }
            runCatching { repo.crear(body) }
                .onSuccess { creado ->
                    val lista = _uiState.value.horarios.toMutableList().apply { add(0, creado) }
                    _uiState.update { it.copy(mutando = false, horarios = lista, seleccionado = creado) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(mutando = false, error = e.message ?: "No se pudo crear el horario") }
                }
        }
    }

    /** Actualizar */
    fun actualizarHorario(id: Int, cambios: HorarioUpdate) {
        viewModelScope.launch {
            _uiState.update { it.copy(mutando = true, error = null) }
            runCatching { repo.actualizar(id, cambios) }
                .onSuccess { actualizado ->
                    val lista = _uiState.value.horarios.map { if (it.id_horario == id) actualizado else it }
                    _uiState.update { it.copy(mutando = false, horarios = lista, seleccionado = actualizado) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(mutando = false, error = e.message ?: "No se pudo actualizar el horario") }
                }
        }
    }

    /** Eliminar */
    fun eliminarHorario(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(mutando = true, error = null) }
            runCatching { repo.eliminar(id) }
                .onSuccess {
                    val lista = _uiState.value.horarios.filterNot { it.id_horario == id }
                    _uiState.update { it.copy(mutando = false, horarios = lista, seleccionado = null) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(mutando = false, error = e.message ?: "No se pudo eliminar el horario") }
                }
        }
    }

    fun limpiarError() = _uiState.update { it.copy(error = null) }
}
