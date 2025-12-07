package com.tesis.appmovil.ui.servicios

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.joinAll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.platform.LocalContext
import com.tesis.appmovil.viewmodel.NegocioImagenViewModel
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tesis.appmovil.data.remote.dto.NegocioUpdate
import com.tesis.appmovil.data.remote.request.NegocioResponse
import com.tesis.appmovil.models.Negocio
import com.tesis.appmovil.models.NegocioImagen
import com.tesis.appmovil.ui.components.BottomNavBar
import com.tesis.appmovil.viewmodel.NegocioViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileScreen(
    negocioId: Int,
    navController: NavController?,
    vm: NegocioViewModel = viewModel(),
    imagenVm: NegocioImagenViewModel = viewModel() // 👈 AGREGA ESTE VIEWMODEL

) {
    val ui by vm.ui.collectAsState()
    val isPreview = LocalInspectionMode.current
    val imagenUi by imagenVm.ui.collectAsState() // 👈 Estado de las imágenes
    val scope = rememberCoroutineScope()
    val snackHost = remember { SnackbarHostState() }
    var headerImageUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current


    val picker = rememberLauncherForActivityResult(GetContent()) { uri: Uri? ->
        uri?.let { headerImageUrl = it.toString() }
    }

    LaunchedEffect(negocioId) {
        if (!isPreview) vm.obtenerNegocio(negocioId)
        imagenVm.cargarImagenes(negocioId) // 👈 Cargar imágenes existentes

    }
    LaunchedEffect(ui.error) {
        ui.error?.let {
            scope.launch { snackHost.showSnackbar(it); vm.limpiarError() }
        }
    }
    LaunchedEffect(ui.mutando, ui.seleccionado) {
        if (!ui.mutando && ui.seleccionado?.id_negocio == negocioId) {
            navController?.popBackStack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackHost) },
        bottomBar = {
            BottomNavBar(
                navController = navController!!,
                negocioId = negocioId,
                selected = "negocio"
            )
        }
    ) { padding ->
        when {
            ui.isLoading -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            ui.detalle == null -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { Text("No se encontró el negocio") }

            else -> {
                val r: NegocioResponse = ui.detalle!!
                if (headerImageUrl == null) {
                    headerImageUrl = r.imagenes?.firstOrNull()?.urlImagen
                }
                val negocioModel = Negocio(
                    id_negocio      = r.idNegocio,
                    id_categoria    = r.categoria.idCategoria,
                    id_ubicacion    = r.ubicacion?.idUbicacion ?: 0,
                    id_usuario      = r.idUsuario,
                    nombre          = r.nombre,
                    descripcion     = r.descripcion,
                    direccion       = r.direccion ?: "",
                    latitud         = r.latitud?.toDoubleOrNull(),
                    longitud        = r.longitud?.toDoubleOrNull(),
                    telefono        = r.telefono,
                    correo_contacto = r.correoContacto,
                    fecha_creacion  = r.fechaCreacion?.let { Date() } ?: Date(),
                    estado_auditoria= r.estadoAuditoria ?: 0,
                    // 👇 AGREGA ESTA LÍNEA
                    imagenes = r.imagenes?.map { dto ->
                        NegocioImagen(
                            id_imagen = dto.idImagen,
                            url_imagen = dto.urlImagen,
                            id_negocio = r.idNegocio,
                            descripcion = dto.descripcion,
                            fecha_subida = null, // puedes parsear dto.fechaSubida si quieres
                            estado = (dto.estado as? Int) ?: 0
                        )
                    }
                )

                BusinessProfile(
                    negocio         = negocioModel,
                    headerImageUrl  = headerImageUrl,

                    onSave = { updated ->
                        scope.launch {
                            // Filtrar solo las imágenes nuevas
                            val nuevasUris = updated.imagenes
                                ?.filter { it.id_imagen <= 0 || it.id_imagen == null }
                                ?.mapNotNull { it.url_imagen?.takeIf { uri -> uri.startsWith("content://") } }
                                ?: emptyList()

                            // Esperar a que terminen de subirse
                            if (nuevasUris.isNotEmpty()) {
                                val job = imagenVm.subirImagenes(
                                    context = context,
                                    negocioId = negocioId,
                                    uris = nuevasUris.map(Uri::parse)
                                )
                                job.join()
                            }

                            // Luego actualizar el negocio
                            vm.actualizarNegocio(
                                negocioId,
                                NegocioUpdate(
                                    idCategoria = updated.id_categoria,
                                    idUbicacion = updated.id_ubicacion,
                                    nombre = updated.nombre,
                                    descripcion = updated.descripcion,
                                    direccion = updated.direccion,
                                    latitud = updated.latitud,
                                    longitud = updated.longitud,
                                    telefono = updated.telefono,
                                    correoContacto = updated.correo_contacto,
                                    estadoAuditoria = null
                                )
                            )

                            // Finalmente refrescar
                            delay(100) // Pequeño delay para asegurar que la BD se actualizó
                            imagenVm.cargarImagenes(negocioId)
                            vm.obtenerNegocio(negocioId)
                        }
                    },
                    onHorarioClick  = { navController?.navigate("horarios/$negocioId") },
                    navToServicios  = { navController?.navigate("servicios") },
                    navToNegocio    = { navController?.navigate("businessProfile/$negocioId") },
                    navToCuenta     = { /* ... */ },
                    imagenVm        = imagenVm,
                    context         = context
                )
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfile(
    negocio: Negocio,
    headerImageUrl: String?,
    onSave: (Negocio) -> Unit,
    onHorarioClick: () -> Unit,
    navToServicios: () -> Unit,
    navToNegocio: () -> Unit,
    navToCuenta: () -> Unit,
    imagenVm: NegocioImagenViewModel,
// 👈 agregar
    context: Context
) {
    val scope = rememberCoroutineScope()  // ✅ define el scope aquí
    var nombre by remember { mutableStateOf(TextFieldValue("")) }
    var telefono by remember { mutableStateOf(TextFieldValue("")) }
    var correo by remember { mutableStateOf(TextFieldValue("")) }
    var descripcion by remember { mutableStateOf(TextFieldValue("")) }
    var direccion by remember { mutableStateOf(TextFieldValue("")) }
    val imagenesState = remember { mutableStateOf(emptyList<NegocioImagen>()) }

    LaunchedEffect(negocio) {
        nombre = TextFieldValue(negocio.nombre)
        telefono = TextFieldValue(negocio.telefono ?: "")
        correo = TextFieldValue(negocio.correo_contacto ?: "")
        descripcion = TextFieldValue(negocio.descripcion ?: "")
        direccion = TextFieldValue(negocio.direccion ?: "")
        imagenesState.value = negocio.imagenes ?: emptyList()
    }


// Estado mutable para las imágenes
    //val imagenesState = remember { mutableStateOf(negocio.imagenes ?: emptyList()) }
    val context = LocalContext.current

// Launcher de selección de imagen
    val imagenVm: NegocioImagenViewModel = viewModel()

    var imagenSeleccionadaIndex by remember { mutableStateOf<Int?>(null) }

    val picker = rememberLauncherForActivityResult(GetContent()) { uri: Uri? ->
        uri?.let { selectedUri ->
            imagenSeleccionadaIndex?.let { index ->
                val imagenActual = imagenesState.value[index]

                if (imagenActual.id_imagen > 0) {
                    // Imagen existente → reemplazar en Supabase y BD
//                    imagenVm.reemplazarImagen(
//                        context = context,
//                        idImagen = imagenActual.id_imagen,
//                        uri = selectedUri
//                    )
                    val job = imagenVm.reemplazarImagen(
                        context = context,
                        idImagen = imagenActual.id_imagen,
                        uri = selectedUri
                    )
                    scope.launch {
                        job.join() // Espera a que termine la subida
                    }

                    val nuevaImagen = imagenActual.copy(url_imagen = selectedUri.toString())
                    val nuevaLista = imagenesState.value.toMutableList()
                    nuevaLista[index] = nuevaImagen
                    imagenesState.value = nuevaLista
                } else {
                    // Imagen local (aún no subida)
                    val nuevaImagen = imagenActual.copy(url_imagen = selectedUri.toString())
                    val nuevaLista = imagenesState.value.toMutableList()
                    nuevaLista[index] = nuevaImagen
                    imagenesState.value = nuevaLista
                }
            }
        }
    }


    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.offset(y = (-56).dp),
                text = { Text("Guardar") },
                icon = { Icon(Icons.Outlined.Check, contentDescription = null) },
                onClick = {
                    onSave(
                        negocio.copy(
                            nombre = nombre.text,
                            descripcion = descripcion.text.ifBlank { null },
                            direccion = direccion.text.ifBlank { null },
                            telefono = telefono.text.ifBlank { null },
                            correo_contacto = correo.text.ifBlank { null },
                            imagenes = imagenesState.value
                        )
                    )
                    navToNegocio()
                },
                expanded = true,
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                elevation = FloatingActionButtonDefaults.elevation()
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { insets ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(insets)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Perfil del Negocio",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
            )

            // LazyRow con todas las imágenes
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(imagenesState.value.size) { index ->
                    Box {
                        AsyncImage(
                            model = imagenesState.value[index].url_imagen,
                            contentDescription = "Imagen del negocio",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillParentMaxHeight()
                                .width(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        IconButton(
                            onClick = {
                                imagenSeleccionadaIndex = index
                                picker.launch("image/*")
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .size(28.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Editar imagen",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Campos de texto
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del negocio") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = telefono,
                onValueChange = {
                    val digitsOnly = it.text.filter { char -> char.isDigit() }.take(9)
                    val newCursor = it.selection.end.coerceAtMost(digitsOnly.length)
                    telefono = it.copy(text = digitsOnly, selection = TextRange(newCursor))
                },
                label = { Text("Teléfono de contacto") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo de contacto") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción del negocio") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                minLines = 4,
                maxLines = 6
            )

            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().clickable { onHorarioClick() }
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Horarios", style = MaterialTheme.typography.titleSmall)
                    }
                    Icon(Icons.Outlined.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}



@Composable
private fun PillItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    else MaterialTheme.colorScheme.surface
    val fg = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = fg)
        Spacer(Modifier.width(8.dp))
        Text(label, color = fg, style = MaterialTheme.typography.labelMedium)
    }
}

