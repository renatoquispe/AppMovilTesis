// ServicioViewModel.kt
package com.tesis.appmovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.appmovil.models.Servicio
import com.tesis.appmovil.repository.ServicioRepository
import com.tesis.appmovil.data.remote.dto.ServicioCreate
import com.tesis.appmovil.data.remote.dto.ServicioUpdate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ServicioUiState(
    val isLoading: Boolean = false,
    val mutando: Boolean = false,
    val servicios: List<Servicio> = emptyList(),
    val seleccionado: Servicio? = null,
    val error: String? = null
)

class ServicioViewModel(
    private val repo: ServicioRepository = ServicioRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(ServicioUiState())
    val ui: StateFlow<ServicioUiState> = _ui

    /** Listar (opcionalmente por id_negocio) */
    fun cargarServicios(idNegocio: Int? = null) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            runCatching { repo.listar(idNegocio) }
                .onSuccess { lista ->
                    _ui.update { it.copy(isLoading = false, servicios = lista) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(isLoading = false, error = e.message ?: "Error al cargar servicios") }
                }
        }
    }

    /** Obtener detalle */
    fun obtenerServicio(id: Int) {
        viewModelScope.launch {
            runCatching { repo.obtener(id) }
                .onSuccess { s ->
                    _ui.update { it.copy(seleccionado = s) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(error = e.message ?: "No se pudo obtener el servicio") }
                }
        }
    }

    /** Crear */
    fun crearServicio(body: ServicioCreate) {
        viewModelScope.launch {
            _ui.update { it.copy(mutando = true, error = null) }
            runCatching { repo.crear(body) }
                .onSuccess { creado ->
                    val lista = _ui.value.servicios.toMutableList().apply { add(0, creado) }
                    _ui.update { it.copy(mutando = false, servicios = lista, seleccionado = creado) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(mutando = false, error = e.message ?: "No se pudo crear el servicio") }
                }
        }
    }

    /** Actualizar */
    fun actualizarServicio(id: Int, cambios: ServicioUpdate) {
        viewModelScope.launch {
            _ui.update { it.copy(mutando = true, error = null) }
            runCatching { repo.actualizar(id, cambios) }
                .onSuccess { actualizado ->
                    val lista = _ui.value.servicios.map { if (it.idServicio == id) actualizado else it }
                    _ui.update { it.copy(mutando = false, servicios = lista, seleccionado = actualizado) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(mutando = false, error = e.message ?: "No se pudo actualizar el servicio") }
                }
        }
    }

    /** Eliminar */
    fun eliminarServicio(id: Int) {
        viewModelScope.launch {
            _ui.update { it.copy(mutando = true, error = null) }
            runCatching { repo.eliminar(id) }
                .onSuccess {
                    val lista = _ui.value.servicios.filterNot { it.idServicio == id }
                    _ui.update { it.copy(mutando = false, servicios = lista, seleccionado = null) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(mutando = false, error = e.message ?: "No se pudo eliminar el servicio") }
                }
        }
    }

    fun limpiarError() = _ui.update { it.copy(error = null) }
}
