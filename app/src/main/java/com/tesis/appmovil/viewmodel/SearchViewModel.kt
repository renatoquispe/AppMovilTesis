package com.tesis.appmovil.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class ServiceItem(
    val id: Int,
    val name: String,
    val isOpenNow: Boolean,
    val schedule: String,
    val price1: String,
    val price2: String,
    val imageUrl: String,
)

data class SearchUiState(
    val query: String = "",
    val popular: List<ServiceItem> = emptyList()
)

class SearchViewModel : ViewModel() {

    var state by mutableStateOf(
        SearchUiState(
            popular = listOf(
                ServiceItem(
                    id = 1,
                    name = "JENCY CREANDO ESTILO",
                    isOpenNow = true,
                    schedule = "Abierto ahora · 08:00–14:00",
                    price1 = "S/45",
                    price2 = "S/85",
                    imageUrl = "https://images.unsplash.com/photo-1517832606299-7ae9b720a186?q=80&w=1200"
                )
            )
        )
    )
        private set

    fun onQueryChange(q: String) {
        state = state.copy(query = q)
        // TODO: luego llamas a tu backend aquí
    }
}

