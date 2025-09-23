//package com.tesis.appmovil.ui.business
//
//import android.Manifest
//import androidx.compose.foundation.layout.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.window.Dialog
//import com.google.android.gms.maps.model.CameraPosition
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.gms.maps.model.LatLngBounds
//import com.google.maps.android.compose.*
//import com.google.accompanist.permissions.ExperimentalPermissionsApi
//import com.google.accompanist.permissions.isGranted
//import com.google.accompanist.permissions.rememberPermissionState
//import com.google.android.gms.maps.model.BitmapDescriptorFactory
//
//@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
//@Composable
//fun BusinessLocationScreen(
//    onLocationSelected: (LatLng) -> Unit,
//    onBack: () -> Unit
//) {
//    // Ubicación inicial (Lima, Perú)
//    val defaultLocation = remember { LatLng(-12.0464, -77.0428) }
//    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
//    var showInstructions by remember { mutableStateOf(true) }
//
//    // Estado para la cámara del mapa
//    val cameraPositionState = rememberCameraPositionState {
//        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
//    }
//
//    // Permisos de ubicación
//    val locationPermissionState = rememberPermissionState(
//        Manifest.permission.ACCESS_FINE_LOCATION
//    )
//
//    // Modal de instrucciones inicial
//    if (showInstructions) {
//        Dialog(onDismissRequest = { showInstructions = false }) {
//            Surface(
//                shape = MaterialTheme.shapes.medium,
//                color = MaterialTheme.colorScheme.surface
//            ) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(24.dp)
//                ) {
//                    Text(
//                        text = "Ubicación en el mapa",
//                        style = MaterialTheme.typography.headlineSmall,
//                        modifier = Modifier.padding(bottom = 16.dp)
//                    )
//
//                    Text(
//                        text = "Selecciona la ubicación exacta de tu negocio.",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = Color.Gray,
//                        modifier = Modifier.padding(bottom = 24.dp)
//                    )
//
//                    Button(
//                        onClick = { showInstructions = false },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(48.dp),
//                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C1349))
//                    ) {
//                        Text("ENTENDIDO")
//                    }
//                }
//            }
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        text = "Ubicación del negocio",
//                        style = MaterialTheme.typography.titleMedium
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = onBack) {
//                        Icon(
//                            imageVector = Icons.Default.ArrowBack,
//                            contentDescription = "Regresar"
//                        )
//                    }
//                }
//            )
//        }
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//        ) {
//            // Mapa (ocupa toda la pantalla excepto el top bar)
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .weight(1f)
//            ) {
//                GoogleMap(
//                    modifier = Modifier.fillMaxSize(),
//                    cameraPositionState = cameraPositionState,
//                    properties = MapProperties(
//                        isMyLocationEnabled = locationPermissionState.status.isGranted,
//                        latLngBoundsForCameraTarget = LatLngBounds(
//                            LatLng(-12.2, -77.2), // Suroeste de Lima
//                            LatLng(-11.8, -76.8)  // Noreste de Lima
//                        )
//                    ),
//                    uiSettings = MapUiSettings(
//                        zoomControlsEnabled = true,
//                        myLocationButtonEnabled = locationPermissionState.status.isGranted,
//                        compassEnabled = false
//                    ),
//                    onMapClick = { latLng ->
//                        selectedLocation = latLng
//                        println("📍 Ubicación seleccionada: Lat=${latLng.latitude}, Lng=${latLng.longitude}")
//                    }
//                ) {
//                    // Marcador SOLO aparece después del click
//                    selectedLocation?.let { location ->
//                        Marker(
//                            state = MarkerState(position = location),
//                            title = "Ubicación del negocio",
//                            snippet = "Lat: ${"%.6f".format(location.latitude)}, Lng: ${"%.6f".format(location.longitude)}",
//                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)
//                        )
//                    }
//                }
//            }
//
//            // Botón de confirmación (fijo en la parte inferior)
//            Button(
//                onClick = {
//                    selectedLocation?.let { onLocationSelected(it) }
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(56.dp)
//                    .padding(16.dp),
//                shape = MaterialTheme.shapes.medium,
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C1349)),
//                enabled = selectedLocation != null
//            ) {
//                Text(
//                    text = if (selectedLocation != null) {
//                        "GUARDAR Y CONTINUAR →"
//                    } else {
//                        "SELECCIONA UNA UBICACIÓN EN EL MAPA"
//                    },
//                    style = MaterialTheme.typography.bodyLarge
//                )
//            }
//        }
//    }
//}

package com.tesis.appmovil.ui.business

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.BitmapDescriptorFactory

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun BusinessLocationScreen(
    onLocationSelected: (LatLng) -> Unit,
    onBack: () -> Unit
) {
    // Ubicación inicial (Lima, Perú)
    val defaultLocation = remember { LatLng(-12.0464, -77.0428) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var showInstructions by remember { mutableStateOf(true) }

    // Estado para la cámara del mapa
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    // Permisos de ubicación
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // Modal de instrucciones inicial
    if (showInstructions) {
        Dialog(onDismissRequest = { showInstructions = false }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Ubicación en el mapa",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "Selecciona la ubicación exacta de tu negocio.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    Button(
                        onClick = { showInstructions = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp), // Botón más alto
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C1349))
                    ) {
                        Text("ENTENDIDO", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ubicación del negocio",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                }
            )
        },
        bottomBar = {
            // Botón en el bottom bar para mejor visibilidad
            Surface(
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Button(
                    onClick = {
                        selectedLocation?.let { onLocationSelected(it) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp) // Botón más alto
                        .padding(16.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C1349)),
                    enabled = selectedLocation != null
                ) {
                    Text(
                        text = if (selectedLocation != null) {
                            "GUARDAR Y CONTINUAR →"
                        } else {
                            "SELECCIONA UNA UBICACIÓN"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 2 // Permite texto en dos líneas si es necesario
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = locationPermissionState.status.isGranted,
                    latLngBoundsForCameraTarget = LatLngBounds(
                        LatLng(-12.2, -77.2), // Suroeste de Lima
                        LatLng(-11.8, -76.8)  // Noreste de Lima
                    )
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = locationPermissionState.status.isGranted,
                    compassEnabled = false
                ),
                onMapClick = { latLng ->
                    selectedLocation = latLng
                    println("📍 Ubicación seleccionada: Lat=${latLng.latitude}, Lng=${latLng.longitude}")
                }
            ) {
                // Marcador SOLO aparece después del click
                selectedLocation?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "Ubicación del negocio",
                        snippet = "Lat: ${"%.6f".format(location.latitude)}, Lng: ${"%.6f".format(location.longitude)}",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)
                    )
                }
            }
        }
    }
}