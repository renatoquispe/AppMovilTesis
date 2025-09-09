package com.tesis.appmovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.appmovil.models.Categoria
import com.tesis.appmovil.repository.CategoriaRepository
import com.tesis.appmovil.data.remote.dto.CategoriaCreate
import com.tesis.appmovil.data.remote.dto.CategoriaUpdate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoriaUiState(
    val isLoading: Boolean = false,
    val mutando: Boolean = false,
    val categorias: List<Categoria> = emptyList(),
    val seleccionado: Categoria? = null,
    val error: String? = null
)

class CategoriaViewModel(
    private val repo: CategoriaRepository = CategoriaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriaUiState())
    val uiState: StateFlow<CategoriaUiState> = _uiState

    /** Listar todas */
    fun cargarCategorias() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { repo.listar() }
                .onSuccess { lista ->
                    _uiState.update { it.copy(isLoading = false, categorias = lista) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error al cargar categorías") }
                }
        }
    }

    /** Obtener detalle */
    fun obtenerCategoria(id: Int) {
        viewModelScope.launch {
            runCatching { repo.obtener(id) }
                .onSuccess { cat ->
                    _uiState.update { it.copy(seleccionado = cat) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "No se pudo obtener la categoría") }
                }
        }
    }

    /** Crear */
    fun crearCategoria(body: CategoriaCreate) {
        viewModelScope.launch {
            _uiState.update { it.copy(mutando = true, error = null) }
            runCatching { repo.crear(body) }
                .onSuccess { creada ->
                    val lista = _uiState.value.categorias.toMutableList().apply { add(0, creada) }
                    _uiState.update { it.copy(mutando = false, categorias = lista, seleccionado = creada) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(mutando = false, error = e.message ?: "No se pudo crear la categoría") }
                }
        }
    }

    /** Actualizar */
    fun actualizarCategoria(id: Int, cambios: CategoriaUpdate) {
        viewModelScope.launch {
            _uiState.update { it.copy(mutando = true, error = null) }
            runCatching { repo.actualizar(id, cambios) }
                .onSuccess { actualizada ->
                    val lista = _uiState.value.categorias.map { if (it.id_categoria == id) actualizada else it }
                    _uiState.update { it.copy(mutando = false, categorias = lista, seleccionado = actualizada) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(mutando = false, error = e.message ?: "No se pudo actualizar la categoría") }
                }
        }
    }

    /** Eliminar */
    fun eliminarCategoria(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(mutando = true, error = null) }
            runCatching { repo.eliminar(id) }
                .onSuccess {
                    val lista = _uiState.value.categorias.filterNot { it.id_categoria == id }
                    _uiState.update { it.copy(mutando = false, categorias = lista, seleccionado = null) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(mutando = false, error = e.message ?: "No se pudo eliminar la categoría") }
                }
        }
    }

    fun limpiarError() = _uiState.update { it.copy(error = null) }
}
