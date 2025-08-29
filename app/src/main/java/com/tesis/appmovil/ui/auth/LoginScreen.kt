package com.tesis.appmovil.ui.auth

import androidx.compose.ui.tooling.preview.Preview


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.tesis.appmovil.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    vm: AuthViewModel,
    onSuccess: () -> Unit
) {
    val state by vm.uiState.collectAsState()

    // Si ya hay user -> éxito
    LaunchedEffect(state.user) {
        if (state.user != null) onSuccess()
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Iniciar sesión", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = state.email,
                onValueChange = vm::onEmailChange,
                label = { Text("Correo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.password,
                onValueChange = vm::onPasswordChange,
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            if (state.error != null) {
                Text(
                    state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = { vm.login() },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Entrar")
                }
            }
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    // Creamos un estado simulado
    var email by remember { mutableStateOf("usuario@ejemplo.com") }
    var password by remember { mutableStateOf("123456") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Iniciar sesión", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            if (error != null) {
                Text(
                    error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = { /* Acción simulada */ },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Entrar")
                }
            }
        }
    }
}

