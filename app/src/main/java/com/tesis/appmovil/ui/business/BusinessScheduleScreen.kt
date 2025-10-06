package com.tesis.appmovil.ui.business

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tesis.appmovil.viewmodel.NegocioViewModel
import kotlinx.coroutines.launch

// Modelo de cada día
data class DaySchedule(
    val day: String,
    var isOpen: MutableState<Boolean> = mutableStateOf(false),
    var openTime: MutableState<String> = mutableStateOf("09:00"),
    var closeTime: MutableState<String> = mutableStateOf("18:00")
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessScheduleScreen(
    negocioViewModel: NegocioViewModel, // ← Agrega el ViewModel como parámetro
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // Obtener el ID del negocio creado en las pantallas anteriores
    val negocioState by negocioViewModel.ui.collectAsState()
    val idNegocio = negocioState.negocioCreadoId

    val days = remember {
        mutableStateListOf(
            DaySchedule("Lunes"),
            DaySchedule("Martes"),
            DaySchedule("Miércoles"),
            DaySchedule("Jueves"),
            DaySchedule("Viernes"),
            DaySchedule("Sábado"),
            DaySchedule("Domingo")
        )
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var selectedDay by remember { mutableStateOf<DaySchedule?>(null) }

    Scaffold(
        modifier = Modifier.imePadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            // Flecha atrás
            IconButton(onClick = { onBack() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Atrás")
            }

            Spacer(Modifier.height(8.dp))

            Text("Horarios de Atención", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Indica los días y horas en que tu negocio atiende",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(16.dp))

            // Lista de días (tu código existente)
            days.forEach { day ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = day.isOpen.value,
                            onCheckedChange = { checked -> day.isOpen.value = checked }
                        )
                        Column {
                            Text(day.day, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                if (day.isOpen.value) "Abierto" else "Cerrado",
                                color = if (day.isOpen.value) Color.Green else Color.Red
                            )
                        }
                    }

                    if (day.isOpen.value) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${day.openTime.value}-${day.closeTime.value}")
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = {
                                selectedDay = day
                                coroutineScope.launch { sheetState.show() }
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Editar horario")
                            }
                        }
                    }
                }
            }

            // Mostrar ID del negocio (debug)
            if (idNegocio != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Creando horarios para negocio ID: $idNegocio",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(24.dp))

            // Estado de carga
            val isLoading = negocioState.mutando

            Button(
                onClick = {
                    if (idNegocio != null) {
                        scope.launch {
                            val result = negocioViewModel.crearHorarios(
                                idNegocio = idNegocio,
                                horarios = days
                            )

                            if (result.isSuccess) {
                                println("✅ Horarios guardados exitosamente")
                                onContinue() // Navegar a la siguiente pantalla
                            } else {
                                println("❌ Error al guardar horarios: ${result.exceptionOrNull()?.message}")
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C1349)),
                enabled = idNegocio != null && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("CONTINUAR →")
                }
            }

            // Mostrar errores
            if (negocioState.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Error: ${negocioState.error}",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (idNegocio == null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Error: No se encontró el ID del negocio. Vuelve a la pantalla anterior.",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // BottomSheet para editar horario (tu código existente)
        if (selectedDay != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    coroutineScope.launch {
                        sheetState.hide()
                        selectedDay = null
                    }
                },
                sheetState = sheetState,
                modifier = Modifier
                    .imePadding()
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Configurar horario de ${selectedDay!!.day}")
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = selectedDay!!.openTime.value,
                        onValueChange = { selectedDay!!.openTime.value = it },
                        label = { Text("Hora de inicio (HH:MM)") },
                        placeholder = { Text("09:00") }
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = selectedDay!!.closeTime.value,
                        onValueChange = { selectedDay!!.closeTime.value = it },
                        label = { Text("Hora de cierre (HH:MM)") },
                        placeholder = { Text("18:00") }
                    )

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                selectedDay = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("GUARDAR")
                    }
                }
            }
        }
    }
}
