package com.tesis.appmovil.viewmodel

import androidx.lifecycle.ViewModel
import com.tesis.appmovil.repository.FakeRepository
import com.tesis.appmovil.repository.FakeAuthRepository
import com.tesis.appmovil.models.Service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


data class HomeUiState(
    val userName: String = "Invitado",
    val location: String = "Lima, Perú",
    val nearby: List<Service> = emptyList(),
    val featured: List<Service> = emptyList(),
    val deals: List<Service> = emptyList()
)

class HomeViewModel(
    private val repo: FakeRepository = FakeRepository(),
    private val authRepo: FakeAuthRepository = FakeAuthRepository // 👈 añadimos el repositorio de auth
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            userName = authRepo.getCurrentUser()?.name ?: "Invitado", // 👈 obtenemos el nombre real
            location = "Lima, Perú",
            nearby = repo.getOffers(),
            featured = repo.getStyles(),
            deals = repo.getServices()
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState
}

