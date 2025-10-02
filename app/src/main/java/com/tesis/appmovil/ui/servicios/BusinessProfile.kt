package com.tesis.appmovil.ui.servicios

import android.net.Uri
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
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
import com.tesis.appmovil.viewmodel.NegocioViewModel
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileScreen(
    negocioId: Int,
    navController: NavController?,
    vm: NegocioViewModel = viewModel()
) {
    val ui by vm.ui.collectAsState()
    val isPreview = LocalInspectionMode.current
    val scope = rememberCoroutineScope()
    val snackHost = remember { SnackbarHostState() }
    var headerImageUrl by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(GetContent()) { uri: Uri? ->
        uri?.let { headerImageUrl = it.toString() }
    }

    LaunchedEffect(negocioId) {
        if (!isPreview) vm.obtenerNegocio(negocioId)
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
            Surface(shadowElevation = 8.dp) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PillItem(Icons.Outlined.Home,  "Servicios", false) {
                        navController?.navigate("servicios")
                    }
                    PillItem(Icons.Outlined.Store, "Negocio",   true ) {
                        navController?.navigate("businessProfile/$negocioId")
                    }
                    PillItem(Icons.Filled.Person,  "Cuenta",    false) {
                        /* nav to cuenta */
                    }
                }
            }
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
                    estado_auditoria= r.estadoAuditoria ?: 0
                )

                BusinessProfile(
                    negocio         = negocioModel,
                    headerImageUrl  = headerImageUrl,
                    onSave          = { updated ->
                        vm.actualizarNegocio(
                            negocioId,
                            NegocioUpdate(
                                id_categoria    = updated.id_categoria,
                                id_ubicacion    = updated.id_ubicacion,
                                nombre          = updated.nombre,
                                descripcion     = updated.descripcion,
                                direccion       = updated.direccion,
                                latitud         = updated.latitud,
                                longitud        = updated.longitud,
                                telefono        = updated.telefono,
                                correoContacto  = updated.correo_contacto,
                                estado_auditoria= null
                            )
                        )
                    },
                    onHorarioClick  = { navController?.navigate("horarios/$negocioId") },
                    onChangeImage   = { picker.launch("image/*") },
                    navToServicios  = { navController?.navigate("servicios") },
                    navToNegocio    = { navController?.navigate("businessProfile/$negocioId") },
                    navToCuenta     = { /* ... */ }
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
    onChangeImage: () -> Unit,
    navToServicios: () -> Unit,
    navToNegocio: () -> Unit,
    navToCuenta: () -> Unit
) {
    var nombre      by remember { mutableStateOf(TextFieldValue(negocio.nombre)) }
    var telefono    by remember { mutableStateOf(TextFieldValue(negocio.telefono ?: "")) }
    var correo      by remember { mutableStateOf(TextFieldValue(negocio.correo_contacto ?: "")) }
    var descripcion by remember { mutableStateOf(TextFieldValue(negocio.descripcion ?: "")) }
    var direccion   by remember { mutableStateOf(TextFieldValue(negocio.direccion ?: "")) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.offset(y = (-56).dp),
                text    = { Text("Guardar") },
                icon    = { Icon(Icons.Outlined.Check, contentDescription = null) },
                onClick = {
                    onSave(
                        negocio.copy(
                            nombre          = nombre.text,
                            descripcion     = descripcion.text.ifBlank { null },
                            direccion       = direccion.text.ifBlank { null },
                            telefono        = telefono.text.ifBlank { null },
                            correo_contacto = correo.text.ifBlank { null }
                        )
                    )
                },
                expanded       = true,
                shape          = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor   = MaterialTheme.colorScheme.onSurface,
                elevation      = FloatingActionButtonDefaults.elevation()
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
            Text(
                negocio.nombre,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box {
                ElevatedCard(
                    shape    = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    AsyncImage(
                        model           = headerImageUrl ?: "https://picsum.photos/900/600",
                        contentDescription = "Foto del negocio",
                        contentScale    = ContentScale.Crop,
                        modifier        = Modifier.fillMaxSize()
                    )
                }
                IconButton(
                    onClick  = onChangeImage,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .size(32.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Cambiar foto",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            OutlinedTextField(
                value         = nombre,
                onValueChange = { nombre = it },
                label         = { Text("Nombre del negocio") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = telefono,
                onValueChange = { newValue ->
                    val digitsOnly = newValue.text.filter { it.isDigit() }.take(9)
                    telefono = TextFieldValue(digitsOnly)
                },
                label = { Text("Teléfono de contacto") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value         = correo,
                onValueChange = { correo = it },
                label         = { Text("Correo de contacto") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value         = descripcion,
                onValueChange = { descripcion = it },
                label         = { Text("Descripción del negocio") },
                modifier      = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                minLines      = 4,
                maxLines      = 6
            )

            OutlinedTextField(
                value         = direccion,
                onValueChange = { direccion = it },
                label         = { Text("Dirección") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )

            ElevatedCard(
                shape    = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHorarioClick() }
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment   = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Horarios", style = MaterialTheme.typography.titleSmall)
                    }
                    Icon(
                        Icons.Outlined.ArrowForwardIos,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BusinessProfilePreview() {
    val demo = Negocio(
        id_negocio      = 1,
        id_categoria    = 1,
        id_ubicacion    = 1,
        id_usuario      = 1,
        nombre          = "Demo Shop",
        descripcion     = "Descripción demo",
        direccion       = "Av. Ejemplo 123",
        latitud         = null,
        longitud        = null,
        telefono        = "000000000",
        correo_contacto = "demo@ejemplo.com",
        fecha_creacion  = Date(),
        estado_auditoria= 1
    )
    BusinessProfile(
        negocio         = demo,
        headerImageUrl  = null,
        onSave          = {},
        onHorarioClick  = {},
        onChangeImage   = {},
        navToServicios  = {},
        navToNegocio    = {},
        navToCuenta     = {}
    )
}
