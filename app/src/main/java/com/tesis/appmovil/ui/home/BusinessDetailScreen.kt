package com.tesis.appmovil.ui.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessDetailScreen(
    navController: NavController? = null,
    businessId: Int, // ← CAMBIADO a Int según tu BD
    onBackClick: () -> Unit = {}
) {
    var business by remember { mutableStateOf<Business?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(businessId) {
        business = loadBusinessData(businessId)
        loading = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detalles del Negocio") },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() ?: onBackClick() }) {
                        Icon(Icons.Outlined.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (business != null) {
            BusinessDetailContent(business = business!!, padding = padding)
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Negocio no encontrado")
            }
        }
    }
}

@Composable
private fun BusinessDetailContent(business: Business, padding: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        // Sección de imágenes del negocio
        item { BusinessGallerySection(business.images) }

        // Sección de información principal
        item { BusinessInfoSection(business) }

        // Sección de servicios
        item { ServicesSection(business.services) }

        // Sección de horarios
        item { BusinessHoursSection(business.hours) }

        // Sección de reseñas
        item { ReviewsSection(business.reviews) }

        // Sección "Acerca de"
        item { AboutSection(business.descripcion) }
    }
}

// MODELOS BASADOS EN TU BD
data class Business(
    val id_negocio: Int,
    val nombre: String,
    val descripcion: String,
    val direccion: String,
    val telefono: String?,
    val correo_contacto: String?,
    val categoria: String,
    val ubicacion: String,
    val rating: Double,
    val services: List<Service>,
    val reviews: List<Review>,
    val hours: List<BusinessHour>,
    val images: List<String>
)

data class Service(
    val id_servicio: Int,
    val nombre: String,
    val descripcion: String?,
    val precio: Double,
    val duracion_minutos: Int
)

data class Review(
    val id_resena: Int,
    val usuario: String,
    val calificacion: Int,
    val comentario: String,
    val fecha: String,
    val imagenes: List<String>
)

data class BusinessHour(
    val dia_semana: String,
    val hora_apertura: String,
    val hora_cierre: String
)

// FUNCIÓN TEMPORAL DE CARGA
private fun loadBusinessData(businessId: Int): Business {
    return Business(
        id_negocio = businessId,
        nombre = "Barbería Cúspide",
        descripcion = "Barbería especializada en cortes modernos y tradicionales",
        direccion = "Av. Ejemplo 123, Lima",
        telefono = "+51 999 999 999",
        correo_contacto = "contacto@barberiacuspide.com",
        categoria = "Barbería",
        ubicacion = "Lima, Perú",
        rating = 4.5,
        services = listOf(
            Service(1, "Corte de Cabello", "Corte moderno con técnicas actualizadas", 25.0, 30),
            Service(2, "Afeitado Clásico", "Afeitado tradicional con navaja", 15.0, 20),
            Service(3, "Corte y Barba", "Combo completo de corte y arreglo de barba", 35.0, 45)
        ),
        reviews = listOf(
            Review(1, "Juan Pérez", 5, "Excelente servicio y atención", "2023-10-15", emptyList()),
            Review(2, "María García", 4, "Buen trabajo, volveré pronto", "2023-10-10", emptyList())
        ),
        hours = listOf(
            BusinessHour("Lunes a Viernes", "09:00", "19:00"),
            BusinessHour("Sábado", "09:00", "17:00"),
            BusinessHour("Domingo", "Cerrado", "")
        ),
        images = listOf(
            "https://ejemplo.com/imagen1.jpg",
            "https://ejemplo.com/imagen2.jpg",
            "https://ejemplo.com/imagen3.jpg"
        )
    )
}

// COMPONENTES DE LA INTERFAZ (los demás componentes que ya te mostré)
@Composable
private fun BusinessGallerySection(images: List<String>) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(images) { imageUrl ->
            AsyncImage(
                model = imageUrl,
                contentDescription = "Imagen del negocio",
                modifier = Modifier
                    .fillMaxHeight()
                    .width(300.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun BusinessInfoSection(business: Business) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = business.nombre,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Rating
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Star, "Rating", tint = Color(0xFFFFD700))
            Spacer(modifier = Modifier.width(4.dp))
            Text("${business.rating} • ${business.categoria}")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dirección
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.LocationOn, "Ubicación")
            Spacer(modifier = Modifier.width(4.dp))
            Text(business.direccion)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Contacto
        business.telefono?.let { phone ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Phone, "Teléfono")
                Spacer(modifier = Modifier.width(4.dp))
                Text(phone)
            }
        }
    }
}
// ... (continuación del código anterior)

@Composable
private fun ServicesSection(services: List<Service>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Servicios",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        services.forEach { service ->
            ServiceItem(service = service)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ServiceItem(service: Service) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                service.descripcion?.let { description ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "S/ ${service.precio}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${service.duracion_minutos} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BusinessHoursSection(hours: List<BusinessHour>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Horarios de Atención",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        hours.forEach { hour ->
            BusinessHourItem(hour = hour)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun BusinessHourItem(hour: BusinessHour) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = hour.dia_semana,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        if (hour.hora_apertura.isNotEmpty() && hour.hora_cierre.isNotEmpty()) {
            Text(
                text = "${hour.hora_apertura} - ${hour.hora_cierre}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = "Cerrado",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun ReviewsSection(reviews: List<Review>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Reseñas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (reviews.isEmpty()) {
            Text(
                text = "No hay reseñas todavía",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        } else {
            reviews.forEach { review ->
                ReviewItem(review = review)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ReviewItem(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header de la reseña
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar del usuario
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = review.usuario.take(1).uppercase(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.usuario,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    StarRating(rating = review.calificacion)
                }

                Text(
                    text = review.fecha,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Comentario
            Text(
                text = review.comentario,
                style = MaterialTheme.typography.bodyMedium
            )

            // Imágenes de la reseña (si existen)
            if (review.imagenes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(review.imagenes) { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Imagen de reseña",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StarRating(rating: Int) {
    Row {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                contentDescription = "Rating",
                tint = if (i <= rating) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AboutSection(description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Acerca de",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp
        )
    }
}

// Botones de contacto flotantes
@Composable
private fun ContactFloatingButtons(business: Business) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(60.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Botón de llamar
        Button(
            onClick = { /* Abrir teléfono */ },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Phone,
                contentDescription = "Llamar",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Llamar")
        }

        // Botón de WhatsApp
        Button(
            onClick = { /* Abrir WhatsApp */ },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF25D366),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Chat,
                contentDescription = "WhatsApp",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("WhatsApp")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BusinessDetailScreenPreview() {
    MaterialTheme {
        BusinessDetailScreen(businessId = 1)
    }
}
