//package com.tesis.appmovil.ui.home
package com.tesis.appmovil.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
            val negocio = state.detalle!!
            RenderBusinessDetail(negocio, onBack)
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
                title = { Text(negocio.nombre) },
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
            val imagen = negocio.imagenes?.firstOrNull()?.urlImagen
            AsyncImage(
                model = imagen ?: "https://via.placeholder.com/600",
                contentDescription = negocio.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Info básica
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = negocio.nombre,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = negocio.categoria.nombre,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                negocio.descripcion?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(Modifier.height(12.dp))

                // Ubicación
                Text(
                    text = "📍 ${negocio.direccion}, ${negocio.ubicacion?.distrito}, ${negocio.ubicacion?.ciudad}",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Contacto
                negocio.telefono?.let {
                    Text("☎ $it", style = MaterialTheme.typography.bodyMedium)
                }
                negocio.correoContacto?.let {
                    Text("✉ $it", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Servicios
            Text(
                text = "Servicios",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            (negocio.servicios ?: emptyList()).forEach { servicio ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = servicio.imagenUrl ?: "https://via.placeholder.com/80",
                            contentDescription = servicio.nombre,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(servicio.nombre, fontWeight = FontWeight.Bold)
                            Text("Duración: ${servicio.duracionMinutos} min")
                        }
                        Text("S/ ${servicio.precio}", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Horarios
            Text(
                text = "Horarios",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            (negocio.horarios ?: emptyList()).forEach {
                Text(
                    "${it.diaSemana}: ${it.horaApertura} - ${it.horaCierre}",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Galería de imágenes adicionales
            if ((negocio.imagenes?.size ?: 0) > 1) {
                Text(
                    text = "Galería",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    negocio.imagenes?.drop(1)?.forEach { img ->
                        AsyncImage(
                            model = img.urlImagen,
                            contentDescription = img.descripcion ?: "",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
            }
        }
    }
}
