package com.tesis.appmovil.ui.account


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun FAQScreen(
    navController: NavController? = null,
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Preguntas frecuentes") },
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
            // Lista de preguntas frecuentes
            val faqItems = remember {
                listOf(
                    FAQItem(
                        question = "¿Cómo puedo editar la información de mi negocio?",
                        answer = "Ve a la sección 'Negocio'. Desde ahí podrás actualizar el nombre, telefono de contacto, descripción, dirección, imágenes y los horarios de atención."
                    ),
                    FAQItem(
                        question = "¿Cómo agrego un nuevo servicio a mi negocio?",
                        answer = "En la sección 'Servicios', presiona 'Agregar servicio'. Completa el nombre, precio, duración. También puedes subir una imagen opcional."
                    ),
                    FAQItem(
                        question = "¿Cómo puedo agregar un descuento u oferta a un servicio?",
                        answer = "Cuando creas o editas un servicio, encontrarás la opción 'Agregar descuento'. Actívala e ingresa el porcentaje de descuento que deseas aplicar."
                    ),
                    FAQItem(
                        question = "¿Puedo editar o eliminar un servicio que ya creé?",
                        answer = "Sí. En la lista de servicios, selecciona uno y elige 'Editar' para modificarlo o 'Eliminar' si deseas removerlo definitivamente."
                    ),
                    FAQItem(
                        question = "¿Cómo puedo cambiar mis datos personales?",
                        answer = "En la sección 'Cuenta', abre 'Mi perfil'. Ahí podrás cambiar tu nombre, correo y fecha de nacimiento. También puedes actualizar tu contraseña."
                    ),
                    FAQItem(
                        question = "¿Cómo gestiono los horarios de atención de mi negocio?",
                        answer = "En 'Negocio', selecciona la opción 'Horarios'. Podrás activar o desactivar días, definir horarios de apertura y cierre, y actualizar tu disponibilidad cuando lo desees."
                    ),
                    FAQItem(
                        question = "¿Qué pasa si un servicio no tiene oferta?",
                        answer = "Si no agregas un descuento, el servicio se mostrará de forma normal sin ninguna promoción activa."
                    ),
                    FAQItem(
                        question = "¿Puedo cambiar las imágenes del negocio después de crearlo?",
                        answer = "Sí. En 'Mi negocio', selecciona la imagen que deseas cambiar y modificala."
                    ),
                    FAQItem(
                        question = "¿Mis cambios se actualizan al instante?",
                        answer = "Sí. Cada vez que edites tu negocio, tus servicios o tu perfil, la información se actualiza inmediatamente en tu cuenta."
                    )
                )
            }


            // Mostrar cada pregunta con su acordeón
            faqItems.forEachIndexed { index, faqItem ->
                FAQAccordion(
                    question = faqItem.question,
                    answer = faqItem.answer,
                    isFirstItem = index == 0
                )
                if (index < faqItems.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// Data class para representar cada pregunta y respuesta
data class FAQItem(
    val question: String,
    val answer: String
)

@Composable
private fun FAQAccordion(
    question: String,
    answer: String,
    isFirstItem: Boolean = false
) {
    var expanded by remember { mutableStateOf(isFirstItem) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Pregunta (siempre visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = if (expanded) "Contraer" else "Expandir"
                )
            }

            // Respuesta (solo visible cuando está expandido)
            if (expanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = answer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Justify
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FAQScreenPreview() {
    MaterialTheme {
        FAQScreen()
    }
}