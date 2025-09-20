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
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
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
        modifier = Modifier.imePadding() // <- mueve toda la pantalla con el teclado
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            // Flecha atrás
            IconButton(onClick = { onBack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Atrás"
                )
            }

            Spacer(Modifier.height(8.dp))

            Text("Horarios de Atención", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Indica los días y horas en que tu negocio atiende",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(16.dp))

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

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { onContinue() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C1349))
            ) {
                Text("CONTINUAR →")
            }
        }

        // BottomSheet para editar horario
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
                    .imePadding()              // mueve el modal con el teclado
                    .navigationBarsPadding()   // ajusta por la barra de navegación
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()), // permite scroll si el teclado tapa
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Configurar horario de ${selectedDay!!.day}")
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = selectedDay!!.openTime.value,
                        onValueChange = { selectedDay!!.openTime.value = it },
                        label = { Text("Hora de inicio") }
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = selectedDay!!.closeTime.value,
                        onValueChange = { selectedDay!!.closeTime.value = it },
                        label = { Text("Hora de cierre") }
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

