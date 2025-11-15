package com.tesis.appmovil.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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

// Lottie (FAB animado)
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.tesis.appmovil.R

// Ubicación
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun HomeScreen(vm: ServicioViewModel, navController: NavController? = null) {
    val state by vm.ui.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Etiqueta que se muestra en el chip de ubicación
    var locationLabel by remember { mutableStateOf("Lima, Perú") }
    // Controla la animación de refresco del chip
    var locRefreshing by remember { mutableStateOf(false) }

    // Pide permiso y, si lo obtiene, resuelve distrito/ciudad
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        scope.launch {
            if (granted) {
                locRefreshing = true
                try {
                    val coords = getCurrentOrLastLocation(context)
                    if (coords != null) {
                        locationLabel = reverseGeocodeName(context, coords.first, coords.second)
                    }
                } finally {
                    locRefreshing = false
                }
            } else {
                // si niega, detenemos animación
                locRefreshing = false
            }
        }
    }

    // Pide servicios y ubicación al entrar
    LaunchedEffect(Unit) {
        vm.cargarServicios()
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController?.navigate("chatbot")
                        ?: context.startActivity(Intent(context, ChatActivity::class.java))
                },
                shape = RoundedCornerShape(18.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                )
            ) {
                val comp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.bellabot))
                val progress by animateLottieCompositionAsState(
                    composition = comp,
                    iterations = LottieConstants.IterateForever
                )
                LottieAnimation(
                    composition = comp,
                    progress = { progress },
                    modifier = Modifier.size(64.dp)
                )
            }
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
                        .statusBarsPadding()               // más aire respecto a la barra de estado
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 6.dp) // un pelín más de espacio arriba
                ) {
                    item {
                        HeaderGreeting(
                            name = "Invitado",
                            location = locationLabel,
                            isRefreshing = locRefreshing,
                            onLocationClick = {
                                // Al tocar el chip, lanzamos permiso/refresh y activamos animación de inmediato
                                locRefreshing = true
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        )
                    }
                    item { SectionTitle("Ofertas especiales") }
                    item {
                        if (state.ofertas.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                                itemsIndexed(state.ofertas.take(5), key = { _, servicio -> servicio.idServicio }) { index, servicio ->
                                    SmallServiceCard(
                                        servicio = servicio,
                                        index = index, // 👈 se pasa el índice
                                        onClick = {
                                            val idDestino = (servicio.idNegocio.takeIf { id -> id > 0 }
                                                ?: servicio.negocio.idNegocio)
                                            if (idDestino > 0) {
                                                navController?.navigate("businessDetail/$idDestino")
                                            }
                                        }
                                    )
                                }

//                                items(state.ofertas.take(5), key = { it.idServicio }) { servicio ->
//                                    SmallServiceCard(
//                                        servicio = servicio,
//                                        onClick = {
//                                            val idDestino = (servicio.idNegocio.takeIf { id -> id > 0 }
//                                                ?: servicio.negocio.idNegocio)
//                                            if (idDestino > 0) {
//                                                navController?.navigate("businessDetail/$idDestino")
//                                            }
//                                        }
//                                    )
//                                }
                            }
                        } else {
                            // Mensaje cuando no hay ofertas
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No hay ofertas especiales en este momento",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }


                    item { SectionTitle("Servicios cerca de ti") }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.servicios.take(4), key = { it.idServicio }) { servicio ->
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

                    item { SectionTitle("Negocios destacados en tu zona") }
                    item {
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HeaderGreeting(
    name: String,
    location: String,
    isRefreshing: Boolean,
    onLocationClick: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp) // separa título + chip del borde superior
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // TÍTULO MÁS CHICO
            Text(
                text = "¿Qué harás hoy?",
                fontSize = 20.sp, // antes headlineSmall, ahora más pequeño
                fontWeight = FontWeight.Bold
            )

            // Chip clickeable + ripple
            val screenMax = (LocalConfiguration.current.screenWidthDp * 0.58f).dp
            val infinite = rememberInfiniteTransition(label = "loc-rot")
            val rotation by remember(isRefreshing) {
                mutableStateOf(isRefreshing)
            }.let {
                if (isRefreshing) {
                    infinite.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(900, easing = LinearEasing)
                        ),
                        label = "rot"
                    )
                } else {
                    mutableStateOf(0f)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onLocationClick() } // <- click con ripple por defecto
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .heightIn(min = 40.dp)
                    .widthIn(max = screenMax)
            ) {
                Text(
                    text = location,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .basicMarquee(
                            iterations = Int.MAX_VALUE,
                            delayMillis = 1300,
                            velocity = 35.dp
                        )
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = "Ubicación",
                    modifier = Modifier.rotate(if (isRefreshing) rotation else 0f)
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
}

@Composable
//private fun SmallServiceCard(servicio: Servicio, onClick: () -> Unit = {}) {
private fun SmallServiceCard(servicio: Servicio, index: Int = 0, onClick: () -> Unit = {}) {
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary
    )
    val purple = colors[index % colors.size]
    val onPurple = MaterialTheme.colorScheme.onPrimary

    val cardWidth = 360.dp
    val cardHeight = 160.dp
    val descuentoPorcentaje = servicio.getDescuentoPorcentaje()

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight)
            .clickable(onClick = onClick)
    ) {
        Row(Modifier.fillMaxSize()) {
            // Lado izquierdo: texto
            Column(
                modifier = Modifier
//                    .weight(1f)
                    .weight(0.5f) // 60% del ancho total para la columna de texto
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
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis // <- agrega esto

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
                            text = "${descuentoPorcentaje}%",
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

            // Lado derecho: imagen + botón encima
            Box(
                modifier = Modifier
                    .weight(0.5f) // 40% del ancho total para la imagen
//                    .width(190.dp)
                    .fillMaxHeight()
                    .background(purple), // fondo del card
                contentAlignment = Alignment.BottomEnd
            ) {
                // Imagen con lado izquierdo circular
                AsyncImage(
                    model = servicio.imagenUrl,
                    contentDescription = servicio.nombre,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(
                            RoundedCornerShape(
                                topStart = 80.dp,
                                bottomStart = 80.dp,
                                topEnd = 0.dp,
                                bottomEnd = 0.dp
                            )
                        ),
                    contentScale = ContentScale.Crop
                )

                // Botón “Obtener oferta” encima de la imagen
                Surface(
                    modifier = Modifier
                        .padding(end = 16.dp, bottom = 12.dp),
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 4.dp,
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
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
    the@ run {
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

/* ------------------ Helpers de ubicación ------------------ */

@SuppressLint("MissingPermission")
suspend fun getCurrentOrLastLocation(context: android.content.Context): Pair<Double, Double>? = try {
    val client = LocationServices.getFusedLocationProviderClient(context)
    val cts = CancellationTokenSource()
    val current = client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token).await()
    if (current != null) Pair(current.latitude, current.longitude)
    else client.lastLocation.await()?.let { Pair(it.latitude, it.longitude) }
} catch (_: Exception) { null }

/** Devuelve distrito / ciudad legible desde lat/lng. */
suspend fun reverseGeocodeName(
    context: android.content.Context,
    lat: Double,
    lng: Double
): String = withContext(Dispatchers.IO) {
    runCatching {
        val geocoder = Geocoder(context, Locale.getDefault())
        val list = geocoder.getFromLocation(lat, lng, 1)
        val addr = list?.firstOrNull()
        val district = addr?.subLocality
        val city = addr?.locality ?: addr?.subAdminArea
        val country = addr?.countryName
        when {
            !district.isNullOrBlank() && !city.isNullOrBlank() -> "$district, $city"
            !city.isNullOrBlank() -> city
            !country.isNullOrBlank() -> country
            else -> "Ubicación actual"
        }
    }.getOrDefault("Ubicación actual")
}
