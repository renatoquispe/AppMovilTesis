// BusinessProfileScreen + BusinessProfile
package com.tesis.appmovil.ui.servicios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tesis.appmovil.data.remote.dto.NegocioUpdate
import com.tesis.appmovil.data.remote.request.NegocioResponse
import com.tesis.appmovil.models.Negocio
import com.tesis.appmovil.viewmodel.NegocioViewModel
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileScreen(
    negocioId: Int,
    navController: NavController? = null,
    vm: NegocioViewModel = viewModel()
) {
    val ui by vm.ui.collectAsState()
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Cargar el negocio cuando entra a la pantalla
    LaunchedEffect(negocioId) {
        if (!isPreview) vm.obtenerNegocio(negocioId)
    }

    // Mostrar errores en snackbar
    LaunchedEffect(ui.error) {
        ui.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                vm.limpiarError()
            }
        }
    }

    // Volver atrás cuando termine de actualizar y el seleccionado coincida
    LaunchedEffect(ui.mutando, ui.seleccionado) {
        if (!ui.mutando && ui.seleccionado?.id_negocio == negocioId) {
            navController?.popBackStack()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { padding ->
        when {
            ui.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            ui.detalle == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) { Text("No se encontró el negocio") }
            }

            else -> {
                val negocioResp: NegocioResponse = ui.detalle!!

                // Mapear NegocioResponse -> Negocio (modelo de UI)
                val negocioModel = Negocio(
                    id_negocio = negocioResp.idNegocio ?: 0,
                    id_categoria = negocioResp.categoria?.idCategoria ?: 0,
                    id_ubicacion = negocioResp.ubicacion?.idUbicacion ?: 0,
                    id_usuario = negocioResp.idUsuario,
                    nombre = negocioResp.nombre ?: "",
                    descripcion = negocioResp.descripcion,
                    direccion = negocioResp.direccion?:"",
                    latitud = negocioResp.latitud?.toDoubleOrNull(),
                    longitud = negocioResp.longitud?.toDoubleOrNull(),
                    telefono = negocioResp.telefono,
                    correo_contacto = negocioResp.correoContacto,
                    fecha_creacion = negocioResp.fechaCreacion?.let { Date() } ?: Date(),
                    estado_auditoria = negocioResp.estadoAuditoria ?: 0
                )

                BusinessProfile(
                    negocio = negocioModel,
                    onSave = { updatedNegocioModel: Negocio ->
                        val update = NegocioUpdate(
                            id_categoria = updatedNegocioModel.id_categoria,
                            id_ubicacion = updatedNegocioModel.id_ubicacion,
                            nombre = updatedNegocioModel.nombre,
                            descripcion = updatedNegocioModel.descripcion,
                            direccion = updatedNegocioModel.direccion,
                            latitud = updatedNegocioModel.latitud,
                            longitud = updatedNegocioModel.longitud,
                            telefono = updatedNegocioModel.telefono,
                            correoContacto = updatedNegocioModel.correo_contacto,
                            estado_auditoria = null
                        )
                        vm.actualizarNegocio(negocioId, update)
                    },
                    onHorarioClick = { /* abrir pantalla de horarios si la tienes */ },
                    navToServicios = { navController?.navigate("servicios") },
                    navToNegocio = { /* ... */ },
                    navToCuenta = { /* ... */ }
                )
            }
        }
    }
}

@Composable
fun BusinessProfile(
    negocio: Negocio,
    onSave: (Negocio) -> Unit,
    onHorarioClick: () -> Unit,
    navToServicios: () -> Unit,
    navToNegocio: () -> Unit,
    navToCuenta: () -> Unit
) {
    var nombre by remember { mutableStateOf(TextFieldValue(negocio.nombre)) }
    var descripcion by remember { mutableStateOf(TextFieldValue(negocio.descripcion ?: "")) }
    var direccion by remember { mutableStateOf(TextFieldValue(negocio.direccion ?: "")) }
    var telefono by remember { mutableStateOf(TextFieldValue(negocio.telefono ?: "")) }
    var correo by remember { mutableStateOf(TextFieldValue(negocio.correo_contacto ?: "")) }
    var latitud by remember { mutableStateOf(negocio.latitud?.toString() ?: "") }
    var longitud by remember { mutableStateOf(negocio.longitud?.toString() ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Perfil del Negocio", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = nombre, onValueChange = { nombre = it },
            label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = descripcion, onValueChange = { descripcion = it },
            label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = direccion, onValueChange = { direccion = it },
            label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = telefono, onValueChange = { telefono = it },
            label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = correo, onValueChange = { correo = it },
            label = { Text("Correo de contacto") }, modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = latitud, onValueChange = { latitud = it },
                label = { Text("Latitud") }, modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = longitud, onValueChange = { longitud = it },
                label = { Text("Longitud") }, modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                val updated = negocio.copy(
                    nombre = nombre.text,
                    descripcion = descripcion.text.ifBlank { null },
                    direccion = direccion.text.ifBlank { null },
                    telefono = telefono.text.ifBlank { null },
                    correo_contacto = correo.text.ifBlank { null },
                    latitud = latitud.toDoubleOrNull(),
                    longitud = longitud.toDoubleOrNull()
                )
                onSave(updated)
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Guardar cambios") }

        OutlinedButton(
            onClick = onHorarioClick,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Gestionar horarios") }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = navToServicios, modifier = Modifier.weight(1f)) {
                Text("Servicios")
            }
            OutlinedButton(onClick = navToNegocio, modifier = Modifier.weight(1f)) {
                Text("Negocio")
            }
            OutlinedButton(onClick = navToCuenta, modifier = Modifier.weight(1f)) {
                Text("Cuenta")
            }
        }
    }
}
