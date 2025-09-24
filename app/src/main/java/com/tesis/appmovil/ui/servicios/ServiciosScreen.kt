package com.tesis.appmovil.ui.servicios

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tesis.appmovil.models.Servicio
import com.tesis.appmovil.viewmodel.ServicioViewModel
import kotlinx.coroutines.launch

@Composable
fun ServiciosScreen(
    vm: ServicioViewModel,
    navController: NavController? = null,
    negocioId: Int = 1,
    onAdd: () -> Unit = {}
) {
    val state by vm.ui.collectAsState()
    val isPreview = LocalInspectionMode.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // ⬇️ Escucha la señal para refrescar al volver desde Edit/Create
    val refreshSignal = navController?.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("refresh_servicios", false)
        ?.collectAsState()

    LaunchedEffect(refreshSignal?.value) {
        if (refreshSignal?.value == true) {
            vm.cargarServicios(negocioId)
            navController?.currentBackStackEntry
                ?.savedStateHandle
                ?.set("refresh_servicios", false)
        }
    }

    // Carga inicial
    LaunchedEffect(negocioId, isPreview) {
        if (!isPreview) vm.cargarServicios(negocioId)
    }

    // Snackbar de errores
    LaunchedEffect(state.error) {
        state.error?.let { msg ->
            scope.launch {
                snackbarHostState.showSnackbar(msg)
                vm.limpiarError()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PillItem(Icons.Outlined.Home, "Servicios", true) { /* ya aquí */ }
                    PillItem(Icons.Outlined.Store, "Negocio", false) { navController?.navigate("negocio") }
                    PillItem(Icons.Outlined.Person, "Cuenta", false) { navController?.navigate("cuenta") }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Agregar") },
                icon = { Icon(Icons.Outlined.Add, contentDescription = "Agregar") },
                onClick = onAdd,
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Tus servicios",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(Modifier.height(8.dp))
                }

                items(state.servicios) { servicio ->
                    ServiceListItemWithActions(
                        servicio = servicio,
                        onEdit = { navController?.navigate("editService/${servicio.idServicio}") },
                        onDeleteConfirmed = { id -> vm.eliminarServicio(id) },
                        onClick = { navController?.navigate("serviceDetail/${servicio.idServicio}") }
                    )
                }

                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }
}

@Composable
private fun ServiceListItemWithActions(
    servicio: Servicio,
    onEdit: () -> Unit,
    onDeleteConfirmed: (Int) -> Unit,
    onClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar servicio") },
            text = { Text("¿Eliminar \"${servicio.nombre}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteConfirmed(servicio.idServicio)
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = servicio.imagenUrl.orEmpty(),
            contentDescription = servicio.nombre,
            modifier = Modifier
                .size(68.dp)
                .background(Color.LightGray, RoundedCornerShape(12.dp))
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                servicio.nombre,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(Modifier.height(4.dp))

            val precioInt = servicio.precio.toDoubleOrNull()?.toInt() ?: 0
            Text("S/ $precioInt", style = MaterialTheme.typography.bodySmall)

            val dur = servicio.duracionMinutos?.toString() ?: "0"
            Text("$dur min", style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.width(12.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            SmallIconButton(
                onClick = onEdit,
                container = Color(0xFFFFD54F),
                content = Color(0xFF4E342E),
                icon = Icons.Outlined.Edit,
                contentDescription = "Editar"
            )
            SmallIconButton(
                onClick = { showDeleteDialog = true },
                container = Color(0xFFEF5350),
                content = Color.White,
                icon = Icons.Outlined.Delete,
                contentDescription = "Eliminar"
            )
        }
    }
}

@Composable
private fun SmallIconButton(
    onClick: () -> Unit,
    container: Color,
    content: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier.height(36.dp),
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
    ) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun PillItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
    val fg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .background(bg, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = fg)
        Spacer(Modifier.width(8.dp))
        Text(label, color = fg, style = MaterialTheme.typography.labelMedium)
    }
}
