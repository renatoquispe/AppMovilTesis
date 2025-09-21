//package com.tesis.appmovil.ui.business
//import com.google.android.gms.maps.model.BitmapDescriptorFactory
//import android.Manifest
//import androidx.compose.foundation.layout.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.LocationOn
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//import com.google.android.gms.maps.model.CameraPosition
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.gms.maps.model.LatLngBounds
//import com.google.maps.android.compose.*
//import com.google.accompanist.permissions.ExperimentalPermissionsApi
//import com.google.accompanist.permissions.isGranted
//import com.google.accompanist.permissions.rememberPermissionState
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
//                .padding(16.dp)
//        ) {
//            // Instrucciones
//            Text(
//                text = "Selecciona la ubicación exacta de tu negocio.",
//                style = MaterialTheme.typography.bodyMedium,
//                color = Color.Gray,
//                modifier = Modifier.padding(bottom = 16.dp)
//            )
//
//            // Mapa
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(400.dp)
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
//                    // Marcador en la ubicación seleccionada
//                    selectedLocation?.let { location ->
//                        Marker(
//                            state = MarkerState(position = location),
//                            title = "Ubicación del negocio",
//                            snippet = "Lat: ${location.latitude}, Lng: ${location.longitude}"
//                        )
//                    }
//
//                    // Marcador central personalizado (solo icono)
//                    selectedLocation?.let {
//                        Marker(
//                            state = MarkerState(position = it),
//                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE),
//                            title = "Ubicación seleccionada"
//                        )
//                    }
//                }
//
//                // Icono de ubicación en el centro (sobrepuesto)
//                Box(
//                    modifier = Modifier
//                        .align(Alignment.Center)
//                        .offset(y = (-24).dp), // Ajusta para que apunte exactamente al centro
//                    contentAlignment = Alignment.Center
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.LocationOn,
//                        contentDescription = "Centro del mapa",
//                        tint = Color(0xFF5C1349), // Color morado de tu app
//                        modifier = Modifier.size(48.dp)
//                    )
//                }
//            }
//
//            Spacer(Modifier.height(16.dp))
//
//            // Botón de confirmación
//            Button(
//                onClick = {
//                    selectedLocation?.let { onLocationSelected(it) }
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(56.dp),
//                shape = MaterialTheme.shapes.medium,
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C1349)),
//                enabled = selectedLocation != null
//            ) {
//                Text(
//                    text = if (selectedLocation != null) {
//                        "CONFIRMAR UBICACIÓN"
//                    } else {
//                        "SELECCIONA UNA UBICACIÓN"
//                    },
//                    style = MaterialTheme.typography.bodyLarge
//                )
//            }
//
//            // Información de la ubicación seleccionada
//            selectedLocation?.let { location ->
//                Spacer(Modifier.height(8.dp))
//                Text(
//                    text = "Ubicación seleccionada:\nLat: ${"%.6f".format(location.latitude)}\nLng: ${"%.6f".format(location.longitude)}",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = Color.Gray
//                )
//            }
//        }
//    }
//}
//
package com.tesis.appmovil.ui.business

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

// Necesitas este import para BitmapDescriptorFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BusinessLocationScreen(
    onLocationSelected: (LatLng) -> Unit,
    onBack: () -> Unit
) {
    // Ubicación inicial (Lima, Perú)
    val defaultLocation = remember { LatLng(-12.0464, -77.0428) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }

    // Estado para la cámara del mapa
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    // Permisos de ubicación
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Instrucciones
            Text(
                text = "Selecciona la ubicación exacta de tu negocio.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Mapa
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .weight(1f)
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
                            snippet = "Lat: ${location.latitude}, Lng: ${location.longitude}",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)
                        )
                    }
                }

                // Icono de ubicación en el centro (SOLO como guía visual, no es un marcador real)
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-24).dp), // Ajusta para que apunte exactamente al centro
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Centro del mapa",
                        tint = Color(0xFF5C1349), // Color morado de tu app
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Botón de confirmación
            Button(
                onClick = {
                    selectedLocation?.let { onLocationSelected(it) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C1349)),
                enabled = selectedLocation != null
            ) {
                Text(
                    text = if (selectedLocation != null) {
                        "CONFIRMAR UBICACIÓN"
                    } else {
                        "SELECCIONA UNA UBICACIÓN"
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Información de la ubicación seleccionada
            selectedLocation?.let { location ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Ubicación seleccionada:\nLat: ${"%.6f".format(location.latitude)}\nLng: ${"%.6f".format(location.longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}