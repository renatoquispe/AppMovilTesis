package com.tesis.appmovil.ui.search

import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tesis.appmovil.viewmodel.HomeNegocioViewModel
import com.tesis.appmovil.viewmodel.ServicioViewModel

// Maps
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuscarScreen(
    vmNegocios: HomeNegocioViewModel,
    vmServicios: ServicioViewModel,
    onClickNegocio: (Int) -> Unit
) {
    val state by vmNegocios.state.collectAsState()
    val serviciosState by vmServicios.ui.collectAsState()

    LaunchedEffect(Unit) {
        if (!state.isLoading && state.negocios.isEmpty()) {
            vmNegocios.cargarDestacados(limit = 10)
            if (!serviciosState.isLoading && serviciosState.servicios.isEmpty()) {
                vmServicios.cargarServicios()
            }
        }
    }

    val imageByNegocio by remember {
        derivedStateOf {
            state.negocios.associate { negocio ->
                negocio.nombre to (negocio.imagenes?.firstOrNull()?.url_imagen ?: "")
            }
        }
    }

    // FILTRO LOCAL
    val query = remember(state.query) { state.query.trim() }
    val negociosFiltrados = remember(state.negocios, query) {
        if (query.isBlank()) state.negocios
        else {
            val q = query.lowercase()
            state.negocios.filter { n ->
                n.nombre.lowercase().contains(q) ||
                        (n.direccion?.lowercase()?.contains(q) == true) ||
                        (n.descripcion?.lowercase()?.contains(q) == true)
            }
        }
    }

    // Google Maps
    val lima = LatLng(-12.0464, -77.0428)
    val firstCoords: LatLng? = negociosFiltrados
        .firstOrNull { it.latitud != null && it.longitud != null }
        ?.let { LatLng(it.latitud!!, it.longitud!!) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(firstCoords ?: lima, 12f)
    }

    // ---------- FIX PARA QUE EL SHEET NUNCA SE OCUlTE ----------
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = true, // 💥 evita el estado HIDDEN
        confirmValueChange = { newValue ->
            newValue != SheetValue.Hidden // 💥 PROHIBIDO OCULTARSE
        }
    )

    val sheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )
    // -----------------------------------------------------------

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val density = LocalDensity.current
    val imeVisible = WindowInsets.ime.getBottom(density) > 0

    val maxSheetHeight = if (imeVisible) screenHeight * 0.30f else screenHeight * 0.40f

    BottomSheetScaffold(
        scaffoldState = sheetScaffoldState,
        sheetPeekHeight = 64.dp,
        sheetSwipeEnabled = true,
        sheetDragHandle = { BottomSheetDefaults.DragHandle() },
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetTonalElevation = 2.dp,
        sheetShadowElevation = 10.dp,

        sheetContent = {
            val titulo = if (state.query.isBlank())
                "Servicios más buscados en tu zona"
            else
                "Resultados para \"${state.query}\""

            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxSheetHeight)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                if (state.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                Spacer(Modifier.height(8.dp))

                state.error?.let {
                    Text(
                        text = "Error: $it",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                }

                if (negociosFiltrados.isEmpty() && !state.isLoading) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (state.query.isBlank()) "No hay negocios para mostrar."
                            else "No se encontraron resultados para \"${state.query}\""
                        )
                    }
                } else {
                    MasBuscadosSection(
                        title = titulo,
                        negocios = negociosFiltrados,
                        imageByNombre = imageByNegocio,
                        onClick = { negocio -> onClickNegocio(negocio.id_negocio) }
                    )
                }
            }
        }
    ) { innerPadding ->
        /*Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )*/
        Box(
            Modifier
                .fillMaxSize()
                .padding(
                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding() - 45.dp // ⬅️ Aumentamos mapa hacia abajo
                )
        )
        {
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
                negociosFiltrados.forEach { n ->
                    if (n.latitud != null && n.longitud != null) {
                        Marker(
                            state = MarkerState(position = LatLng(n.latitud!!, n.longitud!!)),
                            title = n.nombre,
                            snippet = n.direccion ?: ""
                        )
                    }
                }
            }

            SearchBarOverlay(
                text = state.query,
                onTextChange = vmNegocios::onQueryChange,
                onSearch = vmNegocios::buscarAhora,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp, start = 16.dp, end = 16.dp)
            )
        }
    }
}

@Composable
fun MarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    speed: Int = 40
) {
    val scrollState = rememberScrollState()
    var textWidth by remember { mutableStateOf(0) }
    var containerWidth by remember { mutableStateOf(0) }

    LaunchedEffect(textWidth, containerWidth) {
        if (textWidth > containerWidth) {
            while (true) {
                val distance = textWidth - containerWidth
                scrollState.animateScrollTo(distance, tween(distance * speed))
                scrollState.animateScrollTo(0, tween(distance * speed))
            }
        }
    }

    Box(
        modifier = modifier
            .horizontalScroll(scrollState, enabled = false)
            .onGloballyPositioned { containerWidth = it.size.width }
    ) {
        Text(
            text = text,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .onGloballyPositioned { textWidth = it.size.width },
            fontSize = 14.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBarOverlay(
    text: String,
    onTextChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    MarqueeText("Busque por negocio o distrito", speed = 35)
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.surface
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions { onSearch() }
            )

            IconButton(onClick = onSearch) {
                Icon(Icons.Outlined.Search, contentDescription = "Buscar")
            }
        }
    }
}
