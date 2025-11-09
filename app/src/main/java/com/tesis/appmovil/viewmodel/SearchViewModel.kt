package com.tesis.appmovil.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.appmovil.models.Negocio
import com.tesis.appmovil.repository.NegocioRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** UI model (igual que antes, lo dejamos simple) */
data class ServiceItem(
    val id: Int,
    val name: String,
    val isOpenNow: Boolean = false,
    val schedule: String = "",
    val price1: String = "",
    val price2: String = "",
    val imageUrl: String = ""
)

data class SearchUiState(
    val query: String = "",
    val popular: List<ServiceItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class SearchViewModel(
    private val negocioRepo: NegocioRepository = NegocioRepository()
) : ViewModel() {

    var state by mutableStateOf(SearchUiState())
        private set

    init {
        // carga inicial
        loadPopular()
    }

    /** Actualiza query y lanza búsqueda */
    fun onQueryChange(q: String) {
        state = state.copy(query = q)
        // búsqueda simple (sin debounce). Si q es vacío volvemos a la lista popular.
        if (q.isBlank()) {
            loadPopular()
        } else {
            search(q)
        }
    }

    private fun loadPopular() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            try {
                val negocios = negocioRepo.listar()
                    .filter { it.estado_auditoria == 1 } // ✅ solo los habilitados
                val items = fetchServiceItemsWithImages(negocios)
                state = state.copy(popular = items, isLoading = false)
            } catch (t: Throwable) {
                state = state.copy(isLoading = false, error = t.message ?: "Error al cargar negocios")
            }
        }
    }

    private fun search(q: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            try {
                val negocios = negocioRepo.listar(q = q)
                    .filter { it.estado_auditoria == 1 } // ✅ igual aquí
                val items = fetchServiceItemsWithImages(negocios)
                state = state.copy(popular = items, isLoading = false)
            } catch (t: Throwable) {
                state = state.copy(isLoading = false, error = t.message ?: "Error al buscar")
            }
        }
    }


    /**
     * Por cada Negocio hace una llamada a obtenerDetalle (concurrente) y extrae la primera imagen
     * si existe. Devuelve la lista de ServiceItem mapeada.
     *
     * Observación: esto hace N llamadas en paralelo (una por negocio). Si la lista puede ser
     * grande, reemplazar por una estrategia paginada o por un endpoint que ya devuelva imágenes
     * junto con la lista (recomendado).
     */
    private suspend fun fetchServiceItemsWithImages(negocios: List<Negocio>): List<ServiceItem> = coroutineScope {
        // lanzamos N tareas en paralelo (async) y awaitAll
        val deferred = negocios.map { negocio ->
            async {
                // intentamos traer detalle (si falla, devolvemos null y seguimos)
                val detalle = runCatching { negocioRepo.obtenerDetalle(negocio.id_negocio) }.getOrNull()

                // extraer primera imagen conocida (según tu modelo NegocioImagen -> url_imagen)
                val imagenUrl = detalle?.let { d ->
                    // asumimos que el response tiene una lista 'imagenes' de tipo NegocioImagen
                    // y que NegocioImagen.url_imagen es String (según lo que mostraste)
                    try {
                        val imgsField = d::class.java.getDeclaredField("imagenes").apply { isAccessible = true }
                        @Suppress("UNCHECKED_CAST")
                        val imgs = imgsField.get(d) as? List<Any>
                        val first = imgs?.firstOrNull()
                        first?.let {
                            // try direct property name "url_imagen"
                            try {
                                val f = it::class.java.getDeclaredField("url_imagen").apply { isAccessible = true }
                                f.get(it) as? String
                            } catch (_: NoSuchFieldException) {
                                // fallback a otras names si fuera necesario
                                null
                            }
                        }
                    } catch (_: NoSuchFieldException) {
                        null
                    }
                }

                // Mapea a ServiceItem. Como Negocio no tiene precios, dejamos placeholder.
                ServiceItem(
                    id = negocio.id_negocio,
                    name = negocio.nombre,
                    isOpenNow = true, // si tienes lógica real de horarios, ponla aquí
                    schedule = "",    // si dispones, mapea horario acá
                    price1 = "S/--",
                    price2 = "S/--",
                    imageUrl = imagenUrl ?: ""
                )
            }
        }

        deferred.awaitAll()
    }
}
