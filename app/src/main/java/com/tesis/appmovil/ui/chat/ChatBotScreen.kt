package com.tesis.appmovil.ui.chat

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Money
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.tesis.appmovil.R
import com.tesis.appmovil.data.remote.RetrofitClient
import com.tesis.appmovil.data.remote.ServicioFilterRequest
import com.tesis.appmovil.models.Categoria
import com.tesis.appmovil.models.Servicio
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class Step { GREETING, LOCATION_CHOICE, LOCATION_INPUT, CATEGORY, BUDGET, RESULTS }

data class LocalCategory(val id: Int, val nombre: String)
data class LocalService(
    val id: Int,
    val nombre: String,
    val precio: Double,
    val negocioNombre: String?,
    val direccion: String?,
    val distanceKm: Double? = null
)

// ---- helpers para campos variables (DTO/Model) ----
private fun tryGetInt(obj: Any, names: List<String>): Int? =
    names.firstNotNullOfOrNull { n ->
        runCatching {
            val f = obj::class.java.getDeclaredField(n)
            f.isAccessible = true
            (f.get(obj) as? Number)?.toInt()
        }.getOrNull()
    }

private fun tryGetString(obj: Any, names: List<String>): String? =
    names.firstNotNullOfOrNull { n ->
        runCatching {
            val f = obj::class.java.getDeclaredField(n)
            f.isAccessible = true
            f.get(obj) as? String
        }.getOrNull()
    }

private fun catIdOf(any: Any?): Int? = when (any) {
    null -> null
    is com.tesis.appmovil.models.Categoria -> any.id_categoria
    else -> tryGetInt(any, listOf("id_categoria", "idCategoria", "id"))
}
private fun catNameOf(any: Any?): String? = when (any) {
    null -> null
    is com.tesis.appmovil.models.Categoria -> any.nombre
    else -> tryGetString(any, listOf("nombre", "name"))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBotScreen() {
    val api = RetrofitClient.api
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    var step by remember { mutableStateOf(Step.GREETING) }

    var useCurrentLocation by remember { mutableStateOf<Boolean?>(null) }
    var typedAddress by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    var categories by remember { mutableStateOf<List<LocalCategory>>(emptyList()) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var loadingCats by remember { mutableStateOf(false) }

    var priceMinText by remember { mutableStateOf("") }
    var priceMaxText by remember { mutableStateOf("") }

    var results by remember { mutableStateOf<List<LocalService>>(emptyList()) }
    var loadingResults by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // ----------- data -----------
    suspend fun fetchCategoriasApi(): List<Categoria> = try {
        val resp = api.getCategorias()
        if (!resp.isSuccessful) emptyList()
        else when (val body = resp.body()) {
            is com.tesis.appmovil.data.remote.ApiResponse<*> ->
                (body.data as? List<*>)?.filterIsInstance<Categoria>() ?: emptyList()
            is List<*> -> body.filterIsInstance<Categoria>()
            else -> emptyList()
        }
    } catch (_: Exception) { emptyList() }

    suspend fun fallbackCategoriasDesdeServicios(): List<LocalCategory> {
        return try {
            val sResp = api.getServicios()
            if (!sResp.isSuccessful) {
                emptyList()
            } else {
                val data = sResp.body()?.data ?: emptyList<Servicio>()
                val mapa = LinkedHashMap<Int, String>()
                data.forEach { s ->
                    val id = catIdOf(s.negocio?.categoria)
                    val nom = catNameOf(s.negocio?.categoria)
                    if (id != null && !nom.isNullOrBlank()) mapa.putIfAbsent(id, nom)
                }
                mapa.map { (id, nombre) -> LocalCategory(id, nombre) }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun fetchCategoriesOnce(): List<LocalCategory> {
        loadingCats = true
        return try {
            val cats = fetchCategoriasApi()
            if (cats.isNotEmpty()) cats.map { LocalCategory(it.id_categoria, it.nombre) }
            else fallbackCategoriasDesdeServicios()
        } catch (_: Exception) {
            errorMsg = "No se pudieron cargar categorías."
            emptyList()
        } finally { loadingCats = false }
    }

    // ----------- permiso ubicación -----------
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                scope.launch {
                    val loc = getCurrentOrLastLocation(ctx)
                    if (loc != null) {
                        latitude = loc.first
                        longitude = loc.second
                        Log.d("ChatBot", "Ubicación actual: $latitude,$longitude")
                        Toast.makeText(ctx, "Ubicación: $latitude, $longitude", Toast.LENGTH_SHORT).show()
                    } else {
                        errorMsg = "No se pudo obtener ubicación. Escribe tu dirección."
                    }
                    step = Step.CATEGORY
                }
            } else {
                errorMsg = "Permiso denegado. Por favor escribe la ubicación."
                step = Step.LOCATION_INPUT
            }
        }
    )

    // ----------- UI -----------
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Avatar animado en el TopBar
                        BellaIcon(sizeDp = 28.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = when (step) {
                                Step.GREETING -> "BellaBot"
                                Step.LOCATION_CHOICE -> "Ubicación"
                                Step.LOCATION_INPUT -> "Escribe tu zona"
                                Step.CATEGORY -> "Categoría"
                                Step.BUDGET -> "Presupuesto"
                                Step.RESULTS -> "Resultados"
                            }
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            (ctx as? ComponentActivity)
                                ?.onBackPressedDispatcher?.onBackPressed()
                                ?: (ctx as? Activity)?.finish()
                        }
                    ) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Atrás") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            when (step) {
                Step.GREETING -> {
                    BotBubble(
                        icon = { BellaIcon() },
                        text = "Hola 👋 soy BellaBot. ¿Quieres empezar?"
                    )
                    Spacer(Modifier.height(12.dp))
                    PrimaryButton("Sí, vamos", Icons.Outlined.Send) { step = Step.LOCATION_CHOICE }
                }

                Step.LOCATION_CHOICE -> {
                    BotBubble(
                        icon = { Icon(Icons.Outlined.LocationOn, null) },
                        text = "¿Deseas usar tu ubicación actual o escribir otra?"
                    )
                    Spacer(Modifier.height(12.dp))
                    Column {
                        SelectorRow(
                            selected = useCurrentLocation == true,
                            icon = Icons.Outlined.MyLocation,
                            label = "Usar mi ubicación actual",
                        ) {
                            useCurrentLocation = true
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                        SelectorRow(
                            selected = useCurrentLocation == false,
                            icon = Icons.Outlined.LocationOn,
                            label = "Escribir otra ubicación",
                        ) {
                            useCurrentLocation = false
                            step = Step.LOCATION_INPUT
                        }
                    }
                }

                Step.LOCATION_INPUT -> {
                    BotBubble(
                        icon = { Icon(Icons.Outlined.LocationOn, null) },
                        text = "Escribe tu distrito/dirección 🧭"
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = typedAddress,
                        onValueChange = { typedAddress = it },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Outlined.LocationOn, null) },
                        placeholder = { Text("Ej. Av. Colonial 123, Bellavista") }
                    )
                    Spacer(Modifier.height(12.dp))
                    PrimaryButton("Continuar", Icons.Outlined.Send) { step = Step.CATEGORY }
                }

                Step.CATEGORY -> {
                    LaunchedEffect(Unit) {
                        if (categories.isEmpty()) categories = fetchCategoriesOnce()
                    }
                    BotBubble(
                        icon = { Icon(Icons.Outlined.Category, null) },
                        text = "¿Qué servicio buscas? Elige una categoría 🧩"
                    )
                    Spacer(Modifier.height(10.dp))
                    if (loadingCats) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        if (categories.isEmpty()) {
                            AssistantHint("No se pudieron cargar categorías. Puedes continuar sin elegir una.")
                        }
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(categories) { c ->
                                SelectorRow(
                                    selected = selectedCategoryId == c.id,
                                    icon = Icons.Outlined.Category,
                                    label = c.nombre
                                ) { selectedCategoryId = c.id }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        PrimaryButton("Siguiente", Icons.Outlined.Send) { step = Step.BUDGET }
                    }
                }

                Step.BUDGET -> {
                    BotBubble(
                        icon = { Icon(Icons.Outlined.Money, null) },
                        text = "Define tu presupuesto (opcional) 💸"
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = priceMinText,
                            onValueChange = { priceMinText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text("Desde") },
                            leadingIcon = { Icon(Icons.Outlined.Money, null) },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = priceMaxText,
                            onValueChange = { priceMaxText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text("Hasta") },
                            leadingIcon = { Icon(Icons.Outlined.Money, null) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    PrimaryButton("Buscar servicios", Icons.Outlined.Send) {
                        loadingResults = true
                        errorMsg = null
                        scope.launch {
                            try {
                                // Si el usuario eligió ubicación actual enviamos coords; si escribió dirección, no enviamos coords
                                val (lat, lng) = if (useCurrentLocation == true) {
                                    latitude to longitude
                                } else null to null

                                val req = ServicioFilterRequest(
                                    servicio = null,
                                    ubicacion = if (useCurrentLocation == false) typedAddress else null,
                                    precioMin = priceMinText.ifBlank { null }?.toDoubleOrNull(),
                                    precioMax = priceMaxText.ifBlank { null }?.toDoubleOrNull(),
                                    categoryId = selectedCategoryId,
                                    latitude = lat,
                                    longitude = lng,
                                    // subí un poco el radio para asegurar resultados
                                    radiusKm = 3.0
                                )

                                val resp = api.filterServicios(req)
                                if (resp.isSuccessful) {
                                    val body = resp.body()
                                    val raw = (body?.data as? List<*>) ?: emptyList<Any?>()
                                    val list = mutableListOf<LocalService>()
                                    raw.forEach { item ->
                                        when (item) {
                                            is Map<*, *> -> {
                                                val id = (item["idServicio"] as? Number)?.toInt()
                                                    ?: (item["id_servicio"] as? Number)?.toInt()
                                                    ?: 0
                                                val nombre = (item["nombre"] as? String) ?: ""
                                                val precio = (item["precio"] as? Number)?.toDouble() ?: 0.0
                                                val negocioMap = item["negocio"] as? Map<*, *>
                                                val negocioNombre = negocioMap?.get("nombre") as? String
                                                val direccion = negocioMap?.get("direccion") as? String
                                                val dist = (item["distanceKm"] as? Number)?.toDouble()
                                                list.add(LocalService(id, nombre, precio, negocioNombre, direccion, dist))
                                            }
                                            is Servicio -> {
                                                val id = item.idServicio
                                                val nombre = item.nombre ?: ""
                                                val precio = runCatching { item.precio.toDouble() }.getOrDefault(0.0)
                                                val negocioNombre = item.negocio?.nombre
                                                val direccion = item.negocio?.direccion
                                                val dist = runCatching {
                                                    val f = item::class.java.getDeclaredField("distanceKm")
                                                    f.isAccessible = true
                                                    (f.get(item) as? Number)?.toDouble()
                                                }.getOrNull()
                                                list.add(LocalService(id, nombre, precio, negocioNombre, direccion, dist))
                                            }
                                        }
                                    }
                                    results = list.sortedBy { it.distanceKm ?: Double.MAX_VALUE }
                                    Toast.makeText(ctx, "Encontrados: ${results.size}", Toast.LENGTH_SHORT).show()
                                    step = Step.RESULTS
                                } else {
                                    errorMsg = "Error servidor: ${resp.code()}"
                                }
                            } catch (e: Exception) {
                                errorMsg = "Error red: ${e.message}"
                            } finally { loadingResults = false }
                        }
                    }
                    if (errorMsg != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(errorMsg!!, color = MaterialTheme.colorScheme.error)
                    }
                }

                Step.RESULTS -> {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BellaIcon()
                        Text(
                            "Resultados: ${results.size}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    if (results.isEmpty()) {
                        AssistantHint("No se encontraron servicios cerca. 🕵️‍♀️")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(results) { s ->
                                ResultCard(s)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    PrimaryButton("Nueva búsqueda", Icons.Outlined.Send) { step = Step.LOCATION_CHOICE }
                }
            }
        }
    }
}

/* ---------- piezas de UI reutilizables ---------- */

@Composable
private fun BellaIcon(sizeDp: Dp = 24.dp, play: Boolean = true) {
    val comp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.bellabot))
    val progress by animateLottieCompositionAsState(
        composition = comp,
        iterations = LottieConstants.IterateForever,
        isPlaying = play
    )
    LottieAnimation(
        composition = comp,
        progress = { progress },
        modifier = Modifier
            .size(sizeDp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun BotBubble(icon: @Composable () -> Unit, text: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        icon()
        Spacer(Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun AssistantHint(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        BellaIcon()
        Spacer(Modifier.width(8.dp))
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PrimaryButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(onClick = onClick, shape = RoundedCornerShape(14.dp)) {
        Icon(icon, null)
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}

@Composable
private fun SelectorRow(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        tonalElevation = if (selected) 4.dp else 0.dp,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Text(label, modifier = Modifier.weight(1f))
            RadioButton(selected = selected, onClick = onClick)
        }
    }
}

@Composable
private fun ResultCard(s: LocalService) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = s.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Money, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(6.dp))
                Text("Precio: S/ ${"%.2f".format(s.precio)}")
            }
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Storefront, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(6.dp))
                Text("Negocio: ${s.negocioNombre ?: "—"}")
            }
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(6.dp))
                Text("Dirección: ${s.direccion ?: "—"}")
            }
            if (s.distanceKm != null) {
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.MyLocation, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.dp))
                    Text("A ${"%.1f".format(s.distanceKm)} km")
                }
            }
        }
    }
}

/** Ubicación actual (alta precisión) y si falla, última conocida */
@SuppressLint("MissingPermission")
suspend fun getCurrentOrLastLocation(context: Context): Pair<Double, Double>? = try {
    val client = LocationServices.getFusedLocationProviderClient(context)
    val cts = CancellationTokenSource()
    val current = client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token).await()
    if (current != null) Pair(current.latitude, current.longitude)
    else client.lastLocation.await()?.let { Pair(it.latitude, it.longitude) }
} catch (_: Exception) { null }
