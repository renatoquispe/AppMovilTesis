package com.tesis.appmovil.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tesis.appmovil.models.Negocio
import com.tesis.appmovil.viewmodel.HomeNegocioViewModel

@Composable
fun HomeNegocioSection(
    vm: HomeNegocioViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    // Carga inicial segura (solo si no hay datos y no está cargando)
    LaunchedEffect(state.negocios.isEmpty()) {
        if (!state.isLoading && state.negocios.isEmpty()) {
            // Usa cualquiera de las dos, según prefieras:
            // vm.cargarUno()
            vm.cargarDestacados(limit = 1)
        }
    }

    when {
        state.isLoading -> {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        state.error != null -> {
            Text(
                text = "Error: ${state.error}",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }

        state.negocios.isNotEmpty() -> {
            NegocioCardSimple(n = state.negocios.first())
        }

        else -> {
            Text(
                text = "No hay negocios para mostrar.",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun NegocioCardSimple(n: Negocio) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                n.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))

            if (!n.descripcion.isNullOrBlank()) {
                Text(n.descripcion, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(6.dp))
            }
            if (!n.direccion.isNullOrBlank()) {
                Text("Dirección: ${n.direccion}", style = MaterialTheme.typography.bodySmall)
            }
            if (!n.telefono.isNullOrBlank()) {
                Text("Tel: ${n.telefono}", style = MaterialTheme.typography.bodySmall)
            }
            if (!n.correo_contacto.isNullOrBlank()) {
                Text("Email: ${n.correo_contacto}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
