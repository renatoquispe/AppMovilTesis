package com.tesis.appmovil.ui.servicios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tesis.appmovil.viewmodel.ServicioViewModel
import com.tesis.appmovil.data.remote.dto.ServicioUpdate
import androidx.compose.runtime.LaunchedEffect

// Opt-in para las APIs experimentales de Material3 usadas (TopAppBar, Scaffold, etc.)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditServiceScreen(
    servicioId: Int,
    vm: ServicioViewModel,
    navController: NavController
) {
    val ui by vm.ui.collectAsState()
    // intentar tomar el servicio localmente
    val servicio = vm.obtenerServicioPorId(servicioId)

    var nombre by remember { mutableStateOf(servicio?.nombre ?: "") }
    var precio by remember { mutableStateOf(servicio?.precio?.toString() ?: "") }
    var duracion by remember { mutableStateOf(servicio?.duracionMinutos?.toString() ?: "") }
    var descuento by remember { mutableStateOf(servicio?.descuento?.toString() ?: "") }

    // Si no lo tenemos local, pedir al ViewModel (carga detalle)
    LaunchedEffect(servicioId) {
        if (servicio == null) vm.obtenerServicio(servicioId)
    }

    // Cuando la actualización termine (si tu ViewModel establece ui.mutando y ui.seleccionado),
    // volvemos hacia atrás automáticamente.
    LaunchedEffect(ui.seleccionado, ui.mutando) {
        if (!ui.mutando && ui.seleccionado?.idServicio == servicioId) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Editar servicio") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = precio,
                onValueChange = { precio = it },
                label = { Text("Precio") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = duracion,
                onValueChange = { duracion = it },
                label = { Text("Duración (min)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = descuento,
                onValueChange = { descuento = it },
                label = { Text("Descuento (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { navController.popBackStack() }) { Text("Cancelar") }
                Button(onClick = {
                    // Validaciones mínimas
                    val precioDouble = precio.toDoubleOrNull() ?: 0.0
                    val durInt = duracion.toIntOrNull()
                    val descDouble = descuento.toDoubleOrNull()

                    val update = ServicioUpdate(
                        nombre = nombre,
                        precio = precioDouble,
                        duracionMinutos = durInt,
                        descuento = descDouble
                    )
                    vm.actualizarServicio(servicioId, update)
                    // la navegación de vuelta se hace en el LaunchedEffect cuando ViewModel actualice seleccionado
                }) {
                    Text("Guardar")
                }
            }
        }
    }
}
