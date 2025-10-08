package com.tesis.appmovil.ui.business

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.tesis.appmovil.viewmodel.NegocioViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun BusinessDocumentsScreen(
    negocioViewModel: NegocioViewModel, // ← Agregar este parámetro
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedDocuments by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Estado para el permiso de lectura de almacenamiento
    val permissionState = rememberPermissionState(
        permission = Manifest.permission.READ_EXTERNAL_STORAGE
    )

    // Lanzador para seleccionar documentos PDF
    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                selectedDocuments = selectedDocuments + uris.take(3 - selectedDocuments.size)
            }
        }
    )
    LaunchedEffect(permissionState.status) {
        if (!permissionState.status.isGranted && !permissionState.status.shouldShowRationale) {
            println("⚠️ Permiso de almacenamiento no concedido")
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Flecha atrás
        IconButton(onClick = { onBack() }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Atrás"
            )
        }

        Spacer(Modifier.height(8.dp))

        // Título
        Text(
            text = "Documentos de Verificación",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(Modifier.height(4.dp))

        // Subtítulo
        Text(
            text = "Sube los documentos legales de tu negocio para mayor confianza.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(Modifier.height(24.dp))

        // Sección para añadir documentos
        Text(
            text = "Añade tus documentos",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(Modifier.height(12.dp))

        // Botón para elegir archivo - estilo similar a Figma
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                .clickable {
                    if (permissionState.status.isGranted) {
                        documentLauncher.launch("application/pdf")
                    } else {
                        permissionState.launchPermissionRequest()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = "Subir documento",
                    tint = Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Elige un archivo",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Gray
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Información sobre tipos de archivo - estilo similar a Figma
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.Gray)) {
                    append("Tipo de archivo: ")
                }
                withStyle(style = SpanStyle(color = Color.Black, fontWeight = FontWeight.Bold)) {
                    append("PDF, JPG, PNG")
                }
                withStyle(style = SpanStyle(color = Color.Gray)) {
                    append(" • ")
                }
                withStyle(style = SpanStyle(color = Color.Black, fontWeight = FontWeight.Bold)) {
                    append("Tamaño máximo 10MB")
                }
            },
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(24.dp))

        // Línea divisoria
        Divider(color = Color.LightGray, thickness = 1.dp)

        Spacer(Modifier.height(16.dp))

        // Sección de documentos seleccionados
        if (selectedDocuments.isNotEmpty()) {
            Text(
                text = "Documentos seleccionados:",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(Modifier.height(12.dp))

            // Lista de documentos seleccionados
            selectedDocuments.forEachIndexed { index, uri ->
                DocumentItem(
                    fileName = uri.lastPathSegment ?: "Documento ${index + 1}",
                    onRemove = {
                        selectedDocuments = selectedDocuments.toMutableList().apply {
                            removeAt(index)
                        }
                    }
                )
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(24.dp))
        }

        // Botones en la parte inferior
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Botón continuar (solo visible cuando hay documentos)
            if (selectedDocuments.isNotEmpty()) {
                Button(
                    onClick = { onContinue() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C1349))
                ) {
                    Text("CONTINUAR →")
                }
            }

            // Botón omitir (siempre visible)
            TextButton(
                onClick = { onSkip() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "OMITIR →",
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun DocumentItem(fileName: String, onRemove: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.LightGray.copy(alpha = 0.1f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = "Documento",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Eliminar documento",
                    tint = Color.Red,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}