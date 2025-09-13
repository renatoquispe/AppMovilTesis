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
                        question = "¿Cómo encuentro servicios cerca de mí?",
                        answer = "Para encontrar servicios cerca de tu ubicación, ve a la sección 'Buscar' en la barra de navegación inferior. La aplicación usará tu ubicación actual para mostrarte los servicios disponibles en tu zona."
                    ),
                    FAQItem(
                        question = "¿Puedo buscar un servicio en otra zona o ciudad?",
                        answer = "Sí, puedes buscar servicios en cualquier zona o ciudad. En la pantalla de búsqueda, toca el campo de ubicación y escribe la ciudad o área donde deseas buscar servicios."
                    ),
                    FAQItem(
                        question = "¿Qué tipo de servicios puedo encontrar en la app?",
                        answer = "Encontrarás una variedad de servicios profesionales de cuidado personal y mucho más."
                    ),
                    FAQItem(
                        question = "¿Cómo veo las promociones u ofertas disponibles?",
                        answer = "Las promociones y ofertas aparecen en la pantalla de inicio."
                    ),
                    FAQItem(
                        question = "¿Cómo sé si la información está actualizada?",
                        answer = "Nuestra plataforma verifica regularmente la información de los Profesionales. Además, los usuarios pueden calificar y revisar los servicios, lo que ayuda a mantener la información actualizada y confiable."
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