package com.tesis.appmovil.ui.home

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.navigation.NavController
import com.tesis.appmovil.ChatActivity
import com.tesis.appmovil.models.Servicio
import com.tesis.appmovil.viewmodel.ServicioViewModel

@Composable
fun HomeScreen(vm: ServicioViewModel, navController: NavController? = null) {
    val state by vm.ui.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { vm.cargarServicios() }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Ayuda") },
                icon = { Icon(Icons.Outlined.SmartToy, contentDescription = "Asistente") },
                // >>> AQUÍ: abre el chatbot. Usa la ruta Compose "chatbot" si existe;
                // si navController es null, como fallback abre la ChatActivity (WebView).
                onClick = { navController?.navigate("chatbot") ?: ChatActivity.start(context) },
                shape = RoundedCornerShape(16.dp)
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.error}", color = Color.Red)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { HeaderGreeting(name = "Invitado", location = "Lima, Perú") }

                    item { SectionTitle("Servicios disponibles") }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(
                                state.servicios,
                                key = { it.idServicio } // clave estable por servicio
                            ) { servicio ->
                                SmallServiceCard(
                                    servicio = servicio,
                                    onClick = {
                                        // Navegación SEGURA
                                        val idDestino = (servicio.idNegocio.takeIf { id -> id > 0 }
                                            ?: servicio.negocio.idNegocio)
                                        if (idDestino > 0) {
                                            navController?.navigate("businessDetail/$idDestino")
                                        }
                                    }
                                )
                            }
                        }
                    }

                    item { SectionTitle("Estilos cerca de ti") }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(
                                state.servicios.take(4),
                                key = { it.idServicio }
                            ) { servicio ->
                                FeaturedCard(
                                    servicio = servicio,
                                    onClick = {
                                        val idDestino = (servicio.idNegocio.takeIf { id -> id > 0 }
                                            ?: servicio.negocio.idNegocio)
                                        if (idDestino > 0) {
                                            navController?.navigate("businessDetail/$idDestino")
                                        }
                                    }
                                )
                            }
                        }
                    }

                    item { SectionTitle("Servicios destacados en tu zona") }
                    item {
                        // Agrupación por idNegocio REAL (evita 0)
                        val negociosUnicos = state.servicios
                            .groupBy { it.idNegocio.takeIf { id -> id > 0 } ?: it.negocio.idNegocio }
                            .map { (_, lista) -> lista.first() }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(
                                negociosUnicos.take(3),
                                key = { it.idNegocio.takeIf { id -> id > 0 } ?: it.negocio.idNegocio }
                            ) { servicio ->
                                DealRowCard(
                                    servicio = servicio,
                                    onClick = {
                                        val idDestino = (servicio.idNegocio.takeIf { id -> id > 0 }
                                            ?: servicio.negocio.idNegocio)
                                        if (idDestino > 0) {
                                            navController?.navigate("businessDetail/$idDestino")
                                        }
                                    }
                                )
                            }
                        }
                    }

                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun HeaderGreeting(name: String, location: String) {
    Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(2.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "¿Qué harás hoy?",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Spacer(Modifier.width(4.dp))
                Text(location, style = MaterialTheme.typography.labelSmall)
                Icon(Icons.Outlined.LocationOn, contentDescription = null)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
}

@Composable
private fun SmallServiceCard(servicio: Servicio, onClick: () -> Unit = {}) {
    val purple = MaterialTheme.colorScheme.primary
    val onPurple = MaterialTheme.colorScheme.onPrimary
    val cardWidth = 360.dp
    val cardHeight = 160.dp

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight)
            .clickable(onClick = onClick)
    ) {
        Row(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                    .background(purple)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.98f)
                    ) {
                        Text(
                            text = "¡Tiempo limitado!",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = purple,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = servicio.nombre,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "Hasta un",
                            color = Color.White.copy(alpha = 0.95f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "${servicio.descuento?.toInt() ?: 0}%",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = servicio.negocio.nombre,
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = servicio.imagenUrl,
                    contentDescription = servicio.nombre,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 12.dp),
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 4.dp,
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Obtener oferta",
                            style = MaterialTheme.typography.labelSmall,
                            color = purple,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(purple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ArrowForward,
                                contentDescription = null,
                                tint = onPurple,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedCard(servicio: Servicio, onClick: () -> Unit = {}) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val horizontalPadding = 12.dp * 2
    val spacing = 8.dp
    val cardWidth = (screenWidth - horizontalPadding - spacing) / 2

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(cardWidth)
            .clickable(onClick = onClick)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                AsyncImage(
                    model = servicio.imagenUrl,
                    contentDescription = servicio.nombre,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.66f)
                                )
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = servicio.nombre,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = servicio.negocio.direccion ?: "Sin dirección",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "S/ ${servicio.precio}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DealRowCard(servicio: Servicio, onClick: () -> Unit = {}) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val horizontalPadding = 12.dp * 2
    val spacing = 8.dp
    val cardWidth = (screenWidth - horizontalPadding - spacing) / 2

    val primeraImagenNegocio = servicio.negocio.imagenes?.firstOrNull()?.urlImagen
    val categoriaNombre = servicio.negocio.categoria?.nombre ?: "Sin categoría"
    val imagenMostrar = primeraImagenNegocio ?: servicio.imagenUrl.orEmpty()

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(cardWidth)
            .clickable(onClick = onClick)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = imagenMostrar,
                    contentDescription = servicio.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Column(Modifier.padding(12.dp)) {
                Text(
                    servicio.negocio.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    servicio.negocio.direccion ?: "Sin dirección",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.outlineVariant
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            text = categoriaNombre,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }
            }
        }
    }
}
