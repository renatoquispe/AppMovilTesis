package com.tesis.appmovil.ui.auth

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.tesis.appmovil.R
import com.tesis.appmovil.viewmodel.AuthViewModel
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    vm: AuthViewModel,
    onSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit // 👈 AGREGAR ESTE PARÁMETRO

) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as Activity

    // DEBUG
    // Después de un login exitoso
    LaunchedEffect(state.token) {
        state.token?.let { token ->
            vm.saveToken(context, token, state.expiry ?: (System.currentTimeMillis() + 7*24*60*60*1000))
        }
    }


    LaunchedEffect(state.userId) {
        if (state.userId != null) {
            println("✅ LOGIN EXITOSO - UserId: ${state.userId}")
        }
    }

    // Al iniciar la pantalla, carga token guardado
    LaunchedEffect(Unit) {
        vm.loadToken(context)
    }

    // 1) Configurar GoogleSignInClient (idToken + email)
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.google_web_client_id))
            .requestEmail()
            .build()
    }
    val googleClient: GoogleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    // 2) Launcher para recibir el resultado
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            val idToken = account.idToken
            if (!idToken.isNullOrBlank()) {
                vm.loginWithGoogle(idToken, context)
            } else {
                Toast.makeText(context, "No se obtuvo idToken de Google", Toast.LENGTH_SHORT).show()
            }
        } catch (e: com.google.android.gms.common.api.ApiException) {
            val code = e.statusCode
            android.util.Log.e("GoogleSignIn", "Error GoogleSignIn, code=$code", e)
            Toast.makeText(context, "Google Sign-In falló (code=$code)", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            android.util.Log.e("GoogleSignIn", "Error inesperado", e)
            Toast.makeText(context, "Error inesperado en Sign-In", Toast.LENGTH_LONG).show()
        }
    }

    // Navegación por éxito

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
                "Iniciar Sesión",
                style = MaterialTheme.typography.displayLarge
            )
        }

        Spacer(Modifier.height(2.dp))

        // Navegar a registro
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("¿No tienes una cuenta? ", style = MaterialTheme.typography.bodyMedium)
            TextButton(
                onClick = { onNavigateToRegister() },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Regístrate", color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(18.dp))

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

        Spacer(Modifier.height(14.dp))

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

        // Olvidaste contraseña
//        Box(Modifier.fillMaxWidth()) {
//            // Busca esta parte en tu LoginScreen y cámbiala:
//            TextButton(
//                onClick = { /* TODO recuperación */ },
//                modifier = Modifier.align(Alignment.CenterEnd)
//            ) {
//                Text("¿Olvidaste tu contraseña?", color = MaterialTheme.colorScheme.primary)
//            }
//        }
        Box(Modifier.fillMaxWidth()) {
            TextButton(
                onClick = onNavigateToForgotPassword, // 👈 USAR EL NUEVO PARÁMETRO
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text("¿Olvidaste tu contraseña?", color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Botón de inicio de sesión normal
        Button(
            onClick = { vm.login(context) },
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
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onSecondary
                )
            } else {
                Text("INICIAR SESIÓN")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Separador
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Divider(Modifier.weight(1f))
            Text("  o  ")
            Divider(Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        // Botones sociales
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Facebook (queda por implementar)
            IconButton(onClick = { /* TODO Facebook */ }, modifier = Modifier.size(48.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_facebook),
                    contentDescription = "Facebook",
                    tint = Color.Unspecified
                )
            }
            // Google Sign-In con cierre de sesión para forzar selector de cuentas
            IconButton(
                onClick = {
                    googleClient.signOut()
                        .addOnCompleteListener(OnCompleteListener<Void> {
                            googleLauncher.launch(googleClient.signInIntent)
                        })
                },
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
