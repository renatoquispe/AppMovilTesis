package com.tesis.appmovil.ui.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    navController: NavController? = null,
    onBackClick: () -> Unit = {},
    onPasswordChanged: () -> Unit = {}
) {
    // Estados para las contraseñas
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Estados para mostrar/ocultar contraseñas
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Estado para mensajes de error
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Cambiar contraseña") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController?.popBackStack() ?: onBackClick()
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Volver atrás"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            // Campo: Contraseña actual
            PasswordTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = "Contraseña actual",
                isPasswordVisible = currentPasswordVisible,
                onVisibilityChange = { currentPasswordVisible = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo: Nueva contraseña
            PasswordTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = "Nueva contraseña",
                isPasswordVisible = newPasswordVisible,
                onVisibilityChange = { newPasswordVisible = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo: Confirmar contraseña
            PasswordTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirmar nueva contraseña",
                isPasswordVisible = confirmPasswordVisible,
                onVisibilityChange = { confirmPasswordVisible = it }
            )

            // Mensaje de error
            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón para guardar cambios
            Button(
                onClick = {
                    // Validaciones
                    when {
                        currentPassword.isEmpty() -> {
                            errorMessage = "Ingresa tu contraseña actual"
                        }
                        newPassword.isEmpty() -> {
                            errorMessage = "Ingresa una nueva contraseña"
                        }
                        newPassword.length < 6 -> {
                            errorMessage = "La contraseña debe tener al menos 6 caracteres"
                        }
                        newPassword != confirmPassword -> {
                            errorMessage = "Las contraseñas no coinciden"
                        }
                        else -> {
                            // Aquí iría la lógica para cambiar la contraseña
                            errorMessage = ""
                            // Simulamos el cambio exitoso
                            onPasswordChanged()
                            navController?.popBackStack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Guardar cambios")
            }
        }
    }
}

@Composable
private fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPasswordVisible: Boolean,
    onVisibilityChange: (Boolean) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (isPasswordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            Icon(
                imageVector = if (isPasswordVisible) {
                    Icons.Outlined.Visibility
                } else {
                    Icons.Outlined.VisibilityOff
                },
                contentDescription = if (isPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                modifier = Modifier.clickable {
                    onVisibilityChange(!isPasswordVisible)
                }
            )
        },
        shape = RoundedCornerShape(12.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun ChangePasswordScreenPreview() {
    MaterialTheme {
        ChangePasswordScreen()
    }
}