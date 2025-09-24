package com.tesis.appmovil.ui.business

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tesis.appmovil.viewmodel.AuthViewModel
import com.tesis.appmovil.viewmodel.CategoriaViewModel
import com.tesis.appmovil.viewmodel.NegocioViewModel
import com.tesis.appmovil.viewmodel.UbicacionViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterBusinessScreen(
    authViewModel: AuthViewModel,  // ← Recibir como parámetro
    negocioViewModel: NegocioViewModel,  // ← Recibir en lugar de crear
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    // ViewModels
    //val authViewModel: AuthViewModel = viewModel()
    val categoriaViewModel: CategoriaViewModel = viewModel()
    val ubicacionViewModel: UbicacionViewModel = viewModel()
    //val negocioViewModel: NegocioViewModel = viewModel()

    // Coroutine scope para llamadas suspend
    val scope = rememberCoroutineScope()

    // Obtener el ID del usuario autenticado
    val authState by authViewModel.uiState.collectAsState()
    val idUsuario = authState.userId

    // Estados locales
    var nombreNegocio by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("") }
    var ciudadSeleccionada by remember { mutableStateOf("") }
    var distritoSeleccionada by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }

    // Estados para dropdowns
    var expandedCategoria by remember { mutableStateOf(false) }
    var expandedCiudad by remember { mutableStateOf(false) }
    var expandedDistrito by remember { mutableStateOf(false) }

    // Estado del NegocioViewModel
    val negocioState by negocioViewModel.ui.collectAsState()

    // Cargar datos al iniciar
    LaunchedEffect(Unit) {
        categoriaViewModel.cargarCategorias()
        ubicacionViewModel.cargarUbicaciones()
    }

    // Estados de los ViewModels
    val categoriaState by categoriaViewModel.uiState.collectAsState()
    val ubicacionState by ubicacionViewModel.uiState.collectAsState()

    // Filtrar distritos basados en la ciudad seleccionada
    val distritosFiltrados = remember(ciudadSeleccionada, ubicacionState.ubicaciones) {
        if (ciudadSeleccionada.isEmpty()) {
            emptyList()
        } else {
            ubicacionState.ubicaciones
                .filter { it.ciudad == ciudadSeleccionada }
                .map { it.distrito }
                .distinct()
        }
    }

    // Obtener IDs reales de las selecciones
    val categoriaSeleccionadaObj =
        categoriaState.categorias.find { it.nombre == categoriaSeleccionada }
    val ubicacionSeleccionadaObj = ubicacionState.ubicaciones.find {
        it.ciudad == ciudadSeleccionada && it.distrito == distritoSeleccionada
    }

    // Manejar errores del NegocioViewModel
    if (negocioState.error != null) {
        LaunchedEffect(negocioState.error) {
            println("Error en NegocioViewModel: ${negocioState.error}")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
            Text(
                text = "Registra tu negocio",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Spacer(Modifier.height(4.dp))

        // Subtítulo
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
            Text(
                text = "Completa los datos principales de tu negocio.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        Spacer(Modifier.height(24.dp))

        // Nombre del negocio
        OutlinedTextField(
            value = nombreNegocio,
            onValueChange = { nombreNegocio = it },
            placeholder = { Text("Ej. Spa Relax") },
            label = { Text("Nombre del negocio") },
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

        // Categoría (Dropdown)
        ExposedDropdownMenuBox(
            expanded = expandedCategoria,
            onExpandedChange = { expandedCategoria = !expandedCategoria }
        ) {
            OutlinedTextField(
                value = categoriaSeleccionada,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Elije una opción") },
                label = { Text("Categoría") },
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Desplegar categorías"
                    )
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray
                )
            )

            ExposedDropdownMenu(
                expanded = expandedCategoria,
                onDismissRequest = { expandedCategoria = false }
            ) {
                if (categoriaState.isLoading) {
                    DropdownMenuItem(
                        text = { Text("Cargando...") },
                        onClick = {}
                    )
                } else {
                    categoriaState.categorias.forEach { categoria ->
                        DropdownMenuItem(
                            text = { Text(categoria.nombre) },
                            onClick = {
                                categoriaSeleccionada = categoria.nombre
                                expandedCategoria = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Ciudad (Dropdown)
        ExposedDropdownMenuBox(
            expanded = expandedCiudad,
            onExpandedChange = { expandedCiudad = !expandedCiudad }
        ) {
            OutlinedTextField(
                value = ciudadSeleccionada,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Elije una opción") },
                label = { Text("Ciudad") },
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Desplegar ciudades"
                    )
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray
                )
            )

            ExposedDropdownMenu(
                expanded = expandedCiudad,
                onDismissRequest = { expandedCiudad = false }
            ) {
                if (ubicacionState.isLoading) {
                    DropdownMenuItem(
                        text = { Text("Cargando...") },
                        onClick = {}
                    )
                } else {
                    ubicacionState.ubicaciones
                        .map { it.ciudad }
                        .distinct()
                        .forEach { ciudad ->
                            DropdownMenuItem(
                                text = { Text(ciudad) },
                                onClick = {
                                    ciudadSeleccionada = ciudad
                                    distritoSeleccionada = "" // Reset distrito al cambiar ciudad
                                    expandedCiudad = false
                                }
                            )
                        }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Distrito (Dropdown) - Solo se habilita si hay una ciudad seleccionada
        ExposedDropdownMenuBox(
            expanded = expandedDistrito,
            onExpandedChange = {
                if (ciudadSeleccionada.isNotEmpty()) {
                    expandedDistrito = !expandedDistrito
                }
            }
        ) {
            OutlinedTextField(
                value = distritoSeleccionada,
                onValueChange = {},
                readOnly = true,
                placeholder = {
                    Text(
                        if (ciudadSeleccionada.isEmpty()) "Primero selecciona una ciudad"
                        else "Elije una opción"
                    )
                },
                label = { Text("Distrito") },
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Desplegar distritos"
                    )
                },
                enabled = ciudadSeleccionada.isNotEmpty(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (ciudadSeleccionada.isEmpty()) Color.Gray.copy(alpha = 0.5f) else Color.Gray
                )
            )

            ExposedDropdownMenu(
                expanded = expandedDistrito,
                onDismissRequest = { expandedDistrito = false }
            ) {
                distritosFiltrados.forEach { distrito ->
                    DropdownMenuItem(
                        text = { Text(distrito) },
                        onClick = {
                            distritoSeleccionada = distrito
                            expandedDistrito = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Dirección
        OutlinedTextField(
            value = direccion,
            onValueChange = { direccion = it },
            placeholder = { Text("Ej: Av. Larco 1234, Miraflores") },
            label = { Text("Dirección exacta") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray
            )
        )

        Spacer(Modifier.height(24.dp))

        // Botón continuar (habilitado solo cuando todos los campos están llenos)
        val camposCompletos = nombreNegocio.isNotEmpty() &&
                categoriaSeleccionada.isNotEmpty() &&
                ciudadSeleccionada.isNotEmpty() &&
                distritoSeleccionada.isNotEmpty() &&
                direccion.isNotEmpty()

        val isLoading = negocioState.mutando || categoriaState.isLoading || ubicacionState.isLoading

        // Mostrar ID del usuario (para debugging)
        if (idUsuario != null) {
            Text(
                text = "Usuario ID: $idUsuario",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (camposCompletos && !isLoading && idUsuario != null) {
                    scope.launch {
                        val idCategoria = categoriaSeleccionadaObj?.id_categoria
                        val idUbicacion = ubicacionSeleccionadaObj?.id_ubicacion

                        if (idCategoria != null && idUbicacion != null) {
                            val result = negocioViewModel.crearNegocioBasico(
                                nombre = nombreNegocio,
                                idCategoria = idCategoria,
                                idUbicacion = idUbicacion,
                                direccion = direccion,
                                idUsuario = idUsuario // ← Pasar el ID del usuario
                            )

                            if (result.isSuccess) {
                                val idNegocio = result.getOrNull()
                                println("=== 🚨 DEBUG CRÍTICO ===")
                                println("ID devuelto por crearNegocioBasico: $idNegocio")
                                println("Estado completo del ViewModel: ${negocioViewModel.ui.value}")
                                println("Negocio seleccionado: ${negocioViewModel.ui.value.seleccionado}")
                                println("ID del negocio seleccionado: ${negocioViewModel.ui.value.seleccionado?.id_negocio}")
                                println("=== FIN DEBUG ===")
                                onContinue() // Navegar a la siguiente pantalla
                            } else {
                                println("❌ Error al crear negocio: ${result.exceptionOrNull()?.message}")
                            }
                        }
                    }
                } else if (idUsuario == null) {
                    println("⚠️ Error: No se encontró el ID del usuario")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C1349)),
            enabled = camposCompletos && !isLoading && idUsuario != null
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
        if (categoriaState.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Error al cargar categorías: ${categoriaState.error}",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (ubicacionState.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Error al cargar ubicaciones: ${ubicacionState.error}",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (negocioState.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Error al crear negocio: ${negocioState.error}",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (idUsuario == null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Error: No se pudo obtener el ID del usuario. Vuelve a iniciar sesión.",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}