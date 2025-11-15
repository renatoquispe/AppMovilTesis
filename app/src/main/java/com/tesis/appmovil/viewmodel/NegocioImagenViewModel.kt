// NegocioImagenViewModel.kt
package com.tesis.appmovil.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.appmovil.models.NegocioImagen
import com.tesis.appmovil.repository.NegocioImagenRepository
import com.tesis.appmovil.data.remote.dto.NegocioImagenCreate
import com.tesis.appmovil.data.remote.dto.NegocioImagenUpdate
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NegocioImagenUiState(
    val isLoading: Boolean = false,
    val mutando: Boolean = false,
    val imagenes: List<NegocioImagen> = emptyList(),
    val seleccionado: NegocioImagen? = null,
    val error: String? = null
)

class NegocioImagenViewModel(
    private val repo: NegocioImagenRepository = NegocioImagenRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(NegocioImagenUiState())
    val ui: StateFlow<NegocioImagenUiState> = _ui

//    fun subirImagenes(context: Context, negocioId: Int, uris: List<Uri>) {
//        viewModelScope.launch {
//            _ui.update { it.copy(mutando = true, error = null) }
//            runCatching {
//                uris.map { uri -> repo.subirImagen(context, negocioId, uri) }
//            }
//                .onSuccess { nuevas ->
//                    val lista = _ui.value.imagenes.toMutableList().apply { addAll(nuevas) }
//                    _ui.update { it.copy(mutando = false, imagenes = lista) }
//                }
//                .onFailure { e ->
//                    _ui.update { it.copy(mutando = false, error = e.message ?: "Error al subir imágenes") }
//                }
//        }
//    }
fun subirImagenes(context: Context, negocioId: Int, uris: List<Uri>): Job {
    return viewModelScope.launch {
        _ui.update { it.copy(mutando = true, error = null) }
        runCatching {
            uris.map { uri -> repo.subirImagen(context, negocioId, uri) }
        }
            .onSuccess { nuevas ->
                val lista = _ui.value.imagenes.toMutableList().apply { addAll(nuevas) }
                _ui.update { it.copy(mutando = false, imagenes = lista) }
            }
            .onFailure { e ->
                _ui.update {
                    it.copy(
                        mutando = false,
                        error = e.message ?: "Error al subir imágenes"
                    )
                }
            }
    }
}


//    fun reemplazarImagen(context: Context, idImagen: Int, uri: Uri, descripcion: String? = null) {
//        viewModelScope.launch {
//            _ui.update { it.copy(mutando = true, error = null) }
//            runCatching {
//                repo.reemplazarImagen(context, idImagen, uri, descripcion)
//            }.onSuccess { actualizada ->
//                val lista = _ui.value.imagenes.map {
//                    if (it.id_imagen == idImagen) actualizada else it
//                }
//                _ui.update { it.copy(mutando = false, imagenes = lista) }
//            }.onFailure { e ->
//                _ui.update { it.copy(mutando = false, error = e.message ?: "Error al reemplazar imagen") }
//            }
//        }
//    }

    fun reemplazarImagen(
        context: Context,
        idImagen: Int,
        uri: Uri,
        descripcion: String? = null
    ):Job {
        return viewModelScope.launch {
            _ui.update { it.copy(mutando = true, error = null) }
            runCatching {
                repo.reemplazarImagen(context, idImagen, uri, descripcion)
            }.onSuccess { actualizada ->
                // Actualizar la lista localmente en lugar de recargar inmediatamente
                val lista = _ui.value.imagenes.map {
                    if (it.id_imagen == idImagen) actualizada else it
                }
                _ui.update {
                    it.copy(
                        mutando = false,
                        imagenes = lista,
                        error = null
                    )
                }
            }.onFailure { e ->
                _ui.update {
                    it.copy(
                        mutando = false,
                        error = e.message ?: "Error al reemplazar imagen"
                    )
                }
            }
        }
    }


    /** Listar (opcionalmente por id_negocio) */
    fun cargarImagenes(idNegocio: Int) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            runCatching { repo.listar(idNegocio) }
                .onSuccess { lista ->
                    _ui.update { it.copy(isLoading = false, imagenes = lista) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(isLoading = false, error = e.message ?: "Error al cargar imágenes") }
                }
        }
    }

    /** Obtener detalle */
    fun obtenerImagen(id: Int) {
        viewModelScope.launch {
            runCatching { repo.obtener(id) }
                .onSuccess { img ->
                    _ui.update { it.copy(seleccionado = img) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(error = e.message ?: "No se pudo obtener la imagen") }
                }
        }
    }

    /** Crear */
    fun crearImagen(body: NegocioImagenCreate) {
        viewModelScope.launch {
            _ui.update { it.copy(mutando = true, error = null) }
            runCatching { repo.crear(body) }
                .onSuccess { creada ->
                    val lista = _ui.value.imagenes.toMutableList().apply { add(0, creada) }
                    _ui.update { it.copy(mutando = false, imagenes = lista, seleccionado = creada) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(mutando = false, error = e.message ?: "No se pudo crear la imagen") }
                }
        }
    }

    /** Actualizar */
    fun actualizarImagen(id: Int, cambios: NegocioImagenUpdate) {
        viewModelScope.launch {
            _ui.update { it.copy(mutando = true, error = null) }
            runCatching { repo.actualizar(id, cambios) }
                .onSuccess { actualizada ->
                    val lista = _ui.value.imagenes.map {
                        if (it.id_imagen == id) actualizada else it
                    }
                    _ui.update { it.copy(mutando = false, imagenes = lista, seleccionado = actualizada) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(mutando = false, error = e.message ?: "No se pudo actualizar la imagen") }
                }
        }
    }

    /** Eliminar */
    fun eliminarImagen(id: Int) {
        viewModelScope.launch {
            _ui.update { it.copy(mutando = true, error = null) }
            runCatching { repo.eliminar(id) }
                .onSuccess {
                    val lista = _ui.value.imagenes.filterNot { it.id_imagen == id }
                    _ui.update { it.copy(mutando = false, imagenes = lista, seleccionado = null) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(mutando = false, error = e.message ?: "No se pudo eliminar la imagen") }
                }
        }
    }

    fun limpiarError() = _ui.update { it.copy(error = null) }
}
