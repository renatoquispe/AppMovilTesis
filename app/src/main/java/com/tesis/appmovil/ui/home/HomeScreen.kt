package com.tesis.appmovil.ui.home

import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tesis.appmovil.models.Service
import com.tesis.appmovil.viewmodel.HomeViewModel


@Composable
fun HomeScreen(vm: HomeViewModel) {
    val state by vm.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: abrir asistente IA */ }) {
                Icon(Icons.Outlined.SmartToy, contentDescription = "Asistente")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { HeaderGreeting(name = state.userName, location = state.location) }

            item { SectionTitle("Ofertas especiales") }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.nearby) { s -> SmallServiceCard(s) }
                }
            }

            item { SectionTitle("Estilos cerca de tí") }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.featured) { s -> FeaturedCard(s) }
                }
            }

            item { SectionTitle("Servicios destacados en tu zona") }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.deals) { s -> DealRowCard(s) }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun HeaderGreeting(name: String, location: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = "Hola, $name",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal)
        )

        Spacer(Modifier.height(2.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "¿Qué harás hoy?",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Spacer(Modifier.width(4.dp))
                Text(location, style = MaterialTheme.typography.labelSmall)
                Icon(Icons.Outlined.LocationOn, contentDescription = null)
            }
        }
    }
}

@Composable private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
}

//@Composable
//private fun SmallServiceCard(s: Service) {
//    val purple = MaterialTheme.colorScheme.primary
//    val onPurple = MaterialTheme.colorScheme.onPrimary
//    val cardWidth = 360.dp
//    val cardHeight = 160.dp
//
//    Card(
//        shape = RoundedCornerShape(20.dp),
//        modifier = Modifier
//            .width(cardWidth)
//            .height(cardHeight)
//    ) {
//        Row(Modifier.fillMaxSize()) {
//            // LADO IZQUIERDO (morado)
//            Column(
//                modifier = Modifier
//                    .weight(1f)
//                    .fillMaxHeight()
//                    .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
//                    .background(purple)
//                    .padding(16.dp),
//                verticalArrangement = Arrangement.SpaceBetween
//            ) {
//                Column {
//                    Surface(
//                        shape = RoundedCornerShape(12.dp),
//                        color = Color.White.copy(alpha = 0.98f)
//                    ) {
//                        Text(
//                            text = "¡Tiempo limitado!",
//                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
//                            color = purple,
//                            style = MaterialTheme.typography.labelMedium,
//                            fontWeight = FontWeight.SemiBold
//                        )
//                    }
//
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    Text(
//                        text = s.title,
//                        style = MaterialTheme.typography.headlineSmall.copy(
//                            color = Color.White,
//                            fontWeight = FontWeight.Bold
//                        ),
//                        maxLines = 1
//                    )
//
//                    Spacer(modifier = Modifier.height(6.dp))
//
//                    Row(verticalAlignment = Alignment.Top) {
//                        Text(
//                            text = "Hasta un",
//                            color = Color.White.copy(alpha = 0.95f),
//                            style = MaterialTheme.typography.bodyMedium
//                        )
//                        Spacer(Modifier.width(6.dp))
//                        Text(
//                            text = "20%",
//                            color = Color.White,
//                            fontSize = 28.sp,
//                            fontWeight = FontWeight.Bold
//                        )
//                    }
//                }
//
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(
//                        imageVector = Icons.Outlined.LocationOn,
//                        contentDescription = null,
//                        tint = Color.White,
//                        modifier = Modifier.size(16.dp)
//                    )
//                    Spacer(Modifier.width(6.dp))
//                    Text(
//                        text = s.businessName,
//                        color = Color.White,
//                        style = MaterialTheme.typography.labelMedium
//                    )
//                }
//            }
//
//            // LADO DERECHO: círculo parcial
//            Box(
//                modifier = Modifier
//                    .width(160.dp)
//                    .fillMaxHeight(),
//                contentAlignment = Alignment.Center
//            ) {
////                // círculo grande desplazado → solo se ve un pedazo dentro del card
//                AsyncImage(
//                    model = s.imageUrl,
//                    contentDescription = s.title,
//                    modifier = Modifier
//                        .size(imageSize)
//                        .offset(x = imageOffset) // desplaza la imagen hacia afuera
//                        .clip(CircleShape),
//                    contentScale = ContentScale.Crop
//                )
//                // contenedor circular con borde blanco
//                Box(
//                    modifier = Modifier
//                        .size(imageSize)
//                        .offset(x = imageOffset)
//                        .clip(CircleShape)
//                        .border(4.dp, Color.White, CircleShape) // borde blanco
//                ) {
//                    AsyncImage(
//                        model = s.imageUrl,
//                        contentDescription = s.title,
//                        modifier = Modifier.fillMaxSize(), // ocupa todo el círculo
//                        contentScale = ContentScale.Crop
//                    )
//                }
//                // CTA
//                Surface(
//                    modifier = Modifier
//                        .align(Alignment.BottomEnd)
//                        .padding(end = 16.dp, bottom = 12.dp),
//                    shape = RoundedCornerShape(24.dp),
//                    tonalElevation = 4.dp,
//                    color = Color.White
//                ) {
//                    Row(
//                        modifier = Modifier
//                            .padding(horizontal = 12.dp, vertical = 6.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            text = "Obtener oferta",
//                            style = MaterialTheme.typography.labelSmall,
//                            color = purple,
//                            fontWeight = FontWeight.Medium
//                        )
//                        Spacer(Modifier.width(8.dp))
//                        Box(
//                            modifier = Modifier
//                                .size(28.dp)
//                                .clip(CircleShape)
//                                .background(purple),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Icon(
//                                imageVector = Icons.Outlined.ArrowForward,
//                                contentDescription = null,
//                                tint = onPurple,
//                                modifier = Modifier.size(16.dp)
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
@Composable
private fun SmallServiceCard(s: Service) {
    val purple = MaterialTheme.colorScheme.primary
    val onPurple = MaterialTheme.colorScheme.onPrimary
    val cardWidth = 360.dp
    val cardHeight = 160.dp

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight)
    ) {
        Row(Modifier.fillMaxSize()) {
            // LADO IZQUIERDO (morado)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                    .background(purple)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.98f)
                    ) {
                        Text(
                            text = "¡Tiempo limitado!",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = purple,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = s.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "Hasta un",
                            color = Color.White.copy(alpha = 0.95f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "20%",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = s.businessName,
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // LADO DERECHO: imagen cuadrada
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = s.imageUrl,
                    contentDescription = s.title,
                    modifier = Modifier.fillMaxSize(), // ocupa todo el espacio
                    contentScale = ContentScale.Crop
                )

                // CTA (Botón)
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 12.dp),
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 4.dp,
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Obtener oferta",
                            style = MaterialTheme.typography.labelSmall,
                            color = purple,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(purple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ArrowForward,
                                contentDescription = null,
                                tint = onPurple,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun FeaturedCard(s: Service) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val horizontalPadding = 12.dp * 2     // padding del LazyColumn
    val spacing = 8.dp                     // espaciado entre cards

    val cardWidth = (screenWidth - horizontalPadding - spacing) / 2
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(cardWidth)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                // Imagen recortada con esquinas superiores redondeadas
                AsyncImage(
                    model = s.imageUrl,
                    contentDescription = s.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )

                // Gradiente sutil en la parte inferior para legibilidad del texto
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.66f)
                                )
                            )
                        )
                )

                // Row sobre la imagen: columna de textos (izquierda) + círculo de precio (derecha)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = s.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = s.location,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Círculo con el precio dentro
                    Box(
                        modifier = Modifier
                            .size(48.dp) // tamaño del círculo
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "S/ ${s.price}", // asumo que tienes s.price
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } // fin Box (imagen + overlays)
        } // fin Column
    } // fin Card
}

@Composable
private fun DealRowCard(s: Service) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val horizontalPadding = 12.dp * 2
    val spacing = 8.dp

    val cardWidth = (screenWidth - horizontalPadding - spacing) / 2
    Card(shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(cardWidth)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = s.imageUrl,
                    contentDescription = s.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                )
                if (s.rating != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("★ ${"%.1f".format(s.rating)}", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            Column(Modifier.padding(12.dp)) {
                Text(s.businessName, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Text(
                    s.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.outlineVariant
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            text = s.tag ?: "Servicio",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Spacer(Modifier.width(8.dp))

                }
            }
        }
    }
}

//PARA VER EN SPLIT Y DESIGN
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    val vm = HomeViewModel() // usa FakeRepository por defecto
    HomeScreen(vm)
}
