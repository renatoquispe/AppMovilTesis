package com.tesis.appmovil.ui.business

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.tesis.appmovil.R
import com.tesis.appmovil.ui.Dest
import com.tesis.appmovil.viewmodel.AuthViewModel
import com.tesis.appmovil.viewmodel.NegocioViewModel
import kotlinx.coroutines.launch

@Composable
fun BusinessReadyScreen(
    negocioViewModel: NegocioViewModel,
    authViewModel: AuthViewModel, // 👈 Recibir AuthViewModel
    navController: NavController, // 👈 Recibir navController directamente
    onPublish: () -> Unit = {} // 👈 Hacerlo opcional
) {
    val uiState = negocioViewModel.ui.collectAsState()
    val context = LocalContext.current
    val isMutando by remember { derivedStateOf { uiState.value.mutando } }

    LaunchedEffect(uiState.value.error) {
        uiState.value.error?.let { error ->
            println("❌ Error en BusinessReadyScreen: $error")
        }
    }
    // Observar si el registro se completó exitosamente
    LaunchedEffect(uiState.value.registroCompletado) {
        if (uiState.value.registroCompletado) {
            // Obtener el ID del negocio directamente del ViewModel
            val negocioId = negocioViewModel.obtenerIdNegocioEnRegistro()

            if (negocioId != null) {
                println("🚀 Navegando a servicios con negocioId: $negocioId")
                // Navegar a ServiciosScreen con el ID correcto usando la ruta con parámetro
                navController.navigate("servicios/$negocioId") {
                    popUpTo(Dest.Home.route) { inclusive = true }
                    launchSingleTop = true
                }
            } else {
                println("❌ Error: negocioId es null en BusinessReadyScreen")
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 👇 Asegúrate de tener tu ilustración en res/drawable con este nombre
        Image(
            painter = painterResource(id = R.drawable.business_ready),
            contentDescription = "Negocio listo",
            modifier = Modifier
                .size(150.dp)
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Tu negocio está listo",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = {
                // Obtener el ID del negocio creado y cambiar estado de auditoría
                val negocioId = negocioViewModel.obtenerIdNegocioEnRegistro()
                if (negocioId != null) {
                    negocioViewModel.viewModelScope.launch {
                        val resultado = negocioViewModel.completarRegistro(negocioId)
                        if (resultado.isSuccess) {
                            // Éxito: mostrar mensaje (la navegación se hará automáticamente por el LaunchedEffect)
                            Toast.makeText(
                                context,
                                "¡Negocio publicado exitosamente!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // Error: mostrar mensaje
                            Toast.makeText(
                                context,
                                "Error al publicar negocio: ${resultado.exceptionOrNull()?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Error: No se encontró el ID del negocio",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            enabled = !isMutando
        ) {
            if (isMutando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "PUBLICAR NEGOCIO",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

    }
}
