package com.tesis.appmovil.ui.servicios

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tesis.appmovil.data.remote.dto.ServicioCreate
import com.tesis.appmovil.utils.toMultipart
import com.tesis.appmovil.viewmodel.ServicioViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateServiceScreen(
    negocioId: Int,
    vm: ServicioViewModel = viewModel(),
    navController: NavController
) {
    val ui by vm.ui.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var duracion by remember { mutableStateOf("") }
    var descuento by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(GetContent()) { uri ->
        imageUri = uri
    }

    val refreshSignal = navController?.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("refresh_servicios", false)
        ?.collectAsState()

    LaunchedEffect(refreshSignal?.value) {
        if (refreshSignal?.value == true) {
            vm.cargarServicios(negocioId) // recarga
            navController?.currentBackStackEntry
                ?.savedStateHandle
                ?.set("refresh_servicios", false) // consumir señal
        }
    }


    // Navegar atrás cuando termine de crear+y subir y no haya error
    LaunchedEffect(ui.mutando, ui.error) {
        if (!ui.mutando && ui.error == null && ui.servicios.any { it.nombre == nombre }) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nuevo servicio") }) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Selector de imagen
            OutlinedButton(onClick = { launcher.launch("image/*") }) {
                Text("Seleccionar imagen", fontSize = 14.sp)
            }
            imageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            // Campos de texto
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = precio,
                onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d*\$"))) precio = it },
                label = { Text("Precio") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = duracion,
                onValueChange = { if (it.matches(Regex("^\\d*\$"))) duracion = it },
                label = { Text("Duración (min)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = descuento,
                onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d*\$"))) descuento = it },
                label = { Text("Descuento (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.weight(1f))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Cancelar")
                }
                Button(onClick = {
                    val dto = ServicioCreate(
                        idNegocio = negocioId,
                        nombre = nombre,
                        precio = precio.toDoubleOrNull() ?: 0.0,
                        duracionMinutos = duracion.toIntOrNull(),
                        descuento = descuento.toDoubleOrNull(),
                        imagenUrl = null
                    )
                    scope.launch {
                        try {
                            val part = imageUri?.toMultipart(context, "imagen")
                            vm.crearYSubirImagen(dto, part)
                        } catch (e: Exception) {
                            Log.e("CreateService", "Error creando/subiendo", e)
                        }
                    }
                }) {
                    if (ui.mutando) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text("Guardar")
                    }
                }
            }

            // Mostrar error si existe
            ui.error?.let { err ->
                Spacer(Modifier.height(8.dp))
                Text(text = "¡Error! $err", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
