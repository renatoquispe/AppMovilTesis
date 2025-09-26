package com.tesis.appmovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.appmovil.models.Negocio
import com.tesis.appmovil.repository.NegocioRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeNegocioState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val negocios: List<Negocio> = emptyList(),
    val query: String = ""
)

class HomeNegocioViewModel(
    private val repo: NegocioRepository = NegocioRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(HomeNegocioState())
    val state: StateFlow<HomeNegocioState> = _state.asStateFlow()

    // flujo de texto para debounce
    private val queryFlow = MutableStateFlow("")
    private var firstLoadDone = false

    // >>> NUEVO: cancelamos la petición anterior para que la última gane
    private var currentJob: Job? = null

    init {
        viewModelScope.launch {
            queryFlow
                .debounce(350)
                .distinctUntilChanged()
                .collect { q ->
                    buscarRemoto(q)
                }
        }
    }

    /** Primera carga / destacados */
    fun cargarDestacados(limit: Int = 10) {
        if (_state.value.isLoading) { /* opcional: puedes dejarlo */ }
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching { repo.listar(page = 1, pageSize = limit, activos = null) }
                .onSuccess { lista ->
                    firstLoadDone = true
                    _state.update { it.copy(isLoading = false, negocios = lista) }
                }
                .onFailure { e ->
                    if (e !is CancellationException) {
                        _state.update { it.copy(isLoading = false, error = e.message) }
                    }
                }
        }
    }

    /** El usuario escribe en la barra */
    fun onQueryChange(newValue: String) {
        _state.update { it.copy(query = newValue) }
        queryFlow.value = newValue
    }

    /** Fuerza búsqueda inmediata (enter o icono search) */
    fun buscarAhora() {
        buscarRemoto(_state.value.query)
    }

    private fun buscarRemoto(q: String) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            val query = q.trim()

            // >>> CAMBIO: si está vacío, SIEMPRE recargamos destacados
            if (query.isBlank()) {
                cargarDestacados(limit = 10)
                return@launch
            }

            _state.update { it.copy(isLoading = true, error = null) }
            runCatching {
                repo.listar(
                    q = query,
                    distrito = null,
                    ciudad = null,
                    activos = null,
                    page = 1,
                    pageSize = 20
                )
            }
                .onSuccess { lista ->
                    _state.update { it.copy(isLoading = false, negocios = lista) }
                }
                .onFailure { e ->
                    if (e !is CancellationException) {
                        _state.update { it.copy(isLoading = false, error = e.message) }
                    }
                }
        }
    }
}
