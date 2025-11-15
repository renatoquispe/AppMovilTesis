package com.tesis.appmovil.ui.business

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.tesis.appmovil.viewmodel.NegocioImagenViewModel
import com.tesis.appmovil.viewmodel.NegocioViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun BusinessImagesScreen(
    negocioViewModel: NegocioViewModel, // ← Agregar este parámetro
    negocioImagenViewModel: NegocioImagenViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),

    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
//    val uiState by negocioViewModel.ui.collectAsState()
    val uiState by negocioViewModel.ui.collectAsState()

    // Lanzador para seleccionar múltiples imágenes
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                selectedImages = selectedImages + uris.take(5 - selectedImages.size)
            }
        }
    )

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
            text = "Agrega imágenes",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(Modifier.height(4.dp))

        // Subtítulo
        Text(
            text = "Sube fotos que representen a tu negocio.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(Modifier.height(24.dp))

        // Sección para añadir imágenes
        Text(
            text = "Añade tus imágenes",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(Modifier.height(12.dp))

        // Botón para elegir archivo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                .clickable {
                    galleryLauncher.launch("image/*")
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Subir imagen",
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
                    append("JPG o PNG")
                }
                withStyle(style = SpanStyle(color = Color.Gray)) {
                    append(" • ")
                }
                withStyle(style = SpanStyle(color = Color.Black, fontWeight = FontWeight.Bold)) {
                    append("Dimensiones mínimas de 910x500 píxeles")
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

        // Sección de requisitos aceptables
        Text(
            text = "Aceptable",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(Modifier.height(12.dp))

        // Lista de requisitos con checkboxes - colores fijos como en Figma
        RequirementItem(checked = selectedImages.isNotEmpty(), text = "Imágenes nítidas del exterior e interior de tu establecimiento")
        RequirementItem(checked = selectedImages.size >= 2, text = "Al menos dos imágenes")
        RequirementItem(checked = selectedImages.isNotEmpty(), text = "Imágenes de alta resolución")

        Spacer(Modifier.height(16.dp))

        // Línea divisoria
        Divider(color = Color.LightGray, thickness = 1.dp)

        Spacer(Modifier.height(16.dp))

        // Sección de no aceptable
        Text(
            text = "No aceptable",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "• Imágenes borrosas o de baja calidad\n• Logotipos o marcas de agua\n• Imágenes con texto promocional",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        // IMAGEN DE EJEMPLO DE LO NO ACEPTABLE
        Spacer(Modifier.height(12.dp))

        // Reemplaza esta URL con la URL real de tu imagen
        val ejemploNoAceptableUrl = "https://malatestabarberia.com.ar/mala-testa-barberia.webp"

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(ejemploNoAceptableUrl)
                        .build()
                ),
                contentDescription = "Ejemplo de imagen no aceptable",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            // Texto explicativo superpuesto
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(8.dp)
            ) {
                Text(
                    text = "Ejemplo de imagen no aceptable: baja calidad o con marca de agua",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Mostrar imágenes seleccionadas
        if (selectedImages.isNotEmpty()) {
            Text(
                text = "Imágenes seleccionadas:",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(selectedImages.size) { index ->
                    Box(
                        modifier = Modifier.size(100.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(LocalContext.current)
                                    .data(selectedImages[index])
                                    .build()
                            ),
                            contentDescription = "Imagen seleccionada ${index + 1}",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        // Botón para eliminar imagen
                        IconButton(
                            onClick = {
                                selectedImages = selectedImages.toMutableList().apply {
                                    removeAt(index)
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .background(Color.White, CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Eliminar",
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // Botón continuar
        Button(
            onClick = {
                val negocioId = uiState.negocioCreadoId ?: uiState.seleccionado?.id_negocio
                if (negocioId != null) {
                    negocioImagenViewModel.subirImagenes( // 👈 Llamas al método de la instancia
                        context,
                        negocioId,
                        selectedImages
                    )
                    onContinue()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C1349)),
            enabled = selectedImages.size >= 2
        ) {
            Text("CONTINUAR →")
        }
    }
}

@Composable
fun RequirementItem(checked: Boolean, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        // Checkbox estilo Figma (colores fijos)
        Box(
            modifier = Modifier
                .size(20.dp)
                .border(
                    2.dp,
                    if (checked) Color(0xFF4CAF50) else Color.Gray,
                    RoundedCornerShape(4.dp)
                )
                .background(
                    if (checked) Color(0xFF4CAF50) else Color.Transparent,
                    RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Check",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black // Color fijo como en el diseño
        )
    }
}