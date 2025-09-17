// ServicioViewModel.kt
package com.tesis.appmovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.appmovil.models.Servicio
import com.tesis.appmovil.repository.ServicioRepository
import com.tesis.appmovil.data.remote.dto.ServicioCreate
import com.tesis.appmovil.data.remote.dto.ServicioUpdate
import com.tesis.appmovil.data.remote.request.NegocioResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

//data class ServicioUiState(
//    val isLoading: Boolean = false,
//    val mutando: Boolean = false,
//    val servicios: List<Servicio> = emptyList(),
//    val seleccionado: Servicio? = null,
//    val error: String? = null,
//    val negociosDestacados: List<Servicio> = emptyList(),
//
//    // Añade estos estados para el detalle
//    // Estados para el detalle
//    val isLoadingDetalle: Boolean = false,
////    val negocioDetalle: NegocioResponse? = null,
////    val isLoadingDetalle: Boolean = false,
//    val errorDetalle: String? = null
//)
data class ServicioUiState(
    val isLoading: Boolean = false,
    val mutando: Boolean = false,
    val servicios: List<Servicio> = emptyList(),
    val seleccionado: Servicio? = null,
    val error: String? = null,
    val negociosDestacados: List<Servicio> = emptyList(),
    // SOLO estos estados para el detalle
    val isLoadingDetalle: Boolean = false,
    val errorDetalle: String? = null
)

class ServicioViewModel(
    private val repo: ServicioRepository = ServicioRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(ServicioUiState())
    val ui: StateFlow<ServicioUiState> = _ui

    fun obtenerNegocioDesdeServicios(idServicio: Int): NegocioResponse? {
        return _ui.value.servicios.find { it.idServicio == idServicio }?.negocio
    }

    fun obtenerServicio(id: Int) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoadingDetalle = true, errorDetalle = null) }

            runCatching { repo.obtener(id) }
                .onSuccess { servicio ->
                    _ui.update {
                        it.copy(
                            isLoadingDetalle = false,
                            seleccionado = servicio,
                            errorDetalle = null
                        )
                    }
                }
                .onFailure { e ->
                    _ui.update {
                        it.copy(
                            isLoadingDetalle = false,
                            errorDetalle = e.message ?: "No se pudo obtener el servicio"
                        )
                    }
                }
        }
    }

    fun obtenerServiciosDeNegocio(idNegocio: Int): List<Servicio> {
        return _ui.value.servicios.filter { it.negocio.idNegocio == idNegocio }
    }

    fun cargarDetalleNegocio(idNegocio: Int) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoadingDetalle = true, errorDetalle = null) }
            try {
                // Buscar el negocio en los servicios ya cargados
                val negocio = _ui.value.servicios
                    .firstOrNull { it.negocio.idNegocio == idNegocio }
                    ?.negocio

                _ui.update {
                    it.copy(
                        isLoadingDetalle = false,
                        errorDetalle = if (negocio == null) "Negocio no encontrado" else null
                        // No necesitamos almacenar negocioDetalle por separado
                    )
                }
            } catch (e: Exception) {
                _ui.update {
                    it.copy(
                        isLoadingDetalle = false,
                        errorDetalle = e.message ?: "Error al cargar detalle"
                    )
                }
            }
        }
    }

    fun obtenerServicioPorId(id: Int): Servicio? {
        return ui.value.servicios.find { it.idServicio == id }
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
    /** Listar servicios */
    fun cargarServicios(idNegocio: Int? = null) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            runCatching { repo.listar(idNegocio) }
                .onSuccess { lista ->
                    val negociosUnicos = lista
                        .distinctBy { it.negocio.idNegocio }
                        .take(3)
                    _ui.update {
                        it.copy(
                            isLoading = false,
                            servicios = lista,
                            negociosDestacados = negociosUnicos
                        )
                    }
                }
                .onFailure { e ->
                    _ui.update { it.copy(isLoading = false, error = e.message ?: "Error al cargar servicios") }
                }
        }
    }

}
