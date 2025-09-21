// NegocioViewModel
package com.tesis.appmovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.appmovil.models.Negocio
import com.tesis.appmovil.repository.NegocioRepository
import com.tesis.appmovil.data.remote.dto.NegocioCreate
import com.tesis.appmovil.data.remote.dto.NegocioUpdate
import com.tesis.appmovil.data.remote.request.NegocioResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

//data class NegocioUiState(
//    val isLoading: Boolean = false,
//    val mutando: Boolean = false,
//    val negocios: List<Negocio> = emptyList(),
////    val seleccionado: Negocio? = null,
//    val seleccionado: NegocioResponse? = null, // 👈 cambio aquí
//    val error: String? = null
//)
data class NegocioUiState(
    val isLoading: Boolean = false,
    val mutando: Boolean = false,
    val negocios: List<Negocio> = emptyList(),
    val seleccionado: Negocio? = null,
    val detalle: NegocioResponse? = null,
    val error: String? = null
)


class NegocioViewModel(
    private val repo: NegocioRepository = NegocioRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(NegocioUiState())
    val ui: StateFlow<NegocioUiState> = _ui

    /** Listar (con filtros opcionales) */
    fun cargarNegocios(idCategoria: Int? = null, idUbicacion: Int? = null, q: String? = null) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            runCatching { repo.listar(idCategoria, idUbicacion, q) }
                .onSuccess { lista -> _ui.update { it.copy(isLoading = false, negocios = lista) } }
                .onFailure { e -> _ui.update { it.copy(isLoading = false, error = e.message ?: "Error al cargar negocios") } }
        }
    }

    /** Detalle */
//    fun obtenerNegocio(id: Int) {
//        viewModelScope.launch {
//            _ui.update { it.copy(isLoading = true, error = null) }
//            runCatching { repo.obtenerDetalle(id) } // 👈 este debe devolver NegocioResponse
//                .onSuccess { negocio ->
//                    _ui.update { it.copy(isLoading = false, seleccionado = negocio) }
//                }
//                .onFailure { e ->
//                    _ui.update { it.copy(isLoading = false, error = e.message ?: "No se pudo obtener el negocio") }
//                }
//        }
//    }

    // En NegocioViewModel
    fun obtenerNegocio(id: Int) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            runCatching { repo.obtenerDetalle(id) }
                .onSuccess { detalle ->
                    _ui.update { it.copy(isLoading = false, detalle = detalle) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

//    fun obtenerNegocio(id: Int) {
//        viewModelScope.launch {
//            runCatching { repo.obtener(id) }
//                .onSuccess { n -> _ui.update { it.copy(seleccionado = n) } }
//                .onFailure { e -> _ui.update { it.copy(error = e.message ?: "No se pudo obtener el negocio") } }
//        }
//    }

    /** Crear */
    fun crearNegocio(body: NegocioCreate) {
        viewModelScope.launch {
            _ui.update { it.copy(mutando = true, error = null) }
            runCatching { repo.crear(body) }
                .onSuccess { creado ->
                    val lista = _ui.value.negocios.toMutableList().apply { add(0, creado) }
                    _ui.update { it.copy(mutando = false, negocios = lista, seleccionado = creado) }
                }
                .onFailure { e -> _ui.update { it.copy(mutando = false, error = e.message ?: "No se pudo crear el negocio") } }
        }
    }

    /** Actualizar */
    fun actualizarNegocio(id: Int, cambios: NegocioUpdate) {
        viewModelScope.launch {
            _ui.update { it.copy(mutando = true, error = null) }
            runCatching { repo.actualizar(id, cambios) }
                .onSuccess { actualizado ->
                    val lista = _ui.value.negocios.map { if (it.id_negocio == id) actualizado else it }
                    _ui.update { it.copy(mutando = false, negocios = lista, seleccionado = actualizado) }
                }
                .onFailure { e -> _ui.update { it.copy(mutando = false, error = e.message ?: "No se pudo actualizar el negocio") } }
        }
    }

    /** Eliminar */
    fun eliminarNegocio(id: Int) {
        viewModelScope.launch {
            _ui.update { it.copy(mutando = true, error = null) }
            runCatching { repo.eliminar(id) }
                .onSuccess {
                    val lista = _ui.value.negocios.filterNot { it.id_negocio == id }
                    _ui.update { it.copy(mutando = false, negocios = lista, seleccionado = null) }
                }
                .onFailure { e -> _ui.update { it.copy(mutando = false, error = e.message ?: "No se pudo eliminar el negocio") } }
        }
    }

    fun limpiarError() = _ui.update { it.copy(error = null) }
}
