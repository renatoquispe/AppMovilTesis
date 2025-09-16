//package com.tesis.appmovil.viewmodel
//
//import androidx.lifecycle.ViewModel
//import com.tesis.appmovil.repository.FakeRepository
//import com.tesis.appmovil.repository.FakeAuthRepository
//import com.tesis.appmovil.models.Service
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//
//
//data class HomeUiState(
//    val userName: String = "Invitado",
//    val location: String = "Lima, Perú",
//    val nearby: List<Service> = emptyList(),
//    val featured: List<Service> = emptyList(),
//    val deals: List<Service> = emptyList()
//)
//
//class HomeViewModel(
//    private val repo: FakeRepository = FakeRepository(),
//    private val authRepo: FakeAuthRepository = FakeAuthRepository // 👈 añadimos el repositorio de auth
//) : ViewModel() {
//
//    private val _uiState = MutableStateFlow(
//        HomeUiState(
//            userName = authRepo.getCurrentUser()?.name ?: "Invitado", // 👈 obtenemos el nombre real
//            location = "Lima, Perú",
//            nearby = repo.getOffers(),
//            featured = repo.getStyles(),
//            deals = repo.getServices()
//        )
//    )
//    val uiState: StateFlow<HomeUiState> = _uiState
//}
//

package com.tesis.appmovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.appmovil.data.remote.RetrofitClient
import com.tesis.appmovil.models.Service
import com.tesis.appmovil.models.Servicio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

//data class HomeUiState(
//    val userName: String = "Invitado",
//    val location: String = "Lima, Perú",
//    val nearby: List<Service> = emptyList(),
//    val featured: List<Service> = emptyList(),
//    val deals: List<Service> = emptyList(),
//    val isLoading: Boolean = false,
//    val error: String? = null
//)
data class HomeUiState(
    val userName: String = "Invitado",
    val location: String = "Lima, Perú",
    val nearby: List<Servicio> = emptyList(),
    val featured: List<Servicio> = emptyList(),
    val deals: List<Servicio> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadServicios()
    }
    private fun loadServicios() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = RetrofitClient.api.getServicios()

                if (response.isSuccessful) {
                    val wrapper = response.body()
                    val servicios = wrapper?.data ?: emptyList()

                    println("✅ Servicios recibidos en ViewModel: ${servicios.size}")
                    println("➡️ Data: $servicios")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        nearby = servicios,
                        featured = servicios.take(4),
                        deals = servicios
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

}

