//// NegocioViewModel V1
//package com.tesis.appmovil.viewmodel
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.tesis.appmovil.models.Negocio
//import com.tesis.appmovil.repository.NegocioRepository
//import com.tesis.appmovil.data.remote.dto.NegocioCreate
//import com.tesis.appmovil.data.remote.dto.NegocioUpdate
//import com.tesis.appmovil.data.remote.request.NegocioResponse
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//
////data class NegocioUiState(
////    val isLoading: Boolean = false,
////    val mutando: Boolean = false,
////    val negocios: List<Negocio> = emptyList(),
//////    val seleccionado: Negocio? = null,
////    val seleccionado: NegocioResponse? = null, // 👈 cambio aquí
////    val error: String? = null
////)
//data class NegocioUiState(
//    val isLoading: Boolean = false,
//    val mutando: Boolean = false,
//    val negocios: List<Negocio> = emptyList(),
//    val seleccionado: Negocio? = null,
//    val detalle: NegocioResponse? = null,
//    val error: String? = null
//)
//
//
//class NegocioViewModel(
//    private val repo: NegocioRepository = NegocioRepository()
//) : ViewModel() {
//
//    private val _ui = MutableStateFlow(NegocioUiState())
//    val ui: StateFlow<NegocioUiState> = _ui
//
//    /** Listar (con filtros opcionales) */
//    fun cargarNegocios(idCategoria: Int? = null, idUbicacion: Int? = null, q: String? = null) {
//        viewModelScope.launch {
//            _ui.update { it.copy(isLoading = true, error = null) }
//            runCatching { repo.listar(idCategoria, idUbicacion, q) }
//                .onSuccess { lista -> _ui.update { it.copy(isLoading = false, negocios = lista) } }
//                .onFailure { e -> _ui.update { it.copy(isLoading = false, error = e.message ?: "Error al cargar negocios") } }
//        }
//    }
//
//    /** Detalle */
////    fun obtenerNegocio(id: Int) {
////        viewModelScope.launch {
////            _ui.update { it.copy(isLoading = true, error = null) }
////            runCatching { repo.obtenerDetalle(id) } // 👈 este debe devolver NegocioResponse
////                .onSuccess { negocio ->
////                    _ui.update { it.copy(isLoading = false, seleccionado = negocio) }
////                }
////                .onFailure { e ->
////                    _ui.update { it.copy(isLoading = false, error = e.message ?: "No se pudo obtener el negocio") }
////                }
////        }
////    }
//
//    // En NegocioViewModel
//    fun obtenerNegocio(id: Int) {
//        viewModelScope.launch {
//            _ui.update { it.copy(isLoading = true, error = null) }
//            runCatching { repo.obtenerDetalle(id) }
//                .onSuccess { detalle ->
//                    _ui.update { it.copy(isLoading = false, detalle = detalle) }
//                }
//                .onFailure { e ->
//                    _ui.update { it.copy(isLoading = false, error = e.message) }
//                }
//        }
//    }
//
////    fun obtenerNegocio(id: Int) {
////        viewModelScope.launch {
////            runCatching { repo.obtener(id) }
////                .onSuccess { n -> _ui.update { it.copy(seleccionado = n) } }
////                .onFailure { e -> _ui.update { it.copy(error = e.message ?: "No se pudo obtener el negocio") } }
////        }
////    }
//
//    /** Crear */
//    fun crearNegocio(body: NegocioCreate) {
//        viewModelScope.launch {
//            _ui.update { it.copy(mutando = true, error = null) }
//            runCatching { repo.crear(body) }
//                .onSuccess { creado ->
//                    val lista = _ui.value.negocios.toMutableList().apply { add(0, creado) }
//                    _ui.update { it.copy(mutando = false, negocios = lista, seleccionado = creado) }
//                }
//                .onFailure { e -> _ui.update { it.copy(mutando = false, error = e.message ?: "No se pudo crear el negocio") } }
//        }
//    }
//
//    /** Actualizar */
//    fun actualizarNegocio(id: Int, cambios: NegocioUpdate) {
//        viewModelScope.launch {
//            _ui.update { it.copy(mutando = true, error = null) }
//            runCatching { repo.actualizar(id, cambios) }
//                .onSuccess { actualizado ->
//                    val lista = _ui.value.negocios.map { if (it.id_negocio == id) actualizado else it }
//                    _ui.update { it.copy(mutando = false, negocios = lista, seleccionado = actualizado) }
//                }
//                .onFailure { e -> _ui.update { it.copy(mutando = false, error = e.message ?: "No se pudo actualizar el negocio") } }
//        }
//    }
//
//    /** Eliminar */
//    fun eliminarNegocio(id: Int) {
//        viewModelScope.launch {
//            _ui.update { it.copy(mutando = true, error = null) }
//            runCatching { repo.eliminar(id) }
//                .onSuccess {
//                    val lista = _ui.value.negocios.filterNot { it.id_negocio == id }
//                    _ui.update { it.copy(mutando = false, negocios = lista, seleccionado = null) }
//                }
//                .onFailure { e -> _ui.update { it.copy(mutando = false, error = e.message ?: "No se pudo eliminar el negocio") } }
//        }
//    }
//
//    fun limpiarError() = _ui.update { it.copy(error = null) }
//}

// NegocioViewModel.kt - EXTENDIDO para el flujo de 7 pantallas



// NegocioViewModel.kt - VERSIÓN COMPLETA CON GUARDADO INCREMENTAL
package com.tesis.appmovil.viewmodel

import HorarioCreate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.appmovil.data.remote.ApiResponse
import com.tesis.appmovil.data.remote.RetrofitClient.api
import com.tesis.appmovil.models.Negocio
import com.tesis.appmovil.repository.NegocioRepository
import com.tesis.appmovil.data.remote.dto.NegocioCreate
import com.tesis.appmovil.data.remote.dto.NegocioUpdate
import com.tesis.appmovil.data.remote.request.NegocioResponse
import com.tesis.appmovil.repository.HorarioRepository
import com.tesis.appmovil.ui.business.DaySchedule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Modelos temporales para horarios y servicios (si los necesitas después)
//data class HorarioTemporal(
//    val diaSemana: String,
//    val horaApertura: String,
//    val horaCierre: String
//)
// Modifica tu data class HorarioTemporal para que coincida con DaySchedule
data class HorarioTemporal(
    val diaSemana: String,
    val isOpen: Boolean = false,  // ← Agrega esto
    val horaApertura: String = "09:00",  // ← Valor por defecto
    val horaCierre: String = "18:00"     // ← Valor por defecto
)


data class ServicioTemporal(
    val nombre: String,
    val precio: Double,
    val duracionMinutos: Int
)

// Estado del UI
data class NegocioUiState(
    // Estado original para otras funcionalidades
    val isLoading: Boolean = false,
    val mutando: Boolean = false,
    val negocios: List<Negocio> = emptyList(),
    val seleccionado: Negocio? = null,
    val detalle: NegocioResponse? = null,
    val error: String? = null,
    val negocio:   Negocio? = null,

    // Estado para el registro en progreso
    val negocioCreadoId: Int? = null,
    val registroCompletado: Boolean = false
)

class NegocioViewModel(
    private val repo: NegocioRepository = NegocioRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(NegocioUiState())
//    val ui: StateFlow<NegocioUiState> = _ui
    val ui: StateFlow<NegocioUiState> = _ui.asStateFlow()


    // ========== MÉTODOS EXISTENTES (para otras partes de la app) ==========

    /** Listar negocios con filtros opcionales */
    fun cargarNegocios(idCategoria: Int? = null, idUbicacion: Int? = null, q: String? = null) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            runCatching { repo.listar(idCategoria, idUbicacion, q) }
                .onSuccess { lista ->
                    _ui.update { it.copy(isLoading = false, negocios = lista) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(isLoading = false, error = e.message ?: "Error al cargar negocios") }
                }
        }
    }

    /** Obtener detalle de un negocio */
    fun obtenerNegocio(id: Int) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            runCatching { repo.obtenerDetalle(id) }
                .onSuccess { detalle ->
                    _ui.update { it.copy(isLoading = false, detalle = detalle) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(isLoading = false, error = e.message ?: "Error al obtener negocio") }
                }
        }
    }

    /** Eliminar negocio */
    fun eliminarNegocio(id: Int) {
        viewModelScope.launch {
            _ui.update { it.copy(mutando = true, error = null) }
            runCatching { repo.eliminar(id) }
                .onSuccess {
                    val lista = _ui.value.negocios.filterNot { it.id_negocio == id }
                    _ui.update { it.copy(mutando = false, negocios = lista, seleccionado = null) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(mutando = false, error = e.message ?: "No se pudo eliminar el negocio") }
                }
        }
    }


    // ========== MÉTODOS NUEVOS PARA REGISTRO INCREMENTAL ==========

    /**
     * PANTALLA 1: Crear negocio básico (estado_auditoria = 0 por defecto en el backend)
     * Retorna el ID del negocio creado para usar en las siguientes pantallas
     */
    suspend fun crearNegocioBasico(
        nombre: String,
        idCategoria: Int,
        idUbicacion: Int,
        direccion: String,
        idUsuario: Int
    ): Result<Int> {
        return try {
            _ui.update { it.copy(mutando = true, error = null) }

            val negocioCreate = NegocioCreate(
                id_categoria = idCategoria,
                id_ubicacion = idUbicacion,
                nombre = nombre,
                direccion = direccion,
                id_usuario = idUsuario
            )
            println("🔄 Enviando datos al backend: $negocioCreate")

            val negocioCreado = repo.crear(negocioCreate)

            val idNegocio = negocioCreado.id_negocio

            println("✅ Respuesta del backend - ID: $idNegocio")
            println("✅ Negocio completo: $negocioCreado")

            // 🔥 CAMBIO CRÍTICO: Asignación directa del estado

            _ui.update {
                it.copy(
                    mutando = false,
                    seleccionado = negocioCreado,
                    negocioCreadoId = idNegocio,
                    error = null
                )
            }
            println("✅ ID guardado en ViewModel: ${_ui.value.negocioCreadoId}")

            Result.success(idNegocio)
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Error al crear el negocio"
            _ui.update { it.copy(mutando = false, error = errorMsg) }
            Result.failure(Exception(errorMsg))
        }
    }

    /**
     * PANTALLA 2: Actualizar información de contacto
     */
    suspend fun actualizarContacto(
        idNegocio: Int,
        telefono: String,
        correo: String,
        descripcion: String
    ): Result<Unit> {
        return try {
            _ui.update { it.copy(mutando = true, error = null) }

            val update = NegocioUpdate(
                telefono = telefono.ifBlank { null },
                correoContacto = correo.ifBlank { null },
                descripcion = descripcion.ifBlank { null }
            )

//            val negocioActualizado = repo.actualizar(idNegocio, update)
            repo.actualizar(idNegocio, update)
            _ui.update { currentState ->
                val negocioActualizado = currentState.seleccionado?.copy(
                    telefono = telefono.ifBlank { null },
                    correo_contacto = correo.ifBlank { null },
                    descripcion = descripcion.ifBlank { null }
                )

                currentState.copy(
                    mutando = false,
                    seleccionado = negocioActualizado,
                    error = null
                )
            }

//            _ui.update {
//                it.copy(
//                    mutando = false,
//                    seleccionado = negocioActualizado,
//                    error = null
//                )
//            }
            Result.success(Unit)
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Error al actualizar contacto"
            _ui.update { it.copy(mutando = false, error = errorMsg) }
            Result.failure(Exception(errorMsg))
        }
    }

    /**
     * PANTALLA 6: Actualizar ubicación exacta (latitud/longitud)
     */
    suspend fun actualizarUbicacionExacta(
        idNegocio: Int,
        latitud: Double,
        longitud: Double
    ): Result<Unit> {
        return try {
            _ui.update { it.copy(mutando = true, error = null) }

            val update = NegocioUpdate(
                latitud = latitud,
                longitud = longitud
            )

            val negocioActualizado = repo.actualizar(idNegocio, update)

            _ui.update {
                it.copy(
                    mutando = false,
                    seleccionado = negocioActualizado,
                    error = null
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Error al actualizar ubicación"
            _ui.update { it.copy(mutando = false, error = errorMsg) }
            Result.failure(Exception(errorMsg))
        }
    }

    /**
     * PANTALLA 7: Completar registro (cambiar estado_auditoria = 1)
     */
    suspend fun completarRegistro(idNegocio: Int): Result<Unit> {
        return try {
            _ui.update { it.copy(mutando = true, error = null) }

            val update = NegocioUpdate(
                estado_auditoria = true // Cambiar a activo
            )

            val negocioActualizado = repo.actualizar(idNegocio, update)

            _ui.update {
                it.copy(
                    mutando = false,
                    seleccionado = negocioActualizado,
                    registroCompletado = true,
                    error = null
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Error al completar registro"
            _ui.update { it.copy(mutando = false, error = errorMsg) }
            Result.failure(Exception(errorMsg))
        }
    }

    // ========== MÉTODOS PARA TABLAS RELACIONADAS (opcionales, para después) ==========

    /**
     * PANTALLA 3: Crear horarios (para después)
     */
    /**
     * PANTALLA 3: Crear horarios (IMPLEMENTACIÓN REAL)
     */
    /**
     * PANTALLA 3: Crear horarios (IMPLEMENTACIÓN CORREGIDA)
     */

    suspend fun crearHorarios(idNegocio: Int, horarios: List<DaySchedule>): Result<Unit> {
        return try {
            _ui.update { it.copy(mutando = true, error = null) }

            // Filtrar solo los días que están abiertos
            val horariosActivos = horarios.filter { it.isOpen.value }

            if (horariosActivos.isEmpty()) {
                println("⚠️ No hay horarios activos para guardar")
                _ui.update { it.copy(mutando = false, error = null) }
                return Result.success(Unit)
            }

            // Convertir DaySchedule a HorarioCreate
            val horariosCreate = horariosActivos.map { daySchedule ->
                HorarioCreate(
                    idNegocio = idNegocio,
                    diaSemana = daySchedule.day,
                    horaApertura = daySchedule.openTime.value,
                    horaCierre = daySchedule.closeTime.value
                )
            }

            println("📅 Creando ${horariosCreate.size} horarios para negocio $idNegocio")

            // Llamar al repositorio
            val horarioRepo = HorarioRepository()
            val resultados = horarioRepo.crearHorariosLote(horariosCreate)

            println("✅ Horarios creados exitosamente: ${resultados.size}")

            _ui.update { it.copy(mutando = false, error = null) }
            Result.success(Unit)
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Error al crear horarios"
            println("❌ Error creando horarios: $errorMsg")
            _ui.update { it.copy(mutando = false, error = errorMsg) }
            Result.failure(Exception(errorMsg))
        }
    }


//    suspend fun crearHorarios(idNegocio: Int, horarios: List<HorarioTemporal>): Result<Unit> {
//        return try {
//            _ui.update { it.copy(mutando = true, error = null) }
//
//            // TODO: Implementar llamada al repositorio de horarios
//            // Por ahora solo simulación
//            println("📅 Creando ${horarios.size} horarios para negocio $idNegocio")
//
//            _ui.update { it.copy(mutando = false, error = null) }
//            Result.success(Unit)
//        } catch (e: Exception) {
//            val errorMsg = e.message ?: "Error al crear horarios"
//            _ui.update { it.copy(mutando = false, error = errorMsg) }
//            Result.failure(Exception(errorMsg))
//        }
//    }

    /**
     * PANTALLA 4: Subir imágenes (para después)
     */
    suspend fun subirImagenes(idNegocio: Int, imagenesUris: List<String>): Result<Unit> {
        return try {
            _ui.update { it.copy(mutando = true, error = null) }

            // TODO: Implementar llamada al repositorio de imágenes
            println("🖼️ Subiendo ${imagenesUris.size} imágenes para negocio $idNegocio")

            _ui.update { it.copy(mutando = false, error = null) }
            Result.success(Unit)
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Error al subir imágenes"
            _ui.update { it.copy(mutando = false, error = errorMsg) }
            Result.failure(Exception(errorMsg))
        }
    }

    /**
     * PANTALLA 5: Crear servicios (para después)
     */
    suspend fun crearServicios(idNegocio: Int, servicios: List<ServicioTemporal>): Result<Unit> {
        return try {
            _ui.update { it.copy(mutando = true, error = null) }

            // TODO: Implementar llamada al repositorio de servicios
            println("⚙️ Creando ${servicios.size} servicios para negocio $idNegocio")

            _ui.update { it.copy(mutando = false, error = null) }
            Result.success(Unit)
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Error al crear servicios"
            _ui.update { it.copy(mutando = false, error = errorMsg) }
            Result.failure(Exception(errorMsg))
        }
    }

    /**
     * PANTALLA 7: Subir documentos (para después)
     */
    suspend fun subirDocumentos(idNegocio: Int, documentosUris: List<String>): Result<Unit> {
        return try {
            _ui.update { it.copy(mutando = true, error = null) }

            // TODO: Implementar llamada al repositorio de documentos
            println("📄 Subiendo ${documentosUris.size} documentos para negocio $idNegocio")

            _ui.update { it.copy(mutando = false, error = null) }
            Result.success(Unit)
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Error al subir documentos"
            _ui.update { it.copy(mutando = false, error = errorMsg) }
            Result.failure(Exception(errorMsg))
        }
    }
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

    // ========== MÉTODOS UTILITARIOS ==========

    /** Reiniciar el estado del registro */
    fun reiniciarRegistro() {
        _ui.update {
            it.copy(
                negocioCreadoId = null,
                registroCompletado = false,
                error = null
            )
        }
    }

    /** Limpiar errores */
    fun limpiarError() {
        _ui.update { it.copy(error = null) }
    }

    /** Obtener el ID del negocio en registro (para navegación) */
    fun obtenerIdNegocioEnRegistro(): Int? {
        return _ui.value.negocioCreadoId
    }

    /** Verificar si el registro está completado */
    fun estaRegistroCompletado(): Boolean {
        return _ui.value.registroCompletado
    }

    fun obtenerMiNegocio() = viewModelScope.launch {
        _ui.update { it.copy(isLoading = true, error = null) }
        try {
            val resp = api.getMiNegocio()
            if (resp.isSuccessful) {
                val body: ApiResponse<Negocio>? = resp.body()
                if (body?.success == true && body.data != null) {
                    _ui.update {
                        it.copy(isLoading = false, negocio = body.data)
                    }
                } else {
                    throw Exception(body?.message ?: "Negocio no encontrado")
                }
            } else {
                throw Exception("HTTP ${resp.code()}")
            }
        } catch (e: Exception) {
            _ui.update {
                it.copy(isLoading = false, error = e.message)
            }
        }
    }
}