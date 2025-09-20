// app/src/main/java/com/tesis/appmovil/ui/home/BusinessDetailScreen.kt
package com.tesis.appmovil.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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
            // Imagen destacada
            val portada = negocio.imagenes?.firstOrNull()?.urlImagen
            AsyncImage(
                model = portada ?: "https://via.placeholder.com/900x400",
                contentDescription = negocio.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            )

            Spacer(Modifier.height(12.dp))

            // Cabecera con nombre / categoría / desc corta
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

                // Contacto + dirección (con iconitos, como el mock)
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

            // ====== Horarios (como en la pantalla de tu compañera) ======
            Text(
                text = "Horarios",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                (negocio.horarios ?: emptyList()).forEach { h ->
                    // Ej: "Lunes: 09:00:00 - 19:00:00"
                    Text(
                        text = "${h.diaSemana}: ${h.horaApertura} - ${h.horaCierre}",
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

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String?) {
    if (text.isNullOrBlank()) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun buildDireccion(dir: String?, distrito: String?, ciudad: String?): String {
    val partes = listOfNotNull(dir, distrito, ciudad)
    return if (partes.isEmpty()) "Sin dirección" else partes.joinToString(", ")
}
