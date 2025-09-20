package com.tesis.appmovil.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * “Servicios más buscados en tu zona”
 *
 * NOTA: para evitar los errores con `idNegocio` (porque cambia entre modelos/DTOs),
 * aquí no usamos el id. La lista usa una key derivada del índice y del nombre,
 * y la imagen llega mapeada por nombre.
 */
@Composable
fun MasBuscadosSection(
    title: String,
    negocios: List<com.tesis.appmovil.models.Negocio>,
    /** Mapa: nombre del negocio -> url de imagen a mostrar */
    imageByNombre: Map<String, String> = emptyMap(),
    /** Qué hacer al tocar un negocio (se envía el propio objeto) */
    onClick: (com.tesis.appmovil.models.Negocio) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(
                items = negocios,
                key = { index, item -> "${item.nombre}#$index" } // clave estable sin depender de id
            ) { _, negocio ->
                val imagen = imageByNombre[negocio.nombre]
                    ?: "https://via.placeholder.com/96x96.png?text=IA"

                NegocioCard(
                    nombre = negocio.nombre,
                    direccion = negocio.direccion ?: "",
                    imagenUrl = imagen,
                    onClick = { onClick(negocio) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun NegocioCard(
    nombre: String,
    direccion: String,
    imagenUrl: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = imagenUrl,
                contentDescription = nombre,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = direccion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                Text(
                    text = "IA",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF7A3FFF)
                )
            }
        }
    }
}
