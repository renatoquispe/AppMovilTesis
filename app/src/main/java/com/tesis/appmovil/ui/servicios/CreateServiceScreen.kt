package com.tesis.appmovil.ui.servicios

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    // 👇 Agregar para ocultar navbar
    LaunchedEffect(Unit) {
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.set("hide_navbar", true)
    }

    DisposableEffect(Unit) {
        onDispose {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("hide_navbar", false)
        }
    }

    val ui by vm.ui.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var duracion by remember { mutableStateOf("") }
    var descuento by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var createdOk by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(GetContent()) { uri ->
        imageUri = uri
        errorMsg = null
    }

    // 👇 Cálculo del precio con descuento
    val precioConDescuento = remember(descuento, precio) {
        calcularPrecioConDescuento(precio, descuento)
    }

    // 👇 Navegación cuando se crea exitosamente
    LaunchedEffect(createdOk, ui.mutando, ui.error) {
        if (createdOk && !ui.mutando && ui.error == null) {
            // Avisar a ServiciosScreen que recargue
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("refresh_servicios", true)

            // Volver atrás
            navController.popBackStack()
            createdOk = false
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nuevo servicio") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Selector de imagen
            Text("Imagen", style = MaterialTheme.typography.titleMedium)
            OutlinedButton(
                onClick = { launcher.launch("image/*") },
                enabled = !ui.mutando
            ) {
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
                onValueChange = { nombre = it; errorMsg = null },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !ui.mutando
            )

            OutlinedTextField(
                value = precio,
                onValueChange = {
                    if (it.matches(Regex("^\\d*\\.?\\d*\$"))) {
                        precio = it
                        errorMsg = null
                    }
                },
                label = { Text("Precio (S/)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = !ui.mutando
            )

            OutlinedTextField(
                value = duracion,
                onValueChange = {
                    if (it.matches(Regex("^\\d*\$"))) {
                        duracion = it
                        errorMsg = null
                    }
                },
                label = { Text("Duración (min)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = !ui.mutando
            )

            // 👇 CAMBIO: Campo de descuento SOLO números enteros (0-70)
            OutlinedTextField(
                value = descuento,
                onValueChange = {
                    // Solo permite números enteros (0-70) o vacío
                    // Permite desde 0 para que el usuario pueda escribir 10, 20, etc.
                    if (it.matches(Regex("^\\d*\$")) && (it.isEmpty() || (it.toInt() in 0..70))) {
                        descuento = it
                        errorMsg = null
                    }
                },
                label = { Text("Descuento (%)") },
                placeholder = { Text("0-70% (máx 70%)") }, // 👈 Cambiado
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = !ui.mutando,
                suffix = {
                    if (descuento.isNotBlank()) Text("%")
                }
            )

            // 👇 Mostrar cálculo del descuento (solo si descuento > 0)
            if (descuento.isNotBlank() && descuento.toIntOrNull() ?: 0 > 0 &&
                precio.isNotBlank() && precio.toDoubleOrNull() != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "💰 Precio con descuento:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        val precioOriginal = precio.toDouble()
                        val precioFinal = precioConDescuento
                        val ahorro = precioOriginal - precioFinal

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Precio original:",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "S/ ${String.format("%.2f", precioOriginal)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Descuento:",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "$descuento%",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Precio final:",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                "S/ ${String.format("%.2f", precioFinal)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        if (ahorro > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Ahorro:",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "S/ ${String.format("%.2f", ahorro)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Mostrar errores de validación
            errorMsg?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.weight(1f))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(
                    onClick = { navController.popBackStack() },
                    enabled = !ui.mutando
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = {
                        // Validaciones
                        when {
                            nombre.isBlank() -> {
                                errorMsg = "El nombre es obligatorio"
                                return@Button
                            }
                            precio.isBlank() -> {
                                errorMsg = "El precio es obligatorio"
                                return@Button
                            }
                            duracion.isBlank() -> {
                                errorMsg = "La duración es obligatoria"
                                return@Button
                            }
                            precio.toDoubleOrNull() == null -> {
                                errorMsg = "Precio inválido"
                                return@Button
                            }
                            duracion.toIntOrNull() == null -> {
                                errorMsg = "Duración inválida"
                                return@Button
                            }
                            descuento.isNotBlank() && descuento.toIntOrNull() == null -> {
                                errorMsg = "Descuento debe ser un número entero"
                                return@Button
                            }
                            // 👇 CAMBIO: Validación 0-70%
                            descuento.isNotBlank() && descuento.toInt() !in 0..70 -> {
                                errorMsg = "El descuento debe estar entre 0% y 70%"
                                return@Button
                            }
                        }

                        // 👇 Convertir descuento para BD (entero → decimal)
                        val descuentoParaBD = if (descuento.isNotBlank() && descuento.toInt() > 0) {
                            descuento.toInt() / 100.0  // 20 → 0.20
                        } else {
                            null
                        }

                        val dto = ServicioCreate(
                            idNegocio = negocioId,
                            nombre = nombre.trim(),
                            precio = precio.toDouble(),
                            duracionMinutos = duracion.toInt(),
                            descuento = descuentoParaBD, // 👈 Ya convertido a 0.20
                            imagenUrl = null
                        )

                        // Llamar al ViewModel dentro de una corrutina
                        scope.launch {
                            try {
                                // Convertir la imagen a Multipart si existe
                                val part = imageUri?.let { uri ->
                                    try {
                                        uri.toMultipart(context, "imagen")
                                    } catch (e: Exception) {
                                        Log.e("CreateService", "Error convirtiendo imagen", e)
                                        errorMsg = "Error procesando imagen: ${e.message}"
                                        null
                                    }
                                }

                                // Si hubo error con la imagen, no continuar
                                if (imageUri != null && part == null) {
                                    return@launch
                                }

                                // Llamar al ViewModel
                                vm.crearYSubirImagen(dto, part) { servicioCreado ->
                                    createdOk = true
                                }
                            } catch (e: Exception) {
                                errorMsg = "Error: ${e.message}"
                                Log.e("CreateService", "Error general", e)
                            }
                        }
                    },
                    enabled = !ui.mutando
                ) {
                    if (ui.mutando) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text("Crear servicio")
                    }
                }
            }

            // 👇 Espacio extra al final para mejor scroll
            Spacer(Modifier.height(16.dp))

            // Mostrar error del ViewModel si existe
            ui.error?.let { err ->
                Spacer(Modifier.height(8.dp))
                Text(text = "Error: $err", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// 👇 FUNCIÓN AUXILIAR
private fun calcularPrecioConDescuento(precio: String, descuento: String): Double {
    val precioNum = precio.toDoubleOrNull() ?: 0.0
    if (descuento.isBlank() || descuento.toIntOrNull() ?: 0 == 0) return precioNum

    // Descuento ya es entero (20), lo convertimos a decimal (0.20)
    val descuentoDecimal = descuento.toInt() / 100.0
    return precioNum * (1 - descuentoDecimal)
}
