package com.tesis.appmovil.viewmodel

import androidx.lifecycle.ViewModel
import com.tesis.appmovil.data.FakeRepository
import com.tesis.appmovil.models.Service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class HomeUiState(
    val userName: String = "Nombre",
    val location: String = "Lima, Perú",
    val nearby: List<Service> = emptyList(),
    val featured: List<Service> = emptyList(),
    val deals: List<Service> = emptyList()
)

class HomeViewModel(
    private val repo: FakeRepository = FakeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            nearby = repo.getNearbyStyles(),
            featured = repo.getFeatured(),
            deals = repo.getDeals()
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState
}

