package com.tesis.appmovil.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.TextFieldValue
import com.tesis.appmovil.viewmodel.FlujoVerificacion
import com.tesis.appmovil.viewmodel.PasswordRecoveryViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyCodeScreen(
    vm: PasswordRecoveryViewModel,
    email: String,
    tipoFlujo: FlujoVerificacion, // 👈 NUEVO PARÁMETRO
    onBack: () -> Unit,
    onCodeVerified: (String, String) -> Unit
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(Unit) {
        vm.configurarFlujo(tipoFlujo)
        vm.onEmailChange(email)

    }

    LaunchedEffect(state.pasoActual) {
        if (state.pasoActual == 3) {
            println("✅ VerifyCodeScreen: Código verificado, navegando...")
            onCodeVerified(state.email, state.codigo)
        }
    }
//    LaunchedEffect(Unit) {
//        if (!codigoEnviado) {
//            println("🔄 VerifyCodeScreen: Inicializando - Email: $email")
//            vm.configurarFlujo(tipoFlujo)
//            vm.onEmailChange(email)
//
//            // Pequeño delay para asegurar que el estado se actualice
//            delay(100)
//
//            when (tipoFlujo) {
//                FlujoVerificacion.REGISTRO -> vm.solicitarCodigoVerificacion(email)
//                FlujoVerificacion.RECUPERACION -> vm.solicitarCodigoRecuperacion(email)
//            }
//
//            codigoEnviado = true
//            println("✅ VerifyCodeScreen: Código solicitado, flag establecido")
//        }
//    }

    // 👇 Para navegación (este está bien)
//    LaunchedEffect(state.pasoActual) {
//        if (state.pasoActual == 3) {
//            println("✅ VerifyCodeScreen: Código verificado, navegando...")
//            onCodeVerified(state.email, state.codigo)
//        }
//    }



    // Efecto para navegar cuando el código se verifica
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (tipoFlujo) {
                            FlujoVerificacion.REGISTRO -> "Verificar Email"
                            FlujoVerificacion.RECUPERACION -> "Verificar Código"
                        }
                    )
                },
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
                when (tipoFlujo) {
                    FlujoVerificacion.REGISTRO -> "Verifica tu email"
                    FlujoVerificacion.RECUPERACION -> "Verifica tu código"
                },
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(8.dp))

            Text(
                when (tipoFlujo) {
                    FlujoVerificacion.REGISTRO -> "Ingresa el código de 6 dígitos que enviamos a:"
                    FlujoVerificacion.RECUPERACION -> "Ingresa el código de recuperación que enviamos a:"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(32.dp))

            // Campo de código
            OutlinedTextField(
                value = state.codigo,
                onValueChange = {
                    if (it.length <= 6) vm.onCodigoChange(it)
                },
                placeholder = { Text("123456") },
                label = { Text("Código de verificación") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

            // Botón de verificar
            Button(
                onClick = { vm.verificarCodigo() },
                enabled = !state.isLoading && state.codigo.length == 6,
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
                    Text(
                        when (tipoFlujo) {
                            FlujoVerificacion.REGISTRO -> "VERIFICAR EMAIL"
                            FlujoVerificacion.RECUPERACION -> "VERIFICAR CÓDIGO"
                        }
                    )
                }
            }
            TextButton(
                onClick = {
                    when (tipoFlujo) {
                        FlujoVerificacion.REGISTRO -> vm.solicitarCodigoVerificacion(email)
                        FlujoVerificacion.RECUPERACION -> vm.solicitarCodigoRecuperacion(email)
                    }
                },
                enabled = !state.isLoading
            ) {
                Text("Reenviar código")
            }
        }
    }
}
//    LaunchedEffect(state.pasoActual) {
//        if (state.pasoActual == 3) {
//            onCodeVerified(state.email, state.codigo)
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Verificar Código") },
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
//                "Verifica tu código",
//                style = MaterialTheme.typography.headlineMedium
//            )
//
//            Spacer(Modifier.height(8.dp))
//
//            Text(
//                "Ingresa el código de 6 dígitos que enviamos a:",
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//
//            Text(
//                email,
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.primary
//            )
//
//            Spacer(Modifier.height(32.dp))
//
//            // Campo de código
//            OutlinedTextField(
//                value = state.codigo,
//                onValueChange = {
//                    if (it.length <= 6) vm.onCodigoChange(it)
//                },
//                placeholder = { Text("123456") },
//                label = { Text("Código de verificación") },
//                singleLine = true,
//                modifier = Modifier.fillMaxWidth(),
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
//            // Botón de verificar
//            Button(
//                onClick = { vm.verificarCodigo() },
//                enabled = !state.isLoading && state.codigo.length == 6,
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
//                    Text("VERIFICAR CÓDIGO")
//                }
//            }
//        }
//    }
//}