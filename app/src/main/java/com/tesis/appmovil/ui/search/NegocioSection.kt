package com.tesis.appmovil.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

    LaunchedEffect(Unit) {
        vm.cargarUno()
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
            Text(n.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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
