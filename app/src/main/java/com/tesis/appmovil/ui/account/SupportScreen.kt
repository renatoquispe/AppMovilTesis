package com.tesis.appmovil.ui.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    navController: NavController? = null,
    onBackClick: () -> Unit = {}
) {
    var selectedSubject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val subjectOptions = listOf(
        "Problema técnico",
        "Sugerencia",
        "Reportar error",
        "Otro"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Soporte") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController?.popBackStack() ?: onBackClick()
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Volver atrás"
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
            // Información de contacto directo
            SupportOption(
                icon = Icons.Outlined.Email,
                title = "¿Necesitas ayuda?",
                subtitle = "Escríbenos a: soporte@tuapp.com",
                onClick = {
                    // Aquí podrías abrir el cliente de email
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Te responderemos en 24-48 horas",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Formulario simple
            Text(
                "O envíanos un mensaje directo:",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Selector de asunto con menú desplegable - CORREGIDO
            Box(modifier = Modifier.fillMaxWidth()) {
                // Usamos ExposedDropdownMenuBox para una experiencia más fluida
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedSubject,
                        onValueChange = {},
                        label = { Text("Asunto") },
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor() // Importante para anclar el menú
                            .fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.exposedDropdownSize() // se adapta al ancho del TextField
                    ) {
                        subjectOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedSubject = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

//            Box(modifier = Modifier.fillMaxWidth()) {
//                OutlinedTextField(
//                    value = selectedSubject,
//                    onValueChange = {},
//                    label = { Text("Asunto") },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clickable { expanded = true },
//                    readOnly = true,
//                    trailingIcon = {
//                        Icon(
//                            imageVector = Icons.Outlined.ArrowBack,
//                            contentDescription = "Desplegar opciones",
//                            modifier = Modifier.rotate(if (expanded) 90f else 270f)
//                        )
//                    },
//                    shape = RoundedCornerShape(12.dp)
//                )
//
//                // Menú desplegable
//                DropdownMenu(
//                    expanded = expanded,
//                    onDismissRequest = { expanded = false },
//                    modifier = Modifier.fillMaxWidth(0.9f)
//                ) {
//                    subjectOptions.forEach { option ->
//                        DropdownMenuItem(
//                            text = { Text(option) },
//                            onClick = {
//                                selectedSubject = option
//                                expanded = false
//                            }
//                        )
//                    }
//                }
//            }

            Spacer(modifier = Modifier.height(12.dp))

            // Campo de mensaje
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Mensaje") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón enviar
            Button(
                onClick = {
                    // Lógica para enviar el mensaje
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = selectedSubject.isNotEmpty() && message.isNotEmpty()
            ) {
                Text("Enviar mensaje")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Enlace a FAQs
            SupportOption(
                icon = Icons.Outlined.QuestionAnswer,
                title = "Ver Preguntas Frecuentes",
                onClick = {
                    navController?.navigate("faq")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Información de versión
            SupportOption(
                icon = Icons.Outlined.Info,
                title = "Versión de la app",
                subtitle = "1.0.0"
            )
        }
    }
}

@Composable
private fun SupportOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SupportScreenPreview() {
    MaterialTheme {
        SupportScreen()
    }
}