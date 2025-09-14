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
import androidx.compose.ui.unit.dp
import com.tesis.appmovil.R
import com.tesis.appmovil.viewmodel.AuthViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RegisterScreen(
    vm: AuthViewModel,
    onSuccess: () -> Unit,          // Navega al Login
    onNavigateToLogin: () -> Unit   // Para el botón "Iniciar Sesión"
) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    // Al entrar, resetea estados de éxito/error
    LaunchedEffect(Unit) {
        vm.clearTransient()
    }

    // Cuando el registro sea exitoso (state.user != null)
    LaunchedEffect(state.user) {
        if (state.user != null) {
            Toast.makeText(context, "Registrado correctamente", Toast.LENGTH_LONG).show()
            onSuccess()
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

        // Password
        OutlinedTextField(
            value = state.password,
            onValueChange = vm::onPasswordChange,
            placeholder = { Text("*******") },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor    = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor  = Color.Gray,
                cursorColor           = MaterialTheme.colorScheme.primary,
                focusedLabelColor     = MaterialTheme.colorScheme.primary
            )
        )

        state.error?.let { errorMsg ->
            Spacer(Modifier.height(8.dp))
            Text(errorMsg, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(18.dp))

        // Botón Registrar
        Button(
            onClick = {
                focusManager.clearFocus()  // cierra teclado
                keyboard?.hide()
                vm.register(
                    nombre   = state.name,
                    email    = state.email,
                    password = state.password
                )
            },
            enabled = !state.isLoading,
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

        // ... resto de tu UI (separador, botones sociales) ...
    }
}
