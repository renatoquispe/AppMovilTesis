package com.tesis.appmovil.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.tesis.appmovil.viewmodel.PasswordRecoveryViewModel

// ResetPasswordScreen.kt - VERSIÓN ACTUALIZADA
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    vm: PasswordRecoveryViewModel,
    email: String,
    code: String,
    onBack: () -> Unit,
    onPasswordReset: () -> Unit
) {
    val state by vm.uiState.collectAsState()

    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    // 👇 IMPORTANTE: Actualizar email y código en el ViewModel cuando la pantalla se carga
    LaunchedEffect(email, code) {
        vm.onEmailChange(email)
        vm.onCodigoChange(code)
        // Limpiar campos de contraseña al entrar
        vm.onPasswordChange("")
        vm.onConfirmPasswordChange("")
    }

    // Efecto para navegar cuando la contraseña se restablece exitosamente
    LaunchedEffect(state.pasoActual) {
        if (state.pasoActual == 4) { // 👈 Contraseña restablecida exitosamente
            onPasswordReset()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Contraseña") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Crea una nueva contraseña",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Tu nueva contraseña debe ser diferente a la anterior",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // Nueva contraseña
            OutlinedTextField(
                value = state.password, // 👈 Cambiado de state.nuevaContrasena a state.password
                onValueChange = vm::onPasswordChange, // 👈 Cambiado de onNuevaContrasenaChange a onPasswordChange
                placeholder = { Text("Nueva contraseña") },
                label = { Text("Nueva contraseña") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                isError = state.error != null
            )

            Spacer(Modifier.height(16.dp))

            // Confirmar contraseña
            OutlinedTextField(
                value = state.confirmPassword, // 👈 Cambiado de confirmPassword local a state.confirmPassword
                onValueChange = vm::onConfirmPasswordChange, // 👈 Usa la función del ViewModel
                placeholder = { Text("Confirmar contraseña") },
                label = { Text("Confirmar contraseña") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showConfirmPassword) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                isError = state.error != null
            )

            // Mensajes de éxito/error
            state.successMessage?.let { message ->
                Spacer(Modifier.height(16.dp))
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            state.error?.let { error ->
                Spacer(Modifier.height(16.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(32.dp))

            // Botón de restablecer
            Button(
                onClick = {
                    vm.restablecerContrasena() // 👈 Ya incluye la validación de coincidencia
                },
                enabled = !state.isLoading &&
                        state.password.length >= 6 &&
                        state.confirmPassword.length >= 6 &&
                        state.password == state.confirmPassword,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("RESTABLECER CONTRASEÑA")
                }
            }

            // Mensaje de confirmación
            if (state.password.isNotEmpty() && state.confirmPassword.isNotEmpty() && state.password != state.confirmPassword) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Las contraseñas no coinciden",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ResetPasswordScreen(
//    vm: PasswordRecoveryViewModel,
//    email: String,
//    code: String,
//    onBack: () -> Unit,
//    onPasswordReset: () -> Unit
//) {
//    val state by vm.uiState.collectAsState()
//
//    var showPassword by remember { mutableStateOf(false) }
//    var showConfirmPassword by remember { mutableStateOf(false) }
//    var confirmPassword by remember { mutableStateOf("") }
//
//    // 👇 IMPORTANTE: Actualizar email y código en el ViewModel cuando la pantalla se carga
//    LaunchedEffect(email, code) {
//        vm.onEmailChange(email)
//        vm.onCodigoChange(code) // 👈 ESTA LÍNEA FALTABA
//    }
//
//    // Efecto para navegar cuando la contraseña se restablece exitosamente
//    LaunchedEffect(state.successMessage) {
//        if (state.successMessage != null && state.successMessage!!.contains("actualizada")) {
//            onPasswordReset()
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Nueva Contraseña") },
//                navigationIcon = {
//                    IconButton(onClick = onBack) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
//                    }
//                }
//            )
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//                .padding(24.dp),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(
//                "Crea una nueva contraseña",
//                style = MaterialTheme.typography.headlineMedium
//            )
//
//            Spacer(Modifier.height(8.dp))
//
//            Text(
//                "Tu nueva contraseña debe ser diferente a la anterior",
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                textAlign = androidx.compose.ui.text.style.TextAlign.Center
//            )
//
//            Spacer(Modifier.height(32.dp))
//
//            // Nueva contraseña
//            OutlinedTextField(
//                value = state.nuevaContrasena,
//                onValueChange = vm::onNuevaContrasenaChange,
//                placeholder = { Text("Nueva contraseña") },
//                label = { Text("Nueva contraseña") },
//                singleLine = true,
//                modifier = Modifier.fillMaxWidth(),
//                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
//                trailingIcon = {
//                    IconButton(onClick = { showPassword = !showPassword }) {
//                        Icon(
//                            if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
//                            contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña"
//                        )
//                    }
//                },
//                isError = state.error != null
//            )
//
//            Spacer(Modifier.height(16.dp))
//
//            // Confirmar contraseña
//            OutlinedTextField(
//                value = confirmPassword,
//                onValueChange = { confirmPassword = it },
//                placeholder = { Text("Confirmar contraseña") },
//                label = { Text("Confirmar contraseña") },
//                singleLine = true,
//                modifier = Modifier.fillMaxWidth(),
//                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
//                trailingIcon = {
//                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
//                        Icon(
//                            if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
//                            contentDescription = if (showConfirmPassword) "Ocultar contraseña" else "Mostrar contraseña"
//                        )
//                    }
//                },
//                isError = state.error != null
//            )
//
//            // Mensajes de éxito/error
//            state.successMessage?.let { message ->
//                Spacer(Modifier.height(16.dp))
//                Text(
//                    text = message,
//                    color = MaterialTheme.colorScheme.primary,
//                    style = MaterialTheme.typography.bodyMedium
//                )
//            }
//
//            state.error?.let { error ->
//                Spacer(Modifier.height(16.dp))
//                Text(
//                    text = error,
//                    color = MaterialTheme.colorScheme.error,
//                    style = MaterialTheme.typography.bodyMedium
//                )
//            }
//
//            Spacer(Modifier.height(32.dp))
//
//            // Botón de restablecer
//            Button(
//                onClick = {
//                    if (state.nuevaContrasena != confirmPassword) {
//                        vm.limpiarMensajes()
//                        // Puedes mostrar un error aquí si quieres
//                    } else {
//                        vm.restablecerContrasena()
//                    }
//                },
//                enabled = !state.isLoading &&
//                        state.nuevaContrasena.length >= 6 &&
//                        confirmPassword.length >= 6 &&
//                        state.nuevaContrasena == confirmPassword,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(56.dp)
//            ) {
//                if (state.isLoading) {
//                    CircularProgressIndicator(
//                        modifier = Modifier.size(20.dp),
//                        strokeWidth = 2.dp
//                    )
//                } else {
//                    Text("RESTABLECER CONTRASEÑA")
//                }
//            }
//
//            // Mensaje de confirmación
//            if (state.nuevaContrasena.isNotEmpty() && confirmPassword.isNotEmpty() && state.nuevaContrasena != confirmPassword) {
//                Spacer(Modifier.height(8.dp))
//                Text(
//                    text = "Las contraseñas no coinciden",
//                    color = MaterialTheme.colorScheme.error,
//                    style = MaterialTheme.typography.bodySmall
//                )
//            }
//        }
//    }
//}