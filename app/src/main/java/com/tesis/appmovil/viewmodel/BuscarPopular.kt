package com.tesis.appmovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.appmovil.models.Negocio
import com.tesis.appmovil.repository.NegocioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PopularState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val negocios: List<Negocio> = emptyList()
)

class BuscarPopularViewModel(
    private val repo: NegocioRepository = NegocioRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(PopularState())
    val state: StateFlow<PopularState> = _state

    /** Trae 1 negocio para mostrarlo en la tarjeta */
    fun cargarUno() {
        if (_state.value.isLoading) return
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val lista = repo.listar()              // trae todos
                val uno = if (lista.isNotEmpty()) listOf(lista.first()) else emptyList()
                _state.value = PopularState(isLoading = false, negocios = uno)
            } catch (e: Exception) {
                _state.value = PopularState(isLoading = false, error = e.message)
            }
        }
    }
}
