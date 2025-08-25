package com.tesis.appmovil.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { HeaderGreeting(name = state.userName, location = state.location) }

            item { SectionTitle("Estilos cerca de ti") }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.nearby) { s -> SmallServiceCard(s) }
                }
            }

            item { SectionTitle("Servicios destacados en tu zona") }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.featured) { s -> FeaturedCard(s) }
                }
            }

            item { SectionTitle("Ofertas especiales") }
            items(state.deals) { s -> DealRowCard(s) }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun HeaderGreeting(name: String, location: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(text = "Hola, $name", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "¿Qué harás hoy?",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(Icons.Outlined.LocationOn, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text(location, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
}

@Composable
private fun SmallServiceCard(s: Service) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(220.dp)
            .height(150.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AsyncImage(
                    model = s.imageUrl,
                    contentDescription = s.title,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Column(Modifier.weight(1f)) {
                    Text(s.title, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                    Text(
                        "${s.businessName} • servicio de ${s.duration}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(onClick = { /* detalle */ }, shape = RoundedCornerShape(12.dp)) {
                    Text("S/ ${"%.2f".format(s.price)}")
                }
                FilledTonalIconButton(onClick = { /* ir */ }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.ArrowForward, contentDescription = "Ver")
                }
            }
        }
    }
}

@Composable
private fun FeaturedCard(s: Service) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.width(290.dp)) {
        Column {
            Box {
                AsyncImage(
                    model = s.imageUrl,
                    contentDescription = s.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
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
                Text(s.title, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                Text(
                    s.duration,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            text = s.tag ?: "Servicio",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        s.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    FilledTonalIconButton(onClick = { /* ver */ }) {
                        Icon(Icons.Outlined.ArrowForward, contentDescription = "Ver")
                    }
                }
            }
        }
    }
}

@Composable
private fun DealRowCard(s: Service) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = s.imageUrl,
                contentDescription = s.title,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(s.title, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                Text(
                    "${s.businessName} • ${s.duration}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                FilledTonalButton(onClick = { }, shape = RoundedCornerShape(12.dp)) {
                    Text("S/ ${"%.2f".format(s.price)}")
                }
                Spacer(Modifier.height(6.dp))
                FilledTonalIconButton(onClick = { }) {
                    Icon(Icons.Outlined.ArrowForward, contentDescription = "Ir")
                }
            }
        }
    }
}
