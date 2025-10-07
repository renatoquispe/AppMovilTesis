package com.tesis.appmovil.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.tesis.appmovil.R
import com.tesis.appmovil.viewmodel.AuthViewModel
import com.tesis.appmovil.viewmodel.PasswordRecoveryViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RegisterScreen(
    vm: AuthViewModel,
    recoveryVm: PasswordRecoveryViewModel, // 👈 ViewModel para verificación
    onSuccess: (String) -> Unit,
    onNavigateToLogin: () -> Unit   // Para el botón "Iniciar Sesión"
) {
    val state by vm.uiState.collectAsState()
    val recoveryState by recoveryVm.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    var registroExitoso by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordErrorMessage by remember { mutableStateOf("") }

    // Validación: mínimo 8 caracteres, al menos 1 mayúscula y 1 minúscula
    fun isPasswordValid(pw: String): Boolean {
        if (pw.length < 8) return false
        if (!pw.any { it.isUpperCase() }) return false
        if (!pw.any { it.isLowerCase() }) return false
        return true
    }

    LaunchedEffect(Unit) {
        vm.clearTransient()
        recoveryVm.resetearFlujo()
        registroExitoso = false
    }

    LaunchedEffect(state.user) {
        if (state.user != null && !registroExitoso) {
            registroExitoso = true
            onSuccess(state.email)
        }
    }

    LaunchedEffect(recoveryState.pasoActual) {
        if (recoveryState.pasoActual == 2 && recoveryState.successMessage != null) {
            onSuccess(state.email)
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }
    LaunchedEffect(recoveryState.error) {
        recoveryState.error?.let { error ->
            Toast.makeText(context, "Error en verificación: $error", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
            Text("Crear cuenta", style = MaterialTheme.typography.displayLarge)
        }

        Spacer(Modifier.height(6.dp))

        // Link a Login manual
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("¿Ya tienes una cuenta? ", style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onNavigateToLogin, contentPadding = PaddingValues(0.dp)) {
                Text("Iniciar Sesión", color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(18.dp))

        // Nombre
        OutlinedTextField(
            value = state.name,
            onValueChange = vm::onUpdateName,
            placeholder = { Text("Alex") },
            label = { Text("Nombre Completo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor    = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor  = Color.Gray,
                cursorColor           = MaterialTheme.colorScheme.primary,
                focusedLabelColor     = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(Modifier.height(12.dp))

        // Email
        OutlinedTextField(
            value = state.email,
            onValueChange = vm::onEmailChange,
            placeholder = { Text("example@gmail.com") },
            label = { Text("Correo electrónico") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor    = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor  = Color.Gray,
                cursorColor           = MaterialTheme.colorScheme.primary,
                focusedLabelColor     = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(Modifier.height(12.dp))

        // Password con validación en tiempo real
        OutlinedTextField(
            value = state.password,
            onValueChange = {
                vm.onPasswordChange(it)
                passwordErrorMessage = if (!isPasswordValid(it)) {
                    "La contraseña debe tener mínimo 8 caracteres y contener mayúscula y minúscula"
                } else {
                    ""
                }
            },
            placeholder = { Text("*******") },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),

            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor    = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor  = Color.Gray,
                cursorColor           = MaterialTheme.colorScheme.primary,
                focusedLabelColor     = MaterialTheme.colorScheme.primary
            )
        )

        if (passwordErrorMessage.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(passwordErrorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        } else {
            Spacer(Modifier.height(8.dp))
            Text("Requisitos: mínimo 8 caracteres, 1 mayúscula y 1 minúscula", style = MaterialTheme.typography.bodySmall)
        }

        // Mostrar errores del state (si los hay)
        state.error?.let { errorMsg ->
            Spacer(Modifier.height(8.dp))
            Text(errorMsg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(18.dp))

        // Botón Registrar: solo habilitado si la contraseña cumple la regla
        Button(
            onClick = {
                focusManager.clearFocus()
                keyboard?.hide()
                // Validar antes de hacer register
                if (!isPasswordValid(state.password)) {
                    Toast.makeText(context, "La contraseña no cumple los requisitos", Toast.LENGTH_LONG).show()
                    return@Button
                }
                vm.register(
                    nombre   = state.name,
                    email    = state.email,
                    password = state.password
                )
            },
            enabled = !state.isLoading && !recoveryState.isLoading && isPasswordValid(state.password),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier    = Modifier.size(20.dp),
                    color       = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("REGISTRARSE")
            }
        }
    }
}
