package com.tesis.appmovil.viewmodel

import HorarioCreate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.appmovil.repository.HorarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// âœ… NUEVO: modelo de UI para horarios
data class HorarioUi(
    val id: Int,
    val diaSemana: String,
    val horaApertura: String,
    val horaCierre: String,
    val habilitado: Boolean = true ,// nuevo campo
    val estaActivo: Boolean = habilitado,

    )
private fun Any?.toBool01(): Boolean = when (this) {
    is Boolean -> this
    is Number  -> this.toInt() == 1
    is String  -> this == "1" || this.equals("true", ignoreCase = true)
    else       -> false
}

// â¬‡ï¸Ž Agrego campos con defaults: no rompe usos existentes
data class HorarioUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val horarios: List<HorarioUi> = emptyList(), // âœ… NUEVO
    val isSaving: Boolean = false                // âœ… NUEVO
)

class HorarioViewModel(
    private val repo: HorarioRepository = HorarioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HorarioUiState())
    val uiState: StateFlow<HorarioUiState> = _uiState

    /** Crear mÃºltiples horarios (ya estaba) */
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

    /** âœ… NUEVO: listar horarios por negocio */
    fun obtenerHorarios(negocioId: Int) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        try {
            // repo ya devuelve List<HorarioUi>
            val items = repo.getHorariosByNegocio(negocioId)
            _uiState.update { it.copy(isLoading = false, horarios = items) }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error al cargar horarios") }
        }
    }

    /** âœ… NUEVO: actualizar un horario */
    fun actualizarHorario(horario: HorarioUi) = viewModelScope.launch {
        _uiState.update { it.copy(isSaving = true, error = null) }
        try {
            // Espera: repo.updateHorario(id, apertura, cierre) -> HorarioUi
            val updated = repo.updateHorario(
                id = horario.id,
                horaApertura = horario.horaApertura,
                horaCierre = horario.horaCierre
            )
            _uiState.update { st ->
                st.copy(
                    isSaving = false,
                    horarios = st.horarios.map { if (it.id == updated.id) updated else it }
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isSaving = false, error = e.message ?: "No se pudo actualizar el horario") }
        }
    }
    fun crearHorario(
        negocioId: Int,
        diaSemana: String,
        horaApertura: String,
        horaCierre: String
    ) = viewModelScope.launch {
        _uiState.update { it.copy(isSaving = true, error = null) }
        try {
            // repo.crearHorario(...) devuelve HorarioUi
            val nuevo = repo.crearHorario(
                negocioId = negocioId,
                diaSemana = diaSemana,
                horaApertura = horaApertura,
                horaCierre = horaCierre
            )
            _uiState.update { st ->
                st.copy(
                    isSaving = false,
                    horarios = st.horarios + nuevo
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(isSaving = false, error = e.message ?: "No se pudo crear el horario")
            }
        }
    }

    fun desactivarHorario(id: Int) = viewModelScope.launch {
        try {
            repo.desactivarHorario(id)
            _uiState.update { st ->
                st.copy(
                    horarios = st.horarios.map {
                        if (it.id == id) it.copy(habilitado = false) else it
                    }
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message ?: "No se pudo desactivar horario") }
        }
    }

    fun activarHorario(id: Int) = viewModelScope.launch {
        try {
            repo.activarHorario(id)
            _uiState.update { st ->
                st.copy(
                    horarios = st.horarios.map {
                        if (it.id == id) it.copy(habilitado = true) else it
                    }
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message ?: "No se pudo activar horario") }
        }
    }


    fun limpiarError() = _uiState.update { it.copy(error = null) }
}