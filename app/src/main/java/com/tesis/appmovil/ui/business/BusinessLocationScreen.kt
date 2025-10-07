package com.tesis.appmovil.ui.business

import android.Manifest
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import com.tesis.appmovil.viewmodel.NegocioViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import com.google.android.gms.maps.CameraUpdateFactory
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun BusinessLocationScreen(
    negocioViewModel: NegocioViewModel,
    onLocationSelected: (LatLng) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val defaultLocation = remember { LatLng(-12.0464, -77.0428) } // Lima
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var showInstructions by remember { mutableStateOf(true) }

    // === Estados del buscador ===
    var query by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var results by remember { mutableStateOf<List<Address>>(emptyList()) }
    val scope = rememberCoroutineScope()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val ui by negocioViewModel.ui.collectAsState()

    if (showInstructions) {
        Dialog(onDismissRequest = { showInstructions = false }) {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp)
                ) {
                    Text("Ubicación en el mapa", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Puedes tocar el mapa para fijar el pin o usar el buscador por dirección.",
                        style = MaterialTheme.typography.bodyMedium, color = Color.Gray
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { showInstructions = false },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C1349))
                    ) { Text("ENTENDIDO", style = MaterialTheme.typography.bodyLarge) }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ubicación del negocio", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                Button(
                    onClick = { selectedLocation?.let(onLocationSelected) },
                    modifier = Modifier.fillMaxWidth().height(64.dp).padding(16.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C1349)),
                    enabled = selectedLocation != null
                ) {
                    Text(
                        if (selectedLocation != null) "GUARDAR Y CONTINUAR →" else "SELECCIONA O BUSCA UNA UBICACIÓN",
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 2
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(Modifier.fillMaxSize().padding(paddingValues)) {

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = locationPermissionState.status.isGranted,
                    latLngBoundsForCameraTarget = LatLngBounds(
                        LatLng(-12.2, -77.2),
                        LatLng(-11.8, -76.8)
                    )
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = locationPermissionState.status.isGranted,
                    compassEnabled = false
                ),
                onMapClick = { latLng ->
                    selectedLocation = latLng
                    searchError = null
                    val idNegocio = ui.negocio?.id_negocio ?: ui.negocioCreadoId
                    if (idNegocio != null) {
                        scope.launch {
                            negocioViewModel.actualizarUbicacionExacta(
                                idNegocio = idNegocio,
                                latitud = latLng.latitude,
                                longitud = latLng.longitude
                            )
                        }
                    }
                }
            ) {
                selectedLocation?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "Ubicación del negocio",
                        // ↓ Se mantiene el snippet para depuración interna, no visible en lista
                        snippet = "Lat: ${"%.6f".format(location.latitude)}, Lng: ${"%.6f".format(location.longitude)}",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)
                    )
                }
            }

            // ======= BUSCADOR =======
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = {
                            query = it
                            searchError = null
                        },
                        placeholder = { Text("Buscar dirección…") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Buscar") },
                        trailingIcon = {
                            if (query.isNotBlank()) {
                                IconButton(onClick = {
                                    query = ""
                                    results = emptyList()
                                    searchError = null
                                }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Limpiar")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                scope.launch {
                                    buscarDireccion(
                                        context = context,
                                        query = query,
                                        onStart = { isSearching = true },
                                        onFinish = { isSearching = false },
                                        onError = { msg -> searchError = msg },
                                        onResults = { list ->
                                            results = list
                                            if (list.isNotEmpty()) {
                                                val a = list.first()
                                                val latLng = LatLng(a.latitude, a.longitude)
                                                selectedLocation = latLng
                                                scope.launch {
                                                    cameraPositionState.animate(
                                                        CameraUpdateFactory.newLatLngZoom(latLng, 16f)
                                                    )
                                                }
                                                val idNegocio = ui.negocio?.id_negocio ?: ui.negocioCreadoId
                                                if (idNegocio != null) {
                                                    scope.launch {
                                                        negocioViewModel.actualizarUbicacionExacta(
                                                            idNegocio = idNegocio,
                                                            latitud = latLng.latitude,
                                                            longitud = latLng.longitude
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // ======= LISTA DE RESULTADOS =======
                if (results.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        tonalElevation = 8.dp,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(4.dp)
                                .heightIn(max = 220.dp)
                        ) {
                            items(results.take(5)) { addr ->
                                val line = addr.getAddressLine(0) ?: construirLinea(addr)
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val latLng = LatLng(addr.latitude, addr.longitude)
                                            selectedLocation = latLng
                                            scope.launch {
                                                cameraPositionState.animate(
                                                    CameraUpdateFactory.newLatLngZoom(latLng, 16f)
                                                )
                                            }
                                            val idNegocio = ui.negocio?.id_negocio ?: ui.negocioCreadoId
                                            if (idNegocio != null) {
                                                scope.launch {
                                                    negocioViewModel.actualizarUbicacionExacta(
                                                        idNegocio = idNegocio,
                                                        latitud = latLng.latitude,
                                                        longitud = latLng.longitude
                                                    )
                                                }
                                            }
                                            results = emptyList()
                                        }
                                        .padding(12.dp)
                                ) {
                                    Text(line, style = MaterialTheme.typography.bodyMedium)
                                    // ⬇️ Eliminado el texto de lat/lng para no mostrar en UI
                                }
                                Divider()
                            }
                        }
                    }
                }

                if (isSearching) {
                    Spacer(Modifier.height(8.dp))
                    AssistChip(onClick = {}, label = { Text("Buscando…") })
                } else if (searchError != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(searchError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

/* ================= Helpers ================= */

private suspend fun buscarDireccion(
    context: Context,
    query: String,
    onStart: () -> Unit,
    onFinish: () -> Unit,
    onError: (String) -> Unit,
    onResults: (List<Address>) -> Unit
) {
    if (query.isBlank()) {
        onError("Escribe una dirección.")
        return
    }
    if (!Geocoder.isPresent()) {
        onError("Geocoder no disponible en este dispositivo.")
        return
    }
    onStart()
    try {
        val list = geocodeCompat(context, query)
        if (list.isEmpty()) onError("No se encontraron coincidencias.")
        onResults(list)
    } catch (e: Exception) {
        onError("Error al geocodificar: ${e.message ?: "desconocido"}")
    } finally {
        onFinish()
    }
}

private suspend fun geocodeCompat(context: Context, query: String): List<Address> {
    val geocoder = Geocoder(context, Locale.getDefault())
    return if (Build.VERSION.SDK_INT >= 33) {
        suspendCancellableCoroutine { cont ->
            geocoder.getFromLocationName(query, 5, object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    if (cont.isActive) cont.resume(addresses)
                }
                override fun onError(errorMessage: String?) {
                    if (cont.isActive) cont.resume(emptyList())
                }
            })
        }
    } else {
        @Suppress("DEPRECATION")
        withContext(Dispatchers.IO) {
            geocoder.getFromLocationName(query, 5) ?: emptyList()
        }
    }
}

private fun construirLinea(a: Address): String =
    listOfNotNull(a.thoroughfare, a.subThoroughfare, a.locality, a.subAdminArea, a.adminArea)
        .joinToString(", ")
