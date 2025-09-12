package com.tesis.appmovil.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.tesis.appmovil.viewmodel.AuthViewModel
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.tesis.appmovil.R

@Composable
fun RegisterScreen(
    vm: AuthViewModel,
    onSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val state by vm.uiState.collectAsState()

    // Si ya hay user registrado -> éxito
    LaunchedEffect(state.user) {
        if (state.user != null) onSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                "Crear cuenta",
                style = MaterialTheme.typography.displayLarge
            )
        }

        Spacer(Modifier.height(6.dp))

        // Texto para ir a Login
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "¿Ya tienes una cuenta? ",
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(
                onClick = { onNavigateToLogin() },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    "Iniciar Sesión",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
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
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(Modifier.height(12.dp))

        // Correo
        OutlinedTextField(
            value = state.email,
            onValueChange = vm::onEmailChange,
            placeholder = { Text("example@gmail.com") },
            label = { Text("Correo electrónico") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(Modifier.height(12.dp))

        // Contraseña
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
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )

        state.error?.let { errorMsg ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = errorMsg,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(18.dp))

        // Botón de registro
        Button(
            onClick = {
                vm.register(
                    nombre = state.name,
                    email = state.email,
                    password = state.password
                )
            },
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("REGISTRARSE")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Separador
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Divider(Modifier.weight(1f))
            Text("  o  ")
            Divider(Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        // Botones sociales
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            IconButton(
                onClick = { /* TODO Facebook */ },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_facebook),
                    contentDescription = "Facebook",
                    tint = Color.Unspecified
                )
            }
            IconButton(
                onClick = { /* TODO Google */ },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google",
                    tint = Color.Unspecified
                )
            }
        }
    }
}

//@Composable
//fun RegisterScreen(
//    vm: AuthViewModel,
//    onSuccess: () -> Unit
//) {
//    val state by vm.uiState.collectAsState()
//
//    // Si ya hay user registrado -> éxito
//    LaunchedEffect(state.user) {
//        if (state.user != null) onSuccess()
//    }
//
//    Box(Modifier.fillMaxSize()) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .align(Alignment.Center)
//                .padding(24.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text("Registro", style = MaterialTheme.typography.headlineMedium)
//
//            Spacer(Modifier.height(16.dp))
//
//            OutlinedTextField(
//                value = state.name ?: "",
//                onValueChange = { vm.onUpdateName(it) },
//                label = { Text("Nombre") },
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            Spacer(Modifier.height(8.dp))
//
//            OutlinedTextField(
//                value = state.email,
//                onValueChange = { vm.onEmailChange(it) },
//                label = { Text("Email") },
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            Spacer(Modifier.height(8.dp))
//
//            OutlinedTextField(
//                value = state.password,
//                onValueChange = { vm.onPasswordChange(it) },
//                label = { Text("Contraseña") },
//                visualTransformation = PasswordVisualTransformation(),
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            Spacer(Modifier.height(16.dp))
//
//            Button(
//                onClick = { vm.register(state.name ?: "", state.email, state.password) },
//                enabled = !state.isLoading,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("Crear cuenta")
//            }
//
//            state.error?.let {
//                Spacer(Modifier.height(8.dp))
//                Text(it, color = MaterialTheme.colorScheme.error)
//            }
//        }
//    }
//}
