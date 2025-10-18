package com.tesis.appmovil.ui.business

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tesis.appmovil.viewmodel.NegocioViewModel
import kotlinx.coroutines.launch

private const val PHONE_LEN = 9

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessContactInfoScreen(
    negocioViewModel: NegocioViewModel,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val focus = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val ui by negocioViewModel.ui.collectAsState()
    val idNegocio = remember(ui) {
        ui.tryInt("negocioCreadoId")
            ?: ui.tryObj("seleccionado")?.let { sel ->
                sel.tryInt("idNegocio") ?: sel.tryInt("id")
            }
    }
    val isLoadingVm = ui.tryBool("mutando") ?: false

    // Estados locales de entrada
    var telefono by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var guardando by rememberSaveable { mutableStateOf(false) }
    var errorGlobal by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(ui.seleccionado) {
        ui.seleccionado?.let { n ->
            if (telefono.isBlank()) telefono = n.telefono.orEmpty()
            // Ajusta el campo de correo según tu modelo (camelCase o snake_case)
            if (correo.isBlank())   correo   = (try {
                // intenta varias convenciones comunes
                @Suppress("UNCHECKED_CAST")
                (n.javaClass.getDeclaredField("correoContacto").apply { isAccessible = true }.get(n) as? String)
                    ?: (n.javaClass.getDeclaredField("correo_contacto").apply { isAccessible = true }.get(n) as? String)
            } catch (_: Throwable) { null } ).orEmpty()
            if (descripcion.isBlank()) descripcion = n.descripcion.orEmpty()
        }
    }

    // Validaciones
    val telValido = telefono.length == PHONE_LEN
    val correoValido = correo.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(correo).matches()
    val formOk = idNegocio != null && !guardando && !isLoadingVm && telValido && correoValido


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contacto del negocio") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Celular (solo números)
            OutlinedTextField(
                value = telefono,
                onValueChange = { nuevo ->
                    telefono = nuevo.filter(Char::isDigit).take(PHONE_LEN)
                },
                label = { Text("WhatsApp/Celular") },
                placeholder = { Text("9XXXXXXXX") },
                singleLine = true,
                isError = telefono.isNotEmpty() && !telValido,
                supportingText = {
                    if (telefono.isNotEmpty() && !telValido) {
                        Text("Debe tener $PHONE_LEN dígitos (solo números).")
                    } else {
                        Text("${telefono.length}/$PHONE_LEN")
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors()
            )

            // Correo
            OutlinedTextField(
                value = correo,
                onValueChange = { nuevo ->
                    correo = nuevo.filter { !it.isWhitespace() }
                },
                label = { Text("Correo electrónico") },
                placeholder = { Text("nombre@dominio.com") },
                singleLine = true,
                isError = correo.isNotEmpty() && !correoValido,
                supportingText = {
                    if (correo.isNotEmpty() && !correoValido) {
                        Text("Ingresa un correo válido, p. ej. nombre@dominio.com")
                    }
                },
                trailingIcon = {
                    if (correo.isNotEmpty()) {
                        IconButton(onClick = { correo = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Borrar correo")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors()
            )

            // Descripción (multilínea)
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                placeholder = { Text("Breve descripción de tu negocio…") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors()
            )

            errorGlobal?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    errorGlobal = null
                    focus.clearFocus()
                    val id = idNegocio
                    if (id == null) {
                        errorGlobal = "No se encontró el ID del negocio."
                        return@Button
                    }
                    guardando = true
                    scope.launch {
                        try {
                            // Guardar en BD (NO tocar esta llamada si ya existe en tu VM)
                            negocioViewModel.actualizarContacto(
                                idNegocio = id,
                                telefono = telefono,
                                correo = correo,
                                descripcion = descripcion
                            )
                            onContinue()
                        } catch (e: Exception) {
                            errorGlobal = e.message ?: "Error al guardar."
                        } finally {
                            guardando = false
                        }
                    }
                },
                enabled = formOk,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (guardando || isLoadingVm) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar y continuar")
                }
            }
        }
    }
}

private fun Any?.tryObj(field: String): Any? = try {
    val f = this?.javaClass?.getDeclaredField(field)
    f?.isAccessible = true
    f?.get(this)
} catch (_: Throwable) { null }

private fun Any?.tryInt(field: String): Int? = try {
    val f = this?.javaClass?.getDeclaredField(field)
    f?.isAccessible = true
    (f?.get(this) as? Number)?.toInt()
} catch (_: Throwable) { null }

private fun Any?.tryBool(field: String): Boolean? = try {
    val f = this?.javaClass?.getDeclaredField(field)
    f?.isAccessible = true
    f?.get(this) as? Boolean
} catch (_: Throwable) { null }