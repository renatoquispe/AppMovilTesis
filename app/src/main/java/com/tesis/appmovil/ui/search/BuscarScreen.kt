package com.tesis.appmovil.ui.search

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.tesis.appmovil.viewmodel.HomeNegocioViewModel
import com.tesis.appmovil.viewmodel.ServicioViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// ---- Google Maps Compose ----
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
// -----------------------------

/** Estados de la bandeja */
private enum class SheetState { Expanded, Middle, Collapsed }

@Composable
fun BuscarScreen(
    vmNegocios: HomeNegocioViewModel,
    vmServicios: ServicioViewModel,
    onClickNegocio: (Int) -> Unit
) {
    val negociosState by vmNegocios.state.collectAsState()
    val serviciosState by vmServicios.ui.collectAsState()

    // Carga inicial
    LaunchedEffect(Unit) {
        if (!negociosState.isLoading && negociosState.negocios.isEmpty()) {
            vmNegocios.cargarDestacados(limit = 10)
        }
        if (!serviciosState.isLoading && serviciosState.servicios.isEmpty()) {
            vmServicios.cargarServicios()
        }
    }

    // Imagen por nombre de negocio (para tarjetas)
    val imageByNombre by remember(serviciosState.servicios) {
        mutableStateOf(
            serviciosState.servicios
                .groupBy { it.negocio.nombre }
                .mapValues { (_, lista) ->
                    lista.firstNotNullOfOrNull { it.negocio.imagenes?.firstOrNull()?.urlImagen }
                        ?: lista.firstNotNullOfOrNull { it.imagenUrl }
                        ?: ""
                }
        )
    }

    // --- MAPA: posición inicial ---
    val lima = LatLng(-12.0464, -77.0428)
    val firstWithCoords: LatLng? = remember(negociosState.negocios) {
        negociosState.negocios
            .firstOrNull { it.latitud != null && it.longitud != null }
            ?.let { LatLng(it.latitud!!, it.longitud!!) }
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(firstWithCoords ?: lima, 12f)
    }

    // --- SHEET: dimensiones/targets en PX ---
    val density = LocalDensity.current
    val conf = LocalConfiguration.current
    val screenHdp = conf.screenHeightDp.dp
    val screenHp = with(density) { screenHdp.toPx() }

    val topPadding = with(density) { 68.dp.toPx() }     // margen visible arriba cuando está Expanded
    val midY = screenHp * 0.45f                         // posición media
    val collapsedY = screenHp * 0.78f                   // casi abajo para ver más mapa
    val minY = topPadding
    val maxY = collapsedY

    var sheetState by remember { mutableStateOf(SheetState.Middle) }
    val offsetY = remember { Animatable(midY) }
    val scope = rememberCoroutineScope()

    // Sincroniza animación cuando cambie el estado
    LaunchedEffect(sheetState) {
        val target = when (sheetState) {
            SheetState.Expanded -> minY
            SheetState.Middle   -> midY
            SheetState.Collapsed-> maxY
        }
        offsetY.animateTo(target, spring(stiffness = Spring.StiffnessMediumLow))
    }

    Box(Modifier.fillMaxSize()) {

        // 1) Mapa a pantalla completa (la sheet va encima)
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                compassEnabled = false
            )
        ) {
            // Marcadores por negocio (si tienen lat/lng)
            negociosState.negocios.forEach { n ->
                val lat = n.latitud
                val lng = n.longitud
                if (lat != null && lng != null) {
                    Marker(
                        state = MarkerState(position = LatLng(lat, lng)),
                        title = n.nombre,
                        snippet = n.direccion ?: ""
                    )
                }
            }
        }

        // 2) Barra de búsqueda flotante
        SearchBarOverlay(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp, start = 16.dp, end = 16.dp)
        )

        // 3) Bandeja deslizable con la lista
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, offsetY.value.roundToInt()) }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dragAmount ->
                            val newY = (offsetY.value + dragAmount).coerceIn(minY, maxY)
                            scope.launch { offsetY.snapTo(newY) }
                        },
                        onDragEnd = {
                            val y = offsetY.value
                            sheetState = when {
                                y < (minY + midY) / 2f -> SheetState.Expanded
                                y < (midY + maxY) / 2f -> SheetState.Middle
                                else                   -> SheetState.Collapsed
                            }
                        }
                    )
                },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 8.dp
        ) {
            when {
                negociosState.isLoading -> {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }
                negociosState.error != null -> {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Error: ${negociosState.error}", color = MaterialTheme.colorScheme.error)
                    }
                }
                else -> {
                    Column(Modifier.fillMaxSize()) {
                        Spacer(Modifier.height(12.dp))
                        MasBuscadosSection(
                            title = "Servicios más buscados en tu zona",
                            negocios = negociosState.negocios,
                            imageByNombre = imageByNombre,
                            onClick = { negocio ->
                                // Resuelve id de navegación usando servicios
                                val idDestino = serviciosState.servicios.firstNotNullOfOrNull { s ->
                                    if (s.negocio.nombre == negocio.nombre)
                                        s.idNegocio.takeIf { it > 0 } ?: s.negocio.idNegocio
                                    else null
                                } ?: 0
                                if (idDestino > 0) onClickNegocio(idDestino)
                            }
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBarOverlay(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .height(52.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Menu, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Busque el mejor servicio aquí",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(Icons.Outlined.Search, contentDescription = null)
        }
    }
}
