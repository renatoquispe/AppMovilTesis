package com.tesis.appmovil.ui.servicios

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tesis.appmovil.models.Servicio
import com.tesis.appmovil.viewmodel.ServicioViewModel

import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.tesis.appmovil.ui.theme.AppMovilTesisTheme

@Composable
fun ServiciosScreen(
    vm: ServicioViewModel,
    navController: NavController? = null,
    onEdit: (Servicio) -> Unit = {},
    onDelete: (Servicio) -> Unit = {},
    onAdd: () -> Unit = {}
) {
    val state by vm.ui.collectAsState()

    LaunchedEffect(Unit) { vm.cargarServicios() }

    Scaffold(
        bottomBar = {
            BottomPillBar(
                selected = BottomTab.Servicios,
                onSelect = { tab ->
                    when (tab) {
                        BottomTab.Servicios -> { /* ya aquí */ }
                        BottomTab.Negocio   -> navController?.navigate("negocio")
                        BottomTab.Cuenta    -> navController?.navigate("cuenta")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surface,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) { Icon(Icons.Outlined.Add, contentDescription = "Agregar") }
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

            state.error != null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { Text("Error: ${state.error}", color = Color.Red) }

            else -> {
                LazyColumn(
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
                        ServiceListItem(
                            servicio = servicio,
                            onEdit = { onEdit(servicio) },
                            onDelete = { onDelete(servicio) },
                            onClick = { navController?.navigate("serviceDetail/${servicio.idServicio}") }
                        )
                    }

                    item { Spacer(Modifier.height(72.dp)) } // espacio para la bottom bar
                }
            }
        }
    }
}

@Composable
private fun ServiceListItem(
    servicio: Servicio,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    // Colores estilo mock
    val yellow = Color(0xFFFFD54F)   // Amber 300
    val yellowOn = Color(0xFF4E342E) // Marrón para contraste
    val red = Color(0xFFEF5350)      // Red 400
    val redOn = Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Imagen izquierda
        AsyncImage(
            model = servicio.imagenUrl ?: "",
            contentDescription = servicio.nombre,
            modifier = Modifier
                .size(68.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(Modifier.width(12.dp))

        // Texto
        Column(modifier = Modifier.weight(1f)) {
            Text(
                servicio.nombre,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(Modifier.height(4.dp))

            val precio = servicio.precio
            val duracion = servicio.duracionMinutos ?: 0
            val descuento = servicio.descuento ?: 0.0

            MetaRow(label = "Precio:", value = "S/ ${precio.toInt()}")
            MetaRow(label = "Duración:", value = "${duracion}min")
            MetaRow(label = "Descuento:", value = if (descuento > 0) "${descuento.toInt()}%" else "No hay")
        }

        Spacer(Modifier.width(12.dp))

        // Botones Editar / Eliminar
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            SmallIconButton(
                onClick = onEdit,
                container = yellow,
                content = yellowOn,
                icon = Icons.Outlined.Edit,
                contentDescription = "Editar"
            )
            SmallIconButton(
                onClick = onDelete,
                container = red,
                content = redOn,
                icon = Icons.Outlined.Delete,
                contentDescription = "Eliminar"
            )
        }
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
        Spacer(Modifier.width(6.dp))
        Text(text = value, style = MaterialTheme.typography.bodySmall)
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
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
    ) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(18.dp))
    }
}

private enum class BottomTab { Servicios, Negocio, Cuenta }

@Composable
private fun BottomPillBar(
    selected: BottomTab,
    onSelect: (BottomTab) -> Unit
) {
    Surface(shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PillItem(
                icon = Icons.Outlined.Home,
                label = "Servicios",
                selected = selected == BottomTab.Servicios,
                onClick = { onSelect(BottomTab.Servicios) }
            )
            PillItem(
                icon = Icons.Outlined.Store,
                label = "Negocio",
                selected = selected == BottomTab.Negocio,
                onClick = { onSelect(BottomTab.Negocio) }
            )
            PillItem(
                icon = Icons.Outlined.Person,
                label = "Cuenta",
                selected = selected == BottomTab.Cuenta,
                onClick = { onSelect(BottomTab.Cuenta) }
            )
        }
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
//
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun PreviewServiciosScreen() {
    AppMovilTesisTheme {
        ServiciosScreen(
            vm = ServicioViewModel(),            // si tu VM tiene constructor sin args
            navController = rememberNavController(),
            onEdit = {},
            onDelete = {},
            onAdd = {}
        )
    }
}