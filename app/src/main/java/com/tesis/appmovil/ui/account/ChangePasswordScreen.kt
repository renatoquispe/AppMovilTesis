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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tesis.appmovil.viewmodel.AuthViewModel
import com.tesis.appmovil.viewmodel.UsuarioViewModel
import com.tesis.appmovil.data.remote.dto.UsuarioUpdate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    navController: NavController? = null,
    authVM: AuthViewModel = viewModel(), // ← Usamos el mismo ViewModel de auth
    onBackClick: () -> Unit = {},
    onPasswordChanged: () -> Unit = {}
) {
    val usuarioVM: UsuarioViewModel = viewModel()
    val authState by authVM.uiState.collectAsState()

    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

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
            PasswordTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = "Nueva contraseña",
                isPasswordVisible = newPasswordVisible,
                onVisibilityChange = { newPasswordVisible = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirmar nueva contraseña",
                isPasswordVisible = confirmPasswordVisible,
                onVisibilityChange = { confirmPasswordVisible = it }
            )

            if (message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    color = if (isError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    when {
                        newPassword.isEmpty() -> {
                            message = "Ingresa una nueva contraseña"
                            isError = true
                        }
                        newPassword.length < 6 -> {
                            message = "La contraseña debe tener al menos 6 caracteres"
                            isError = true
                        }
                        newPassword != confirmPassword -> {
                            message = "Las contraseñas no coinciden"
                            isError = true
                        }
                        else -> {
                            val userId = authState.userId
                            if (userId != null) {
                                usuarioVM.actualizarUsuario(
                                    userId,
                                    UsuarioUpdate(
                                        contrasena = newPassword // 👈 solo la contraseña
                                    )
                                )
                                message = "Contraseña actualizada correctamente"
                                isError = false
                                onPasswordChanged()
                                navController?.popBackStack()
                            } else {
                                message = "No se encontró usuario logueado"
                                isError = true
                            }
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
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            Icon(
                imageVector = if (isPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                contentDescription = if (isPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                modifier = Modifier.clickable { onVisibilityChange(!isPasswordVisible) }
            )
        },
        shape = RoundedCornerShape(12.dp)
    )
}
