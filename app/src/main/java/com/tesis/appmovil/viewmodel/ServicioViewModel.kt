// app/src/main/java/com/tesis/appmovil/viewmodel/ServicioViewModel.kt
package com.tesis.appmovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.appmovil.models.Servicio
import com.tesis.appmovil.repository.ServicioRepository
import com.tesis.appmovil.data.remote.dto.ServicioCreate
import com.tesis.appmovil.data.remote.dto.ServicioUpdate
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

data class ServicioUiState(
    val isLoading: Boolean = false,          // carga de listas
    val mutando: Boolean = false,            // creando/actualizando/eliminando
    val servicios: List<Servicio> = emptyList(),
    val ofertas: List<Servicio> = emptyList(), // ← NUEVO: lista separada para ofertas
    val seleccionado: Servicio? = null,      // detalle actual
    val error: String? = null,               // error general
    val cargando: Boolean = false            // carga del DETALLE (lo usa EditServiceScreen)
)

class ServicioViewModel(
    private val repo: ServicioRepository = ServicioRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(ServicioUiState())
    val ui: StateFlow<ServicioUiState> = _ui.asStateFlow()

    /** Listado (opcional por negocio) */
    fun cargarServicios(idNegocio: Int? = null) = viewModelScope.launch {
        _ui.update { it.copy(isLoading = true, error = null) }

        // Cargar servicios normales y ofertas en paralelo
        val serviciosDeferred = viewModelScope.async { repo.listar(idNegocio) }
        val ofertasDeferred = viewModelScope.async { repo.obtenerOfertas() }

        try {
            val servicios = serviciosDeferred.await()
            val ofertas = ofertasDeferred.await()

            _ui.update {
                it.copy(
                    isLoading = false,
                    servicios = servicios,
                    ofertas = ofertas
                )
            }
        } catch (e: Exception) {
            _ui.update {
                it.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar datos"
                )
            }
        }
    }
//    fun cargarServicios(idNegocio: Int? = null) = viewModelScope.launch {
//        _ui.update { it.copy(isLoading = true, error = null) }
//        runCatching { repo.listar(idNegocio) }
//            .onSuccess { lista ->
//                _ui.update { it.copy(isLoading = false, servicios = lista) }
//            }
//            .onFailure { e ->
//                _ui.update { it.copy(isLoading = false, error = e.message ?: "Error al cargar servicios") }
//            }
//    }
    fun cargarOfertas() = viewModelScope.launch {
        _ui.update { it.copy(isLoading = true, error = null) }
        runCatching { repo.obtenerOfertas() }
            .onSuccess { ofertas ->
                _ui.update { it.copy(isLoading = false, ofertas = ofertas) }
            }
            .onFailure { e ->
                _ui.update { it.copy(isLoading = false, error = e.message ?: "Error al cargar ofertas") }
            }
    }

    /** Crear (con o sin imagen Multipart ya preparada) */

    // En ServicioViewModel.kt - ACTUALIZA esta función

    // En ServicioViewModel.kt - AGREGAR ESTA FUNCIÓN
    fun crearYSubirImagen(
        dto: ServicioCreate,
        imagenPart: MultipartBody.Part? = null,
        onSuccess: (Servicio) -> Unit = {}
    ) = viewModelScope.launch {
        _ui.update { it.copy(mutando = true, error = null) }
        runCatching {
            val servicioCreado = repo.crear(dto)

            // Si hay imagen, subirla después de crear el servicio
            if (imagenPart != null) {
                repo.subirImagenServicio(servicioCreado.idServicio, imagenPart)
            } else {
                servicioCreado
            }
        }
            .onSuccess { servicioFinal ->
                _ui.update { current ->
                    current.copy(
                        mutando = false,
                        servicios = replaceServicio(current.servicios, servicioFinal),
                        seleccionado = servicioFinal
                    )
                }
                onSuccess(servicioFinal)
            }
            .onFailure { e ->
                _ui.update { it.copy(mutando = false, error = e.message ?: "Error al crear servicio") }
            }
    }
    //version 2
//    fun crearYSubirImagen(
//        dto: ServicioCreate,
//        imagenPart: MultipartBody.Part? = null,
//        onSuccess: (Servicio) -> Unit = {}
//    ) = viewModelScope.launch {
//        _ui.update { it.copy(mutando = true, error = null) }
//        try {
//            // 1. Primero crear el servicio
//            val servicioCreado = repo.crear(dto)
//
//            // 2. Si hay imagen, subirla
//            val servicioFinal = if (imagenPart != null) {
//                repo.subirImagenServicio(servicioCreado.idServicio, imagenPart)
//            } else {
//                servicioCreado
//            }
//
//            // 3. Actualizar UI
//            _ui.update { current ->
//                current.copy(
//                    mutando = false,
//                    servicios = replaceServicio(current.servicios, servicioFinal),
//                    seleccionado = servicioFinal
//                )
//            }
//
//            // 4. Llamar el callback de éxito
//            onSuccess(servicioFinal)
//
//        } catch (e: Exception) {
//            _ui.update { it.copy(mutando = false, error = e.message ?: "Error al crear servicio") }
//        }
//    }




    //version original
//    fun crearYSubirImagen(dto: ServicioCreate, imagenPart: MultipartBody.Part? = null) = viewModelScope.launch {
//        _ui.update { it.copy(mutando = true, error = null) }
//        runCatching { repo.crearConImagen(dto, imagenPart) }
//            .onSuccess { creado ->
//                _ui.update { current ->
//                    current.copy(
//                        mutando = false,
//                        servicios = replaceServicio(current.servicios, creado),
//                        seleccionado = creado
//                    )
//                }
//            }
//            .onFailure { e ->
//                _ui.update { it.copy(mutando = false, error = e.message ?: "Error al crear servicio") }
//            }
//    }

    /** Obtener detalle → la pantalla Edit usa ui.cargando */
    fun obtenerServicio(id: Int) = viewModelScope.launch {
        _ui.update { it.copy(cargando = true, error = null) }
        runCatching { repo.obtener(id) }
            .onSuccess { s ->
                _ui.update { it.copy(cargando = false, seleccionado = s) }
            }
            .onFailure { e ->
                _ui.update { it.copy(cargando = false, error = e.message ?: "Error al cargar detalle") }
            }
    }

    /**
     * Actualizar (incluye imagenUrl=null para eliminar imagen)
     * Ahora admite callbacks para navegar/avisar en la UI justo al finalizar.
     */
    fun actualizarServicio(
        id: Int,
        body: ServicioUpdate,
        onSuccess: (Servicio) -> Unit = {},
        onError: (String) -> Unit = {}
    ) = viewModelScope.launch {
        _ui.update { it.copy(mutando = true, error = null) }
        runCatching { repo.actualizar(id, body) }
            .onSuccess { actualizado ->
                _ui.update { current ->
                    current.copy(
                        mutando = false,
                        servicios = replaceServicio(current.servicios, actualizado),
                        seleccionado = actualizado
                    )
                }
                onSuccess(actualizado) // dispara la navegación/feedback desde la UI
            }
            .onFailure { e ->
                val msg = e.message ?: "Error al actualizar"
                _ui.update { it.copy(mutando = false, error = msg) }
                onError(msg) // permite mostrar error en pantalla
            }
    }

    /** Eliminar servicio */
    fun eliminarServicio(id: Int) = viewModelScope.launch {
        _ui.update { it.copy(mutando = true, error = null) }
        runCatching { repo.eliminar(id) }
            .onSuccess {
                _ui.update { current ->
                    current.copy(
                        mutando = false,
                        servicios = current.servicios.filterNot { it.idServicio == id },
                        seleccionado = if (current.seleccionado?.idServicio == id) null else current.seleccionado
                    )
                }
            }
            .onFailure { e ->
                _ui.update { it.copy(mutando = false, error = e.message ?: "Error al eliminar") }
            }
    }

    /** Subir imagen (Multipart) y refrescar el detalle */
//    fun subirImagenServicio(idServicio: Int, imagenPart: MultipartBody.Part) = viewModelScope.launch {
//        _ui.update { it.copy(mutando = true, error = null) }
//        runCatching {
//            repo.subirImagen(idServicio, imagenPart)   // upload
//            repo.obtener(idServicio)                   // traer actualizado (con nueva imagenUrl)
//        }
//            .onSuccess { actualizado ->
//                _ui.update { current ->
//                    current.copy(
//                        mutando = false,
//                        servicios = replaceServicio(current.servicios, actualizado),
//                        seleccionado = actualizado
//                    )
//                }
//            }
//            .onFailure { e ->
//                _ui.update { it.copy(mutando = false, error = e.message ?: "Error al subir imagen") }
//            }
//    }

    fun limpiarError() {
        _ui.update { it.copy(error = null) }
    }
    // En ServicioViewModel.kt - AGREGAR ESTAS FUNCIONES

    fun subirImagenServicio(idServicio: Int, imagenPart: MultipartBody.Part) = viewModelScope.launch {
        _ui.update { it.copy(mutando = true, error = null) }
        runCatching {
            repo.subirImagenServicio(idServicio, imagenPart)
        }
            .onSuccess { servicioActualizado ->
                _ui.update { current ->
                    current.copy(
                        mutando = false,
                        servicios = replaceServicio(current.servicios, servicioActualizado),
                        seleccionado = servicioActualizado
                    )
                }
            }
            .onFailure { e ->
                _ui.update { it.copy(mutando = false, error = e.message ?: "Error al subir imagen") }
            }
    }

    fun eliminarImagenServicio(idServicio: Int) = viewModelScope.launch {
        _ui.update { it.copy(mutando = true, error = null) }
        runCatching {
            repo.eliminarImagenServicio(idServicio)
        }
            .onSuccess { servicioActualizado ->
                _ui.update { current ->
                    current.copy(
                        mutando = false,
                        servicios = replaceServicio(current.servicios, servicioActualizado),
                        seleccionado = servicioActualizado
                    )
                }
            }
            .onFailure { e ->
                _ui.update { it.copy(mutando = false, error = e.message ?: "Error al eliminar imagen") }
            }
    }

    // ---------- Helpers ----------

    /** Reemplaza el servicio con mismo idServicio; si no existe, inserta al inicio. */
    private fun replaceServicio(lista: List<Servicio>, nuevo: Servicio): List<Servicio> {
        val idx = lista.indexOfFirst { it.idServicio == nuevo.idServicio }
        return if (idx >= 0) lista.toMutableList().apply { this[idx] = nuevo } else listOf(nuevo) + lista
    }
}
