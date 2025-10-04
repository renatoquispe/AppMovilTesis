// app/src/main/java/com/tesis/appmovil/ui/home/BusinessDetailScreen.kt

// app/src/main/java/com/tesis/appmovil/ui/home/BusinessDetailScreen.kt
package com.tesis.appmovil.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.tesis.appmovil.data.remote.request.NegocioResponse
import com.tesis.appmovil.viewmodel.NegocioViewModel

@Composable
fun BusinessDetailScreen(
    idNegocio: Int,
    vm: NegocioViewModel,
    onBack: () -> Unit
) {
    val state by vm.ui.collectAsState()

    LaunchedEffect(idNegocio) {
        vm.obtenerNegocio(idNegocio)
    }

    when {
        state.isLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        state.error != null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
            }
        }
        state.detalle != null -> {
            RenderBusinessDetail(state.detalle!!, onBack)
        }
        else -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Negocio no encontrado")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RenderBusinessDetail(
    negocio: NegocioResponse,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(negocio.nombre, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Carrusel de imágenes mejorado
            BusinessImagesCarousel(
                imagenes = negocio.imagenes?.map { it.urlImagen ?: "https://via.placeholder.com/900x400" }
                    ?: emptyList(),
                negocioNombre = negocio.nombre
            )

            Spacer(Modifier.height(16.dp))

            // Resto del contenido...
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = negocio.nombre,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = negocio.categoria?.nombre ?: "—",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                negocio.descripcion?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(Modifier.height(12.dp))

                // Contacto + dirección
                InfoRow(
                    icon = Icons.Filled.LocationOn,
                    text = buildDireccion(
                        negocio.direccion,
                        negocio.ubicacion?.distrito,
                        negocio.ubicacion?.ciudad
                    )
                )
                negocio.telefono?.let {
                    InfoRow(icon = Icons.Filled.Phone, text = it)
                }
                negocio.correoContacto?.let {
                    InfoRow(icon = Icons.Filled.Email, text = it)
                }
            }

            Spacer(Modifier.height(20.dp))

            // ====== Servicios ======
            Text(
                text = "Servicios",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            (negocio.servicios ?: emptyList()).forEach { servicio ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = servicio.imagenUrl ?: "https://via.placeholder.com/88",
                            contentDescription = servicio.nombre,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                servicio.nombre,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Duración: ${servicio.duracionMinutos} min",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "S/ ${servicio.precio}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ====== Horarios ======
            Text(
                text = "Horarios",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                (negocio.horarios ?: emptyList()).forEach { h ->
                    Text(
                        text = "${h.diaSemana}: ${h.getHorarioCompleto()}",

//                        text = "${h.diaSemana}: ${h.horaApertura} - ${h.horaCierre}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BusinessImagesCarousel(imagenes: List<String>, negocioNombre: String) {
    // Si no hay imágenes, mostrar placeholder
    if (imagenes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clip(RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No hay imágenes disponibles",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { imagenes.size })

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                SubcomposeAsyncImage(
                    model = imagenes[page],
                    contentDescription = "$negocioNombre - Imagen ${page + 1}",
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Error al cargar imagen")
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Indicador de página actual - optimizado para 2 imágenes
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1}/2", // Fijo en 2 porque es el máximo
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Indicadores de puntos - optimizado para 2 imágenes
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Solo mostramos puntos para las imágenes que existen
            repeat(imagenes.size) { index ->
                val isSelected = pagerState.currentPage == index

                val size by animateDpAsState(
                    targetValue = if (isSelected) 12.dp else 8.dp,
                    label = "dotSize"
                )
                val color by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    label = "dotColor"
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp) // Más espacio entre puntos
                        .size(size)
                        .clip(RoundedCornerShape(50))
                        .background(color)
                )
            }
        }

    }
}


@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String?) {
    if (text.isNullOrBlank()) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun buildDireccion(dir: String?, distrito: String?, ciudad: String?): String {
    val partes = listOfNotNull(dir, distrito, ciudad)
    return if (partes.isEmpty()) "Sin dirección" else partes.joinToString(", ")
}

