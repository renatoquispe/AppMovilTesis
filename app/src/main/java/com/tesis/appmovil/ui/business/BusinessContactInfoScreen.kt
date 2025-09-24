
package com.tesis.appmovil.ui.business

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tesis.appmovil.viewmodel.NegocioViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessContactInfoScreen(
    negocioViewModel: NegocioViewModel,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // Obtener el ID del negocio creado en la pantalla anterior
    val negocioState by negocioViewModel.ui.collectAsState()
    val idNegocio = negocioState.negocioCreadoId

    // DEBUG
    println("🔍 BusinessContactInfoScreen - ID del negocio: $idNegocio")
    println("🔍 Estado completo: $negocioState")

    // Estados locales
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    // Estado de carga
    val isLoading = negocioState.mutando

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Flecha atrás
        IconButton(onClick = { onBack() }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Atrás"
            )
        }

        Spacer(Modifier.height(8.dp))

        // Título
        Text(
            text = "Información de contacto",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(4.dp))

        // Subtítulo
        Text(
            text = "Agrega formas de comunicación para tus clientes.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(Modifier.height(24.dp))

        // Teléfono
        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            placeholder = { Text("Ej: 987654321") },
            label = { Text("Teléfono de contacto") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(Modifier.height(12.dp))

        // Correo
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            placeholder = { Text("Ej: contacto@sparelax.com") },
            label = { Text("Correo de contacto") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray
            )
        )

        Spacer(Modifier.height(12.dp))

        // Descripción
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            placeholder = { Text("Ofrecemos masajes de relajación y terapias naturales para tu bienestar.") },
            label = { Text("Descripción del negocio") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray
            )
        )

        // Mostrar ID del negocio (debug)
        if (idNegocio != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Actualizando negocio ID: $idNegocio",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(24.dp))

        // Botón continuar
        Button(
            onClick = {
                if (idNegocio != null) {
                    scope.launch {
                        val result = negocioViewModel.actualizarContacto(
                            idNegocio = idNegocio,
                            telefono = telefono,
                            correo = correo,
                            descripcion = descripcion
                        )

                        if (result.isSuccess) {
                            println("✅ Información de contacto actualizada para negocio $idNegocio")
                            onContinue() // Navegar a la siguiente pantalla
                        } else {
                            println("❌ Error al actualizar contacto: ${result.exceptionOrNull()?.message}")
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C1349)),
            enabled = idNegocio != null && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("CONTINUAR →")
            }
        }

        // Mostrar errores si los hay
        if (negocioState.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Error: ${negocioState.error}",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (idNegocio == null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Error: No se encontró el ID del negocio. Vuelve a la pantalla anterior.",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}