package com.tesis.appmovil.viewmodel

import HorarioCreate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.appmovil.repository.HorarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HorarioUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

class HorarioViewModel(
    private val repo: HorarioRepository = HorarioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HorarioUiState())
    val uiState: StateFlow<HorarioUiState> = _uiState

    /** Crear múltiples horarios (método principal que necesitas) */
    suspend fun crearHorariosLote(horarios: List<HorarioCreate>): Result<Unit> {
        return try {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repo.crearHorariosLote(horarios)
            _uiState.update { it.copy(isLoading = false) }
            Result.success(Unit)
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Error al crear horarios"
            _uiState.update { it.copy(isLoading = false, error = errorMsg) }
            Result.failure(Exception(errorMsg))
        }
    }

    fun limpiarError() = _uiState.update { it.copy(error = null) }
}