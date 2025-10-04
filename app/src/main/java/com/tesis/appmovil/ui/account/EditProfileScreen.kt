package com.tesis.appmovil.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tesis.appmovil.viewmodel.UsuarioViewModel
import com.tesis.appmovil.data.remote.dto.UsuarioUpdate
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    nav: NavController,
    usuarioVM: UsuarioViewModel = viewModel()
) {
    val uiState by usuarioVM.uiState.collectAsState()
    val user = uiState.seleccionado

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf(TextFieldValue("")) }
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var birthDate by remember { mutableStateOf(TextFieldValue("")) }

    // Cargar datos en los campos cuando cambie el usuario
    LaunchedEffect(user) {
        user?.let {
            name = TextFieldValue(it.nombre ?: "")
            email = TextFieldValue(it.correo ?: "")
            birthDate = TextFieldValue(it.fechaNacimiento ?: "")
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Editar Perfil") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    val saving = uiState.mutando
                    IconButton(
                        onClick = {
                            user?.let {
                                usuarioVM.actualizarUsuario(
                                    it.idUsuario,
                                    UsuarioUpdate(
                                        nombre = name.text.trim(),
                                        correo = email.text.trim(),
                                        fechaNacimiento = birthDate.text.trim()
                                    )
                                )
                                // Navegar directo sin mostrar snackbar aquí
                                nav.popBackStack("cuenta", inclusive = false)
                            }
                        },
                        enabled = !saving
                    ) {
                        Icon(Icons.Outlined.Check, contentDescription = "Guardar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) } // 👈 importante
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = birthDate,
                onValueChange = { txt ->
                    // Permite solo números y guiones en formato yyyy-MM-dd
                    if (txt.text.matches(Regex("""\d{0,4}(-)?\d{0,2}(-)?\d{0,2}"""))) {
                        birthDate = txt
                    }
                },
                label = { Text("Fecha de nacimiento") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}