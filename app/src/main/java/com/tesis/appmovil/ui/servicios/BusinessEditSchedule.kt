package com.tesis.appmovil.ui.business

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tesis.appmovil.viewmodel.HorarioUi
import com.tesis.appmovil.viewmodel.HorarioViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.Normalizer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessEditSchedule(
    negocioId: Int,
    onBack: () -> Unit = {},
    vm: HorarioViewModel = viewModel()
) {
    val ui by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // --- helpers ---
    fun normalizeKey(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
            .lowercase()

    val diasSemana = remember {
        listOf("Lunes","Martes","Miércoles","Jueves","Viernes","Sábado","Domingo")
    }
    val diasKeys = remember { diasSemana.associateWith { normalizeKey(it) } }

    // Fuente de verdad visual
    val stateMap = remember(negocioId) { mutableStateMapOf<String, HorarioUi>() }
    var firstSyncDone by remember(negocioId) { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Al entrar: limpiar y cargar
    LaunchedEffect(negocioId) {
        stateMap.clear()
        firstSyncDone = false
        vm.obtenerHorarios(negocioId)
    }

    // Sincronizar con backend cuando termina la carga
    LaunchedEffect(ui.horarios, ui.isLoading) {
        if (ui.isLoading) return@LaunchedEffect

        stateMap.clear()

        val byKey = ui.horarios.associateBy { normalizeKey(it.diaSemana) }
        diasSemana.forEach { dia ->
            val key = diasKeys.getValue(dia)
            val found = byKey[key]
            val item = if (found != null) {
                found.copy(
                    horaApertura = found.horaApertura.take(5),
                    horaCierre   = found.horaCierre.take(5)
                    // habilitado viene ya mapeado en el VM (estado_auditoria == 1)
                )
            } else {
                HorarioUi(
                    id = 0,
                    diaSemana = dia,
                    horaApertura = "09:00",
                    horaCierre = "18:00",
                    habilitado = false
                )
            }
            stateMap[dia] = item
        }

        firstSyncDone = true
    }

    // Derivar SIEMPRE de stateMap (sin remember) para que refleje lo último
    val horariosMostrados = diasSemana.map { dia ->
        stateMap[dia] ?: HorarioUi(0, dia, "09:00", "18:00", false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Horarios del negocio") },
                navigationIcon = {
                    IconButton(onClick = { if (!isSaving) onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isSaving) return@FloatingActionButton
                    isSaving = true

                    scope.launch {
                        // Tomar lo que se ve en pantalla
                        val aGuardar = diasSemana.mapNotNull { stateMap[it] }

                        // Particionar para asegurar ORDEN y evitar reactivaciones
                        val toDisable = aGuardar.filter { it.id != 0 && !it.habilitado }
                        val toCreate  = aGuardar.filter { it.id == 0 &&  it.habilitado }
                        val toEnable  = aGuardar.filter { it.id != 0 && it.habilitado }
                            .filter { h -> ui.horarios.find { it.id == h.id }?.habilitado == false }
                        val toUpdate  = aGuardar.filter { it.id != 0 && it.habilitado }
                            .filter { h -> ui.horarios.find { it.id == h.id }?.habilitado == true }

                        // 1) Desactivar primero
                        for (h in toDisable) {
                            vm.desactivarHorario(h.id)
                            delay(30)
                        }

                        // 2) Crear nuevos habilitados (posicional para evitar mismatch de nombres)
                        for (h in toCreate) {
                            vm.crearHorario(
                                negocioId,
                                h.diaSemana,
                                h.horaApertura.take(5),
                                h.horaCierre.take(5)
                            )
                            delay(30)
                        }

                        // 3) Activar los existentes que estaban en 0
                        for (h in toEnable) {
                            vm.activarHorario(h.id)
                            delay(30)
                        }

                        // 4) Actualizar horas de los ya activos
                        for (h in toUpdate) {
                            vm.actualizarHorario(
                                h.copy(
                                    horaApertura = h.horaApertura.take(5),
                                    horaCierre   = h.horaCierre.take(5)
                                )
                            )
                            delay(30)
                        }

                        // Refrescar desde backend y bloquear UI hasta tener la foto real
                        firstSyncDone = false
                        vm.obtenerHorarios(negocioId)

                        snackbarHostState.showSnackbar("Horarios guardados")
                        isSaving = false
                    }
                },
                containerColor = if (isSaving) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Save, contentDescription = if (isSaving) "Guardando…" else "Guardar")
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                !firstSyncDone -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                ui.error != null && stateMap.isEmpty() -> {
                    Text(
                        text = ui.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(horariosMostrados, key = { it.diaSemana }) { item ->
                            HorarioCard(
                                horario = item,
                                onHorarioEdit = { nuevo ->
                                    stateMap[nuevo.diaSemana] = nuevo.copy(
                                        horaApertura = nuevo.horaApertura.take(5),
                                        horaCierre   = nuevo.horaCierre.take(5)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Overlay de guardado
            if (isSaving) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.35f))
                ) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
private fun HorarioCard(
    horario: HorarioUi,
    onHorarioEdit: (HorarioUi) -> Unit
) {
    var apertura by remember(horario.diaSemana) { mutableStateOf(horario.horaApertura.take(5)) }
    var cierre   by remember(horario.diaSemana) { mutableStateOf(horario.horaCierre.take(5)) }
    var habilitado by remember(horario.diaSemana) { mutableStateOf(horario.habilitado) }

    // Re-sincroniza si el padre actualiza (tras recarga real del backend)
    LaunchedEffect(horario.id, horario.horaApertura, horario.horaCierre, horario.habilitado) {
        apertura   = horario.horaApertura.take(5)
        cierre     = horario.horaCierre.take(5)
        habilitado = horario.habilitado
    }

    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = horario.diaSemana, style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = habilitado,
                    onCheckedChange = { checked ->
                        habilitado = checked
                        onHorarioEdit(
                            horario.copy(
                                horaApertura = apertura,
                                horaCierre   = cierre,
                                habilitado   = checked
                            )
                        )
                    }
                )
            }

            if (habilitado) {
                OutlinedTextField(
                    value = apertura,
                    onValueChange = {
                        apertura = it.take(5)
                        onHorarioEdit(
                            horario.copy(
                                horaApertura = apertura,
                                horaCierre   = cierre,
                                habilitado   = true
                            )
                        )
                    },
                    label = { Text("Hora apertura (HH:mm)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = cierre,
                    onValueChange = {
                        cierre = it.take(5)
                        onHorarioEdit(
                            horario.copy(
                                horaApertura = apertura,
                                horaCierre   = cierre,
                                habilitado   = true
                            )
                        )
                    },
                    label = { Text("Hora cierre (HH:mm)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
