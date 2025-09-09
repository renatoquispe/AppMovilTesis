package com.tesis.appmovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.appmovil.models.Mensaje
import com.tesis.appmovil.repository.MensajeRepository
import com.tesis.appmovil.data.remote.dto.MensajeCreate
import com.tesis.appmovil.data.remote.dto.MensajeUpdate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MensajeUiState(
    val isLoading: Boolean = false,
    val mutando: Boolean = false,
    val mensajes: List<Mensaje> = emptyList(),
    val seleccionado: Mensaje? = null,
    val error: String? = null
)

class MensajeViewModel(
    private val repo: MensajeRepository = MensajeRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(MensajeUiState())
    val ui: StateFlow<MensajeUiState> = _ui

    /** Listar (opcionalmente por usuario) */
    fun cargarMensajes(idUsuario: Int? = null) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            runCatching { repo.listar(idUsuario) }
                .onSuccess { lista -> _ui.update { it.copy(isLoading = false, mensajes = lista) } }
                .onFailure { e -> _ui.update { it.copy(isLoading = false, error = e.message ?: "Error al cargar mensajes") } }
        }
    }

    /** Detalle */
    fun obtenerMensaje(id: Int) {
        viewModelScope.launch {
            runCatching { repo.obtener(id) }
                .onSuccess { msg -> _ui.update { it.copy(seleccionado = msg) } }
                .onFailure { e -> _ui.update { it.copy(error = e.message ?: "No se pudo obtener el mensaje") } }
        }
    }

    /** Crear */
    fun crearMensaje(body: MensajeCreate) {
        viewModelScope.launch {
            _ui.update { it.copy(mutando = true, error = null) }
            runCatching { repo.crear(body) }
                .onSuccess { creado ->
                    val lista = _ui.value.mensajes.toMutableList().apply { add(0, creado) }
                    _ui.update { it.copy(mutando = false, mensajes = lista, seleccionado = creado) }
                }
                .onFailure { e -> _ui.update { it.copy(mutando = false, error = e.message ?: "No se pudo crear el mensaje") } }
        }
    }

    /** Actualizar */
    fun actualizarMensaje(id: Int, cambios: MensajeUpdate) {
        viewModelScope.launch {
            _ui.update { it.copy(mutando = true, error = null) }
            runCatching { repo.actualizar(id, cambios) }
                .onSuccess { actualizado ->
                    val lista = _ui.value.mensajes.map { if (it.id_mensaje == id) actualizado else it }
                    _ui.update { it.copy(mutando = false, mensajes = lista, seleccionado = actualizado) }
                }
                .onFailure { e -> _ui.update { it.copy(mutando = false, error = e.message ?: "No se pudo actualizar el mensaje") } }
        }
    }

    /** Eliminar */
    fun eliminarMensaje(id: Int) {
        viewModelScope.launch {
            _ui.update { it.copy(mutando = true, error = null) }
            runCatching { repo.eliminar(id) }
                .onSuccess {
                    val lista = _ui.value.mensajes.filterNot { it.id_mensaje == id }
                    _ui.update { it.copy(mutando = false, mensajes = lista, seleccionado = null) }
                }
                .onFailure { e -> _ui.update { it.copy(mutando = false, error = e.message ?: "No se pudo eliminar el mensaje") } }
        }
    }

    fun limpiarError() = _ui.update { it.copy(error = null) }
}
