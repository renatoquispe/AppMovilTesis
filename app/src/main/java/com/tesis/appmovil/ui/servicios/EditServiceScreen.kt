package com.tesis.appmovil.ui.servicios

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tesis.appmovil.data.remote.dto.ServicioUpdate
import com.tesis.appmovil.models.Servicio
import com.tesis.appmovil.utils.toMultipart
import com.tesis.appmovil.viewmodel.ServicioViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditServiceScreen(
    servicioId: Int,
    vm: ServicioViewModel,
    navController: NavController
) {
    // Cuando entres a esta pantalla, envía señal para ocultar navbar
    LaunchedEffect(Unit) {
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.set("hide_navbar", true)
    }

    // Cuando salgas de esta pantalla, envía señal para mostrar navbar
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
    val focus = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // ------- State -------
    var nombre by rememberSaveable { mutableStateOf("") }
    var precioText by rememberSaveable { mutableStateOf("") }
    var duracionText by rememberSaveable { mutableStateOf("") }
    var descuentoText by rememberSaveable { mutableStateOf("") }

    var imagenUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var pickedImage by remember { mutableStateOf<Uri?>(null) }
    var removePhoto by rememberSaveable { mutableStateOf(false) }

    var errorMsg by rememberSaveable { mutableStateOf<String?>(null) }
    var hydrated by rememberSaveable { mutableStateOf(false) }
    var updatedOk by rememberSaveable { mutableStateOf(false) }

    // ------- Cargar detalle -------
    LaunchedEffect(servicioId) { vm.obtenerServicio(servicioId) }

    LaunchedEffect(ui.seleccionado) {
        if (!hydrated) {
            (ui.seleccionado as? Servicio)?.let { s ->
                nombre = s.nombre
                precioText = s.precio.toString()
                duracionText = s.duracionMinutos.toString()
//                descuentoText = s.descuento?.toString() ?: ""
                descuentoText = s.descuento?.toDouble()?.let { (it * 100).toInt().toString() } ?: ""

                imagenUrl = s.imagenUrl
                hydrated = true
            }
        }
    }

    // Selector de imagen
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        errorMsg = null
        if (uri != null) {
            pickedImage = uri
            removePhoto = false
        }
    }

    // ------- Navegación al terminar -------
    LaunchedEffect(updatedOk, ui.mutando, ui.error) {
        if (updatedOk && !ui.mutando && ui.error == null) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("refresh_servicios", true)

            val popped = navController.popBackStack()
            if (!popped) {
                navController.navigate("servicios") {
                    popUpTo(0)
                    launchSingleTop = true
                }
            }
            updatedOk = false
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Editar servicio") }) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (ui.cargando && !hydrated) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Text("Imagen", style = MaterialTheme.typography.titleMedium)
            AsyncImage(
                model = when {
                    pickedImage != null -> pickedImage
                    removePhoto -> null
                    else -> imagenUrl
                },
                contentDescription = "Imagen del servicio",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { pickImageLauncher.launch("image/*") },
                    enabled = !ui.mutando
                ) { Text("Cambiar foto") }

                OutlinedButton(
                    onClick = {
                        if (imagenUrl != null) {
                            // Eliminar imagen de Supabase
                            scope.launch {
                                try {
                                    vm.eliminarImagenServicio(servicioId)
                                    pickedImage = null
                                    removePhoto = true
                                    imagenUrl = null
                                    errorMsg = null
                                } catch (e: Exception) {
                                    errorMsg = "Error al eliminar imagen: ${e.message}"
                                }
                            }
                        } else {
                            pickedImage = null
                            removePhoto = true
                            errorMsg = null
                        }
                    },
                    enabled = (!ui.mutando && (pickedImage != null || imagenUrl != null))
                ) { Text("Eliminar foto") }
            }

            Divider()

            // 👇 AQUÍ ESTÁN LOS CAMPOS DE TEXTO QUE FALTABAN
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it; errorMsg = null },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !ui.mutando
            )
            OutlinedTextField(
                value = precioText,
                onValueChange = { txt ->
                    if (txt.isEmpty() || txt.matches(Regex("""\d*\.?\d{0,2}"""))) {
                        precioText = txt
                        errorMsg = null
                    }
                },
                label = { Text("Precio") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !ui.mutando
            )
            OutlinedTextField(
                value = duracionText,
                onValueChange = { txt ->
                    if (txt.isEmpty() || txt.matches(Regex("""\d{0,4}"""))) {
                        duracionText = txt
                        errorMsg = null
                    }
                },
                label = { Text("Duración (min)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !ui.mutando
            )
//            OutlinedTextField(
//                value = descuentoText,
//                onValueChange = { txt ->
//                    if (txt.isEmpty() || txt.matches(Regex("""\d{0,3}(\.\d{0,2})?"""))) {
//                        descuentoText = txt
//                        errorMsg = null
//                    }
//                },
//                label = { Text("Descuento (%)") },
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
//                modifier = Modifier.fillMaxWidth(),
//                singleLine = true,
//                enabled = !ui.mutando
//            )
            // 👇 CAMPO DE DESCUENTO (con validación y rango 0–70)
            OutlinedTextField(
                value = descuentoText,
                onValueChange = {
                    // Solo permite números enteros (0–70)
                    if (it.matches(Regex("^\\d*\$")) && (it.isEmpty() || (it.toInt() in 0..70))) {
                        descuentoText = it
                        errorMsg = null
                    }
                },
                label = { Text("Descuento (%)") },
                placeholder = { Text("0-70% (máx 70%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = !ui.mutando,
                suffix = {
                    if (descuentoText.isNotBlank()) Text("%")
                }
            )

// 👇 Mostrar cálculo del descuento (solo si hay precio y descuento válidos)
            if (descuentoText.isNotBlank() && descuentoText.toIntOrNull() ?: 0 > 0 &&
                precioText.isNotBlank() && precioText.toDoubleOrNull() != null) {

                val precioConDescuento = calcularPrecioConDescuento(precioText, descuentoText)
                val precioOriginal = precioText.toDouble()
                val ahorro = precioOriginal - precioConDescuento

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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Precio original:", style = MaterialTheme.typography.bodySmall)
                            Text("S/ ${String.format("%.2f", precioOriginal)}", style = MaterialTheme.typography.bodySmall)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Descuento:", style = MaterialTheme.typography.bodySmall)
                            Text("$descuentoText%", style = MaterialTheme.typography.bodySmall)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Precio final:",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "S/ ${String.format("%.2f", precioConDescuento)}",
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
                                Text("Ahorro:", style = MaterialTheme.typography.bodySmall)
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
                        focus.clearFocus()
                        errorMsg = null

                        val precio = precioText.toDoubleOrNull()
                        val duracion = duracionText.toIntOrNull()
//                        val descuento = descuentoText.toDoubleOrNull()
                        val descuento = descuentoText.toDoubleOrNull()?.div(100)


                        when {
                            nombre.isBlank() -> {
                                errorMsg = "El nombre es obligatorio"
                                return@Button
                            }
                            precio == null -> {
                                errorMsg = "Precio inválido"
                                return@Button
                            }
                            duracion == null || duracion <= 0 -> {
                                errorMsg = "Duración inválida"
                                return@Button
                            }
                            descuentoText.isNotBlank() &&
                                    (descuento == null || descuento < 0.0 || descuento > 100.0) -> {
                                errorMsg = "Descuento inválido (0 a 100)"
                                return@Button
                            }
                        }

                        scope.launch {
                            try {
                                // 1. Primero actualizar los datos básicos del servicio
                                val dto = ServicioUpdate(
                                    nombre = nombre.trim(),
                                    precio = precio,
                                    duracionMinutos = duracion,
                                    descuento = descuento,
                                    imagenUrl = if (removePhoto) null else imagenUrl
                                )

                                vm.actualizarServicio(
                                    id = servicioId,
                                    body = dto,
                                    onSuccess = { servicioActualizado ->
                                        // 2. Si hay una nueva imagen seleccionada, subirla
                                        if (pickedImage != null && !removePhoto) {
                                            scope.launch {
                                                try {
                                                    val part = pickedImage?.toMultipart(context, "imagen")
                                                    if (part != null) {
                                                        vm.subirImagenServicio(servicioId, part)
                                                    }
                                                    updatedOk = true
                                                } catch (e: Exception) {
                                                    errorMsg = "Error subiendo imagen: ${e.message}"
                                                }
                                            }
                                        } else {
                                            updatedOk = true
                                        }
                                    },
                                    onError = { msg -> errorMsg = msg }
                                )
                            } catch (e: Exception) {
                                errorMsg = "Error al actualizar: ${e.message}"
                            }
                        }
                    },
                    enabled = !ui.mutando
                ) {
                    if (ui.mutando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp).padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    Text("Guardar")
                }
            }
        }
    }
}
// 👇 FUNCIÓN AUXILIAR (puedes copiarla desde CreateServiceScreen)
private fun calcularPrecioConDescuento(precio: String, descuento: String): Double {
    val precioNum = precio.toDoubleOrNull() ?: 0.0
    if (descuento.isBlank() || descuento.toIntOrNull() ?: 0 == 0) return precioNum
    val descuentoDecimal = descuento.toInt() / 100.0
    return precioNum * (1 - descuentoDecimal)
}
