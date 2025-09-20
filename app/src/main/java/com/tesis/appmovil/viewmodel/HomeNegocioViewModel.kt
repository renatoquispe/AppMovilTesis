package com.tesis.appmovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.appmovil.models.Negocio
import com.tesis.appmovil.repository.NegocioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeNegocioState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val negocios: List<Negocio> = emptyList()
)

class HomeNegocioViewModel(
    private val repo: NegocioRepository = NegocioRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(HomeNegocioState())
    val state: StateFlow<HomeNegocioState> = _state

    /** Carga 1 negocio (el primero disponible) */
    fun cargarUno() {
        if (_state.value.isLoading) return
        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                // Antes: repo.listarDestacados(limit = 1)
                val destacados = repo.listar().take(1)

                _state.value = _state.value.copy(
                    isLoading = false,
                    negocios = destacados
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error desconocido"
                )
            }
        }
    }

    /** Si luego quieres N negocios destacados, usa este helper. */
    fun cargarDestacados(limit: Int = 1) {
        if (_state.value.isLoading) return
        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val lista = repo.listar().let { if (it.size > limit) it.take(limit) else it }
                _state.value = _state.value.copy(isLoading = false, negocios = lista)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error desconocido"
                )
            }
        }
    }
}
