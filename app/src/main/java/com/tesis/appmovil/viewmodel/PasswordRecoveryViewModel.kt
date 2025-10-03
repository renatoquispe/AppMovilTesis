package com.tesis.appmovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.appmovil.data.remote.RetrofitClient
import com.tesis.appmovil.data.remote.request.RestablecerContrasenaRequest
import com.tesis.appmovil.data.remote.request.SolicitarCodigoRequest
import com.tesis.appmovil.data.remote.request.VerificarCodigoRequest
import com.tesis.appmovil.repository.PasswordRecoveryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
// En PasswordRecoveryViewModel.kt
// PasswordRecoveryViewModel.kt
class PasswordRecoveryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PasswordRecoveryUiState())
    val uiState: StateFlow<PasswordRecoveryUiState> = _uiState

    private var flujoActual: FlujoVerificacion = FlujoVerificacion.RECUPERACION
    private var codigoSolicitado = false // 👈 NUEVO: Prevenir múltiples solicitudes
    private var solicitudEnProgreso = false // 👈 NUEVO: Para evitar solicitudes simultáneas

    private var registroVerificacionSolicitado = false

    // ✅ Configurar el flujo
//    fun configurarFlujo(flujo: FlujoVerificacion) {
//        flujoActual = flujo
//        resetearFlujo()
//    }

    // ✅ Funciones para actualizar campos (que usa tu VerifyCodeScreen actual)
    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    fun onCodigoChange(codigo: String) {
        _uiState.value = _uiState.value.copy(codigo = codigo, error = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword, error = null)
    }

    // ✅ Función de verificación que usa tu VerifyCodeScreen actual
    fun verificarCodigo() {
        val state = _uiState.value
        if (state.codigo.isBlank()) {
            _uiState.value = state.copy(error = "Ingresa el código")
            return
        }

        _verificarCodigo(state.email, state.codigo, flujoActual)
    }

    // ✅ Función privada común para verificar código
    private fun _verificarCodigo(email: String, codigo: String, flujo: FlujoVerificacion) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                // ✅ USA LA RUTA CORRECTA SEGÚN EL FLUJO
                val response = when (flujo) {
                    FlujoVerificacion.RECUPERACION ->
                        RetrofitClient.api.verificarCodigoRecuperacion(VerificarCodigoRequest(email, codigo))
                    FlujoVerificacion.REGISTRO ->
                        RetrofitClient.api.verificarCodigoEmail(VerificarCodigoRequest(email, codigo))
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pasoActual = 3, // Código verificado
                        error = null,
                        successMessage = when (flujo) {
                            FlujoVerificacion.REGISTRO -> "Email verificado exitosamente"
                            FlujoVerificacion.RECUPERACION -> "Código verificado exitosamente"
                        }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = response.body()?.message ?: "Código inválido"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error: ${e.message ?: "No se pudo verificar el código"}"
                )
            }
        }
    }
    fun solicitarCodigoVerificacion(email: String) {
        // ✅ PROTECCIÓN DOBLE
        if (solicitudEnProgreso || registroVerificacionSolicitado) {
            println("🚫 VERIFICACIÓN REGISTRO BLOQUEADA:")
            if (solicitudEnProgreso) println("   - Solicitud en progreso")
            if (registroVerificacionSolicitado) println("   - Verificación de registro ya solicitada")
            return
        }

        println("✅ SOLICITANDO VERIFICACIÓN DE REGISTRO para: $email")
        registroVerificacionSolicitado = true
        flujoActual = FlujoVerificacion.REGISTRO
        _solicitarCodigo(email, "verificacion")
    }

    fun solicitarCodigoRecuperacion(email: String) {
        if (solicitudEnProgreso) {
            println("⚠️ Solicitud en progreso, ignorando...")
            return
        }

        flujoActual = FlujoVerificacion.RECUPERACION
        _solicitarCodigo(email, "recuperacion")
    }

//    fun solicitarCodigoRecuperacion(email: String) {
//        if (solicitudEnProgreso) {
//            println("⚠️ Solicitud en progreso, ignorando...")
//            return
//        }
//
//        flujoActual = FlujoVerificacion.RECUPERACION
//        _solicitarCodigo(email, "recuperacion")
//    }
//    fun solicitarCodigoVerificacion(email: String) {
//        if (solicitudEnProgreso) {
//            println("⚠️ Solicitud en progreso, ignorando...")
//            return
//        }
//
//        flujoActual = FlujoVerificacion.REGISTRO
//        _solicitarCodigo(email, "verificacion")
//    }
    // ✅ Función privada común para solicitar código
    private fun _solicitarCodigo(email: String, tipo: String) {
    solicitudEnProgreso = true
    _uiState.value = _uiState.value.copy(isLoading = true, error = null)

    viewModelScope.launch {
        try {
            println("🔐 _solicitarCodigo - Email: $email, Tipo: $tipo")

            val response = when (tipo) {
                "recuperacion" ->
                    RetrofitClient.api.solicitarCodigoRecuperacion(SolicitarCodigoRequest(email))
                "verificacion" ->
                    RetrofitClient.api.solicitarCodigoVerificacion(SolicitarCodigoRequest(email))
                else -> throw IllegalArgumentException("Tipo inválido")
            }

            if (response.isSuccessful && response.body()?.success == true) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    pasoActual = 2,
                    error = null,
                    email = email,
                    successMessage = "Código enviado a $email"
                )
                println("✅ Código enviado exitosamente - Tipo: $tipo")
            } else {
                // ❌ RESET EN CASO DE ERROR
                if (tipo == "verificacion") registroVerificacionSolicitado = false
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = response.body()?.message ?: "Error al enviar código"
                )
                println("❌ Error al solicitar código")
            }
        } catch (e: Exception) {
            // ❌ RESET EN CASO DE EXCEPCIÓN
            if (tipo == "verificacion") registroVerificacionSolicitado = false
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Error: ${e.message ?: "No se pudo enviar el código"}"
            )
            println("❌ Excepción al solicitar código: ${e.message}")
        } finally {
            solicitudEnProgreso = false
        }
    }
}
//    private fun _solicitarCodigo(email: String, tipo: String) {
//        if (codigoSolicitado || solicitudEnProgreso) {
//            println("⚠️ Código ya solicitado o solicitud en progreso, ignorando...")
//            return
//        }
//
//        codigoSolicitado = true
//        solicitudEnProgreso = true
//        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
//
//        viewModelScope.launch {
//            try {
//                // ✅ USA LA RUTA CORRECTA SEGÚN EL TIPO
//                val response = when (tipo) {
//                    "recuperacion" ->
//                        RetrofitClient.api.solicitarCodigoRecuperacion(SolicitarCodigoRequest(email))
//                    "verificacion" ->
//                        RetrofitClient.api.solicitarCodigoVerificacion(SolicitarCodigoRequest(email))
//                    else -> throw IllegalArgumentException("Tipo de solicitud inválido")
//                }
//
//                if (response.isSuccessful && response.body()?.success == true) {
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        pasoActual = 2, // Código enviado
//                        error = null,
//                        email = email,
//                        successMessage = "Código enviado a $email"
//                    )
//                    println("✅ Código solicitado exitosamente - Tipo: $tipo")
//                } else {
//                    codigoSolicitado = false
//                    solicitudEnProgreso = false
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        error = response.body()?.message ?: "Error al enviar código"
//                    )
//                }
//            } catch (e: Exception) {
//                codigoSolicitado = false
//                solicitudEnProgreso = false
//                _uiState.value = _uiState.value.copy(
//                    isLoading = false,
//                    error = "Error: ${e.message ?: "No se pudo enviar el código"}"
//                )
//            } finally {
//                solicitudEnProgreso = false
//            }
//        }
//    }
    // ✅ Para recuperación de contraseña
//    fun solicitarCodigoRecuperacion(email: String) {
//        flujoActual = FlujoVerificacion.RECUPERACION
//        _solicitarCodigo(email, "recuperacion")
//    }

    // ✅ Para verificación de registro
//    fun solicitarCodigoVerificacion(email: String) {
//        flujoActual = FlujoVerificacion.REGISTRO
//        _solicitarCodigo(email, "verificacion")
//    }



    // ✅ Restablecer contraseña (solo para recuperación)
    fun restablecerContrasena() {
        val state = _uiState.value
        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(error = "Las contraseñas no coinciden")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.restablecerContrasena(
                    RestablecerContrasenaRequest(state.email, state.codigo, state.password)
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pasoActual = 4, // Contraseña restablecida
                        error = null,
                        successMessage = "Contraseña restablecida exitosamente"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = response.body()?.message ?: "Error al restablecer contraseña"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error: ${e.message ?: "No se pudo restablecer la contraseña"}"
                )
            }
        }
    }
    fun resetearFlujo() {
        codigoSolicitado = false
        solicitudEnProgreso = false
        _uiState.value = PasswordRecoveryUiState()
        println("🔄 Flujo COMPLETAMENTE resetado")
    }
    // También resetear cuando se cambia de flujo
    fun configurarFlujo(flujo: FlujoVerificacion) {
        resetearFlujo() // 👈 Resetear al cambiar de flujo
        flujoActual = flujo
        println("🔄 Configurando flujo: $flujo")
    }
//    fun resetearFlujo() {
//        codigoSolicitado = false
//        _uiState.value = PasswordRecoveryUiState()
//    }
//    fun resetearFlujo() {
//        _uiState.value = PasswordRecoveryUiState()
//    }
    // En PasswordRecoveryViewModel.kt - AGREGA esta función
    fun solicitarCodigo() {
        val state = _uiState.value
        if (state.email.isBlank()) {
            _uiState.value = state.copy(error = "Ingresa tu correo electrónico")
            return
        }

        // Para el flujo de recuperación de contraseña
        flujoActual = FlujoVerificacion.RECUPERACION
        _solicitarCodigo(state.email, "recuperacion")
    }
}

// Enum para los tipos de flujo
enum class FlujoVerificacion {
    RECUPERACION, // Para "Olvidé mi contraseña"
    REGISTRO      // Para verificación de email después del registro
}

// Actualiza tu data class para incluir todos los campos necesarios
data class PasswordRecoveryUiState(
    val isLoading: Boolean = false,
    val pasoActual: Int = 1, // 1=Solicitar, 2=Código enviado, 3=Verificado, 4=Completado
    val error: String? = null,
    val successMessage: String? = null,
    val email: String = "",
    val codigo: String = "",
    val password: String = "",
    val confirmPassword: String = ""
)



//class PasswordRecoveryViewModel : ViewModel() {
//    private val repository = PasswordRecoveryRepository()
//    private val _uiState = MutableStateFlow(PasswordRecoveryState())
//    val uiState: StateFlow<PasswordRecoveryState> = _uiState
//
//    fun onEmailChange(email: String) {
//        _uiState.value = _uiState.value.copy(email = email, error = null)
//    }
//
//    fun onCodigoChange(codigo: String) {
//        _uiState.value = _uiState.value.copy(codigo = codigo, error = null)
//    }
//
//    fun onNuevaContrasenaChange(contrasena: String) {
//        _uiState.value = _uiState.value.copy(nuevaContrasena = contrasena, error = null)
//    }
//
//    fun solicitarCodigo() {
//        val email = _uiState.value.email
//        if (email.isBlank()) {
//            _uiState.value = _uiState.value.copy(error = "Ingresa tu correo electrónico")
//            return
//        }
//
//        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
//
//        viewModelScope.launch {
//            try {
//                val response = repository.solicitarCodigoRecuperacion(email)
//                if (response.isSuccessful && response.body()?.success == true) {
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        pasoActual = 2,
//                        successMessage = "Código enviado a tu correo"
//                    )
//                } else {
//                    val errorMsg = response.body()?.message ?: "Error al enviar código"
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        error = errorMsg
//                    )
//                }
//            } catch (e: Exception) {
//                _uiState.value = _uiState.value.copy(
//                    isLoading = false,
//                    error = e.message ?: "Error de conexión"
//                )
//            }
//        }
//    }
//
//    fun verificarCodigo() {
//        val state = _uiState.value
//        if (state.codigo.length != 6) {
//            _uiState.value = state.copy(error = "El código debe tener 6 dígitos")
//            return
//        }
//
//        _uiState.value = state.copy(isLoading = true, error = null)
//
//        viewModelScope.launch {
//            try {
//                val response = repository.verificarCodigo(state.email, state.codigo)
//                if (response.isSuccessful && response.body()?.success == true) {
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        pasoActual = 3,
//                        successMessage = "Código verificado correctamente"
//                    )
//                } else {
//                    val errorMsg = response.body()?.message ?: "Código inválido"
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        error = errorMsg
//                    )
//                }
//            } catch (e: Exception) {
//                _uiState.value = _uiState.value.copy(
//                    isLoading = false,
//                    error = e.message ?: "Error de conexión"
//                )
//            }
//        }
//    }
//
//    fun restablecerContrasena() {
//        val state = _uiState.value
//        if (state.nuevaContrasena.length < 6) {
//            _uiState.value = state.copy(error = "La contraseña debe tener al menos 6 caracteres")
//            return
//        }
//
//        _uiState.value = state.copy(isLoading = true, error = null)
//
//        viewModelScope.launch {
//            try {
//                val response = repository.restablecerContrasena(
//                    state.email,
//                    state.codigo,
//                    state.nuevaContrasena
//                )
//                if (response.isSuccessful && response.body()?.success == true) {
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        successMessage = "Contraseña actualizada correctamente"
//                    )
//                } else {
//                    val errorMsg = response.body()?.message ?: "Error al actualizar contraseña"
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        error = errorMsg
//                    )
//                }
//            } catch (e: Exception) {
//                _uiState.value = _uiState.value.copy(
//                    isLoading = false,
//                    error = e.message ?: "Error de conexión"
//                )
//            }
//        }
//    }
//
//    fun limpiarMensajes() {
//        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
//    }
//
//    fun resetearFlujo() {
//        _uiState.value = PasswordRecoveryState()
//    }
//    // Agregar estos métodos a tu PasswordRecoveryViewModel
//    fun solicitarCodigoVerificacion(email: String) {
//        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
//
//        viewModelScope.launch {
//            try {
//                val response = repository.solicitarCodigoVerificacion(email)
//                if (response.isSuccessful && response.body()?.success == true) {
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        pasoActual = 2, // Código enviado
//                        successMessage = "Código de verificación enviado a tu correo"
//                    )
//                } else {
//                    val errorMsg = response.body()?.message ?: "Error al enviar código de verificación"
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        error = errorMsg
//                    )
//                }
//            } catch (e: Exception) {
//                _uiState.value = _uiState.value.copy(
//                    isLoading = false,
//                    error = e.message ?: "Error de conexión"
//                )
//            }
//        }
//    }
//
//    fun verificarEmail() {
//        val state = _uiState.value
//        if (state.codigo.length != 6) {
//            _uiState.value = state.copy(error = "El código debe tener 6 dígitos")
//            return
//        }
//
//        _uiState.value = state.copy(isLoading = true, error = null)
//
//        viewModelScope.launch {
//            try {
//                val response = repository.verificarEmail(state.email, state.codigo)
//                if (response.isSuccessful && response.body()?.success == true) {
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        successMessage = "Email verificado correctamente"
//                    )
//                } else {
//                    val errorMsg = response.body()?.message ?: "Código inválido"
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        error = errorMsg
//                    )
//                }
//            } catch (e: Exception) {
//                _uiState.value = _uiState.value.copy(
//                    isLoading = false,
//                    error = e.message ?: "Error de conexión"
//                )
//            }
//        }
//    }
//    fun verificarCodigoEmail() {
//        val state = _uiState.value
//        println("🔐 DEBUG ViewModel: Verificando código. Email: ${state.email}, Código: ${state.codigo}")
//
//        if (state.codigo.length != 6) {
//            _uiState.value = state.copy(error = "El código debe tener 6 dígitos")
//            return
//        }
//
//        _uiState.value = state.copy(isLoading = true, error = null)
//
//        viewModelScope.launch {
//            try {
//                val response = repository.verificarEmail(state.email, state.codigo)
//                println("🔐 DEBUG ViewModel: Respuesta verificación - ${response.isSuccessful}")
//
//                if (response.isSuccessful && response.body()?.success == true) {
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        pasoActual = 3, // 👈 IMPORTANTE: Cambiar a paso 3 (verificado)
//                        successMessage = "Email verificado correctamente"
//                    )
//                    println("🔐 DEBUG ViewModel: Email verificado, pasoActual = 3")
//                } else {
//                    val errorMsg = response.body()?.message ?: "Código inválido o expirado"
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        error = errorMsg
//                    )
//                    println("🔐 DEBUG ViewModel: Error verificación - $errorMsg")
//                }
//            } catch (e: Exception) {
//                val errorMsg = "Error de conexión: ${e.message}"
//                _uiState.value = _uiState.value.copy(
//                    isLoading = false,
//                    error = errorMsg
//                )
//                println("🔐 DEBUG ViewModel: Excepción verificación - $errorMsg")
//            }
//        }
//    }
//
//
//}