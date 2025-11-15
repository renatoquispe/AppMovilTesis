package com.tesis.appmovil.ui.chat

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
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
    val negocioId: Int?,
    val negocioNombre: String?,
    val direccion: String?,
    val distanceKm: Double? = null
)

// ---- helpers (DTO/Model flexibles) ----
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ChatBotScreen(navController: NavController? = null) {
    val api = RetrofitClient.api
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    var step by remember { mutableStateOf(Step.GREETING) }
    var lastStep by remember { mutableStateOf(step) }

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
            if (!sResp.isSuccessful) emptyList()
            else {
                val data = sResp.body()?.data ?: emptyList<Servicio>()
                val mapa = LinkedHashMap<Int, String>()
                data.forEach { s ->
                    val id = catIdOf(s.negocio?.categoria)
                    val nom = catNameOf(s.negocio?.categoria)
                    if (id != null && !nom.isNullOrBlank()) mapa.putIfAbsent(id, nom)
                }
                mapa.map { (id, nombre) -> LocalCategory(id, nombre) }
            }
        } catch (_: Exception) { emptyList() }
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
                    lastStep = step; step = Step.CATEGORY
                }
            } else {
                errorMsg = "Permiso denegado. Por favor escribe la ubicación."
                lastStep = step; step = Step.LOCATION_INPUT
            }
        }
    )

    // ----------- Fondo degradado llamativo -----------
    val bg = Brush.verticalGradient(
        listOf(
            Color(0xFF0E1B4D),
            Color(0xFF4F46E5),
            Color(0xFF7C3AED),
            Color(0xFFEDE9FE)
        )
    )

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BellaTopIcon(sizeDp = 40.dp)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(padding)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                color = Color.White.copy(alpha = 0.14f),
                tonalElevation = 0.dp,
                shape = RoundedCornerShape(24.dp)
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        val forward = targetState.ordinal > initialState.ordinal
                        val offset = if (forward) { { full: Int -> full / 3 } } else { { full: Int -> -full / 3 } }
                        (slideInHorizontally(tween(350, easing = FastOutSlowInEasing), initialOffsetX = offset) +
                                fadeIn(tween(250)))
                            .togetherWith(
                                slideOutHorizontally(tween(300), targetOffsetX = offset) +
                                        fadeOut(tween(200))
                            )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) { current ->
                    when (current) {
                        Step.GREETING -> GreetingStep { lastStep = step; step = Step.LOCATION_CHOICE }
                        Step.LOCATION_CHOICE -> LocationChoiceStep(
                            useCurrentLocation = useCurrentLocation,
                            onUseCurrent = {
                                useCurrentLocation = true
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            },
                            onTypeOther = { useCurrentLocation = false; lastStep = step; step = Step.LOCATION_INPUT }
                        )
                        Step.LOCATION_INPUT -> LocationInputStep(
                            typedAddress = typedAddress,
                            onChange = { typedAddress = it },
                            onNext = { lastStep = step; step = Step.CATEGORY }
                        )
                        Step.CATEGORY -> CategoryStep(
                            categories = categories,
                            loading = loadingCats,
                            selectedCategoryId = selectedCategoryId,
                            loadOnce = {
                                if (categories.isEmpty()) scope.launch { categories = fetchCategoriesOnce() }
                            },
                            onSelect = { selectedCategoryId = it },
                            onNext = { lastStep = step; step = Step.BUDGET },
                            errorMsg = errorMsg
                        )
                        Step.BUDGET -> BudgetStep(
                            priceMinText = priceMinText,
                            priceMaxText = priceMaxText,
                            onMinChange = { priceMinText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            onMaxChange = { priceMaxText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            onSearch = {
                                loadingResults = true
                                errorMsg = null
                                scope.launch {
                                    try {
                                        val (lat, lng) = if (useCurrentLocation == true) latitude to longitude else null to null
                                        val req = ServicioFilterRequest(
                                            servicio = null,
                                            ubicacion = if (useCurrentLocation == false) typedAddress else null,
                                            precioMin = priceMinText.ifBlank { null }?.toDoubleOrNull(),
                                            precioMax = priceMaxText.ifBlank { null }?.toDoubleOrNull(),
                                            categoryId = selectedCategoryId,
                                            latitude = lat,
                                            longitude = lng,
                                            radiusKm = 3.0
                                        )
                                        val resp = RetrofitClient.api.filterServicios(req)
                                        if (resp.isSuccessful) {
                                            val raw = (resp.body()?.data as? List<*>) ?: emptyList<Any?>()
                                            val list = mutableListOf<LocalService>()
                                            raw.forEach { item ->
                                                when (item) {
                                                    is Map<*, *> -> {
                                                        val id = (item["idServicio"] as? Number)?.toInt()
                                                            ?: (item["id_servicio"] as? Number)?.toInt() ?: 0
                                                        val nombre = (item["nombre"] as? String) ?: ""
                                                        val precio = (item["precio"] as? Number)?.toDouble() ?: 0.0
                                                        val negocioMap = item["negocio"] as? Map<*, *>
                                                        val negocioId = (negocioMap?.get("idNegocio") as? Number)?.toInt()
                                                        val negocioNombre = negocioMap?.get("nombre") as? String
                                                        val direccion = negocioMap?.get("direccion") as? String
                                                        val dist = (item["distanceKm"] as? Number)?.toDouble()
                                                        list.add(LocalService(id, nombre, precio, negocioId, negocioNombre, direccion, dist))
                                                    }
                                                    is Servicio -> {
                                                        val id = item.idServicio
                                                        val nombre = item.nombre ?: ""
                                                        val precio = runCatching { item.precio.toDouble() }.getOrDefault(0.0)
                                                        val negocioId = item.negocio?.idNegocio
                                                        val negocioNombre = item.negocio?.nombre
                                                        val direccion = item.negocio?.direccion
                                                        val dist = runCatching {
                                                            val f = item::class.java.getDeclaredField("distanceKm")
                                                            f.isAccessible = true
                                                            (f.get(item) as? Number)?.toDouble()
                                                        }.getOrNull()
                                                        list.add(LocalService(id, nombre, precio, negocioId, negocioNombre, direccion, dist))
                                                    }
                                                }
                                            }
                                            results = list.sortedBy { it.distanceKm ?: Double.MAX_VALUE }
                                            Toast.makeText(ctx, "Encontrados: ${results.size}", Toast.LENGTH_SHORT).show()
                                            lastStep = step; step = Step.RESULTS
                                        } else errorMsg = "Error servidor: ${resp.code()}"
                                    } catch (e: Exception) {
                                        errorMsg = "Error red: ${e.message}"
                                    } finally { loadingResults = false }
                                }
                            },
                            errorMsg = errorMsg
                        )
                        Step.RESULTS -> ResultsStep(
                            results = results,
                            onNewSearch = { lastStep = step; step = Step.LOCATION_CHOICE },
                            onOpenBusiness = { negocioId, nombre ->
                                openBusiness(ctx, navController, negocioId, nombre)
                            }
                        )
                    }
                }
            }
            LoaderOverlay(show = loadingCats || loadingResults)
        }
    }
}

/* ===================== Pasos ===================== */

@Composable
private fun GreetingStep(onNext: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        BotBubble(icon = { AssistantIcon() }, text = "Hola 👋 soy BellaBot. ¿Quieres empezar?")
        Spacer(Modifier.height(12.dp))
        PrimaryButton("Sí, vamos", Icons.Outlined.Send, onNext)
    }
}

@Composable
private fun LocationChoiceStep(
    useCurrentLocation: Boolean?,
    onUseCurrent: () -> Unit,
    onTypeOther: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        BotBubble(icon = { AssistantIcon() }, text = "¿Deseas usar tu ubicación actual o escribir otra?")
        Spacer(Modifier.height(12.dp))
        SelectorRow(selected = useCurrentLocation == true, icon = Icons.Outlined.MyLocation, label = "Usar mi ubicación actual", onClick = onUseCurrent)
        Spacer(Modifier.height(6.dp))
        SelectorRow(selected = useCurrentLocation == false, icon = Icons.Outlined.LocationOn, label = "Escribir otra ubicación", onClick = onTypeOther)
    }
}

@Composable
private fun LocationInputStep(
    typedAddress: String,
    onChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        BotBubble(icon = { AssistantIcon() }, text = "Escribe tu distrito/dirección 🧭")
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = typedAddress,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Outlined.LocationOn, null) },
            placeholder = { Text("Ej. Av. Colonial 123, Bellavista") }
        )
        Spacer(Modifier.height(12.dp))
        PrimaryButton("Continuar", Icons.Outlined.Send, onNext)
    }
}

@Composable
private fun CategoryStep(
    categories: List<LocalCategory>,
    loading: Boolean,
    selectedCategoryId: Int?,
    loadOnce: () -> Unit,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit,
    errorMsg: String?
) {
    LaunchedEffect(Unit) { loadOnce() }

    Column(Modifier.fillMaxSize()) {
        BotBubble(icon = { AssistantIcon() }, text = "¿Qué servicio buscas? Elige una categoría 🧩")
        Spacer(Modifier.height(10.dp))

        if (loading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
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
                    ) { onSelect(c.id) }
                }
            }
            Spacer(Modifier.height(12.dp))
            PrimaryButton("Siguiente", Icons.Outlined.Send, onNext)
        }
        if (errorMsg != null) {
            Spacer(Modifier.height(8.dp)); Text(errorMsg, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun BudgetStep(
    priceMinText: String,
    priceMaxText: String,
    onMinChange: (String) -> Unit,
    onMaxChange: (String) -> Unit,
    onSearch: () -> Unit,
    errorMsg: String?
) {
    Column(Modifier.fillMaxSize()) {
        BotBubble(icon = { AssistantIcon() }, text = "Define tu presupuesto (opcional) 💸")
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = priceMinText,
                onValueChange = onMinChange,
                label = { Text("Desde") },
                leadingIcon = { Icon(Icons.Outlined.Money, null) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            OutlinedTextField(
                value = priceMaxText,
                onValueChange = onMaxChange,
                label = { Text("Hasta") },
                leadingIcon = { Icon(Icons.Outlined.Money, null) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }
        Spacer(Modifier.height(12.dp))
        PrimaryButton("Buscar servicios", Icons.Outlined.Send, onSearch)
        if (errorMsg != null) { Spacer(Modifier.height(8.dp)); Text(errorMsg, color = MaterialTheme.colorScheme.error) }
    }
}

@Composable
private fun ResultsStep(
    results: List<LocalService>,
    onNewSearch: () -> Unit,
    onOpenBusiness: (negocioId: Int?, nombre: String?) -> Unit
) {
    val context = LocalContext.current   // 👈 LEE AQUÍ el contexto (no dentro de onClick)

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BellaIcon()
            Text("Resultados: ${results.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(8.dp))

        if (results.isEmpty()) {
            AssistantHint("No se encontraron servicios cerca. 🕵️‍♀️")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(results) { s ->
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = if (isSystemInDarkTheme()) Color(0xFF2B2B2B) else Color.White
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            val textColor = if (isSystemInDarkTheme()) Color.White else Color.Black

                            Text(
                                s.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = textColor
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Money, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(6.dp))
                                Text("Precio: S/ ${"%.2f".format(s.precio)}", color = textColor)
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Storefront, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(6.dp))
                                Text("Negocio: ${s.negocioNombre ?: "—"}", color = textColor)
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(6.dp))
                                Text("Dirección: ${s.direccion ?: "—"}", color = textColor)
                            }

                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = { onOpenBusiness(s.negocioId, s.negocioNombre) },
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Outlined.Storefront, null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Ver negocio", color = textColor)
                                }
                                OutlinedButton(
                                    onClick = { openMaps(context, s.direccion, s.negocioNombre) },
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Outlined.Map, null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Cómo llegar", color = textColor)
                                }
                                OutlinedButton(
                                    onClick = { shareResult(context, s) },
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Outlined.Share, null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Compartir", color = textColor)
                                }
                            }
                        }
                    }

                }
            }
        }
        Spacer(Modifier.height(10.dp))
        PrimaryButton("Nueva búsqueda", Icons.Outlined.Send, onNewSearch)
    }
}

/* ===================== Overlays y piezas de UI ===================== */

@Composable
private fun LoaderOverlay(show: Boolean) {
    AnimatedVisibility(
        visible = show,
        enter = fadeIn(animationSpec = tween(150)),
        exit = fadeOut(animationSpec = tween(150))
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            val comp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.bellabot))
            val progress by animateLottieCompositionAsState(comp, iterations = LottieConstants.IterateForever)
            LottieAnimation(composition = comp, progress = { progress }, modifier = Modifier.size(120.dp))
        }
    }
}

// TopBar avatar grande
@Composable private fun BellaTopIcon(sizeDp: Dp = 40.dp, play: Boolean = true) {
    BellaIcon(sizeDp = sizeDp, play = play)
}

// Icono general (bellabot.json)
@Composable
private fun BellaIcon(sizeDp: Dp = 24.dp, play: Boolean = true) {
    val comp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.bellabot))
    val progress by animateLottieCompositionAsState(composition = comp, iterations = LottieConstants.IterateForever, isPlaying = play)
    LottieAnimation(
        composition = comp,
        progress = { progress },
        modifier = Modifier.size(sizeDp).clip(CircleShape),
        contentScale = ContentScale.Crop
    )
}

// Icono de burbuja (asistente.json) — 68dp fijo
@Composable
private fun AssistantIcon(sizeDp: Dp = 68.dp, play: Boolean = true) {
    val comp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.asistente))
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        Text(text, color = Color.White)
    }
}

@Composable
private fun PrimaryButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Button(onClick = onClick, shape = RoundedCornerShape(14.dp)) {
        Icon(icon, null); Spacer(Modifier.width(8.dp)); Text(label)
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
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = Color.White.copy(alpha = if (selected) 0.35f else 0.25f)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(10.dp))
            Text(label, modifier = Modifier.weight(1f))
            RadioButton(selected = selected, onClick = onClick)
        }
    }
}

/* ===== Helpers de acciones ===== */

private fun openMaps(ctx: Context, direccion: String?, nombre: String?) {
    val q = (nombre?.let { "$it, " } ?: "") + (direccion ?: "")
    if (q.isBlank()) {
        Toast.makeText(ctx, "Sin dirección para navegar", Toast.LENGTH_SHORT).show()
        return
    }
    val uri = Uri.parse("geo:0,0?q=${Uri.encode(q)}")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }
    runCatching { ctx.startActivity(intent) }
        .onFailure { Toast.makeText(ctx, "No se pudo abrir Maps", Toast.LENGTH_SHORT).show() }
}

private fun shareResult(ctx: Context, s: LocalService) {
    val text = buildString {
        append("Servicio: ${s.nombre}\n")
        append("Precio: S/ ${"%.2f".format(s.precio)}\n")
        if (!s.negocioNombre.isNullOrBlank()) append("Negocio: ${s.negocioNombre}\n")
        if (!s.direccion.isNullOrBlank()) append("Dirección: ${s.direccion}")
    }
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    ctx.startActivity(Intent.createChooser(send, "Compartir servicio"))
}

private fun openBusiness(ctx: Context, navController: NavController?, negocioId: Int?, @Suppress("UNUSED_PARAMETER") nombre: String?) {
    if (negocioId == null) {
        Toast.makeText(ctx, "No se encontró el negocio", Toast.LENGTH_SHORT).show()
        return
    }
    if (navController != null) {
        navController.navigate("businessDetail/$negocioId")
        return
    }
    runCatching {
        val clazz = Class.forName("com.tesis.appmovil.ui.business.BusinessDetailActivity")
        val i = Intent(ctx, clazz).apply { putExtra("idNegocio", negocioId) }
        ctx.startActivity(i)
    }.onFailure {
        Toast.makeText(ctx, "Busca el negocio (id=$negocioId) desde Inicio", Toast.LENGTH_SHORT).show()
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
