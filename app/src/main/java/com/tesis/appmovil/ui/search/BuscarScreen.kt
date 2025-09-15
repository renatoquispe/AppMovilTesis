package com.tesis.appmovil.ui.search

import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.tesis.appmovil.R
import com.tesis.appmovil.viewmodel.SearchViewModel
import com.tesis.appmovil.viewmodel.ServiceItem

/** Host del Fragment con Google Maps — robusto para reentradas */
@Composable
fun BuscarFragmentHost(modifier: Modifier = Modifier) {
    val activity = LocalContext.current as FragmentActivity
    val fm = activity.supportFragmentManager
    val tag = "buscar_fragment"
    val stableId = R.id.search_fragment_container

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            FragmentContainerView(ctx).apply {
                id = stableId
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        update = { container ->
            val existing = fm.findFragmentByTag(tag)

            // comprobaciones de estado y logging para debug
            if (existing == null) {
                android.util.Log.d("BuscarFragmentHost", "No hay fragment → se añade uno nuevo")
                fm.commit {
                    setReorderingAllowed(true)
                    add(container.id, BuscarFragment(), tag)
                }
                return@AndroidView
            }

            // ¿la view del fragment está realmente dentro del container actual?
            val isViewAttachedToThisContainer = existing.view?.parent == container

            if (isViewAttachedToThisContainer) {
                android.util.Log.d("BuscarFragmentHost", "Fragment ya está listo y su view está en el container → no hacer nada")
                return@AndroidView
            } else {
                // el fragment existía, pero su view NO está en el container actual:
                // lo removemos y añadimos uno nuevo para asegurarnos de que se cree la vista correctamente
                android.util.Log.d(
                    "BuscarFragmentHost",
                    "Fragment existe pero su view NO pertenece al container actual → remove + add nuevo fragment"
                )

                fm.commit {
                    setReorderingAllowed(true)
                    // remove el fragment viejo (desmonta su view) y añade uno fresco
                    remove(existing)
                    add(container.id, BuscarFragment(), tag)
                }
            }
        }
    )
}


/** Pantalla: mapa al fondo + overlay con controles */
@Composable
fun BuscarScreen(vm: SearchViewModel = viewModel()) {
    Box(Modifier.fillMaxSize()) {
        // 1) Mapa al fondo
        BuscarFragmentHost(Modifier.fillMaxSize())

        // 2) Overlay con buscador + lista
        ControlsOverlay(
            query = vm.state.query,
            onQueryChange = vm::onQueryChange,
            popular = vm.state.popular,
            onClickService = { /* TODO abrir detalle */ },
            onClickIA = { /* TODO IA */ }
        )
    }
}

/* ---------- resto de composables (los tuyos) ---------- */

@Composable
private fun ControlsOverlay(
    query: String,
    onQueryChange: (String) -> Unit,
    popular: List<ServiceItem>,
    onClickService: (ServiceItem) -> Unit,
    onClickIA: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp)),
            placeholder = { Text("Busque el mejor servicio aquí") },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(28.dp)
        )

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp) // evita solape con bottom bar
        ) {
            item { SectionCardTitle("Servicios más buscados en tu zona") }
            items(popular, key = { it.id }) { item ->
                ServiceCard(
                    item = item,
                    onClick = { onClickService(item) },
                    onClickIA = onClickIA
                )
            }
        }
    }
}

@Composable
private fun SectionCardTitle(title: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
private fun ServiceCard(
    item: ServiceItem,
    onClick: () -> Unit,
    onClickIA: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 4.dp,
        onClick = onClick
    ) {
        Column(Modifier.fillMaxWidth()) {

            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
                contentScale = ContentScale.Crop
            )

            Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val openText = if (item.isOpenNow) "Abierto ahora" else "Cerrado"
                Text(
                    text = "$openText · ${item.schedule}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Corte/Haircut", style = MaterialTheme.typography.bodySmall)
                        Text(item.price1, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text("Corte + Ritual de barba", style = MaterialTheme.typography.bodySmall)
                        Text(item.price2, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text("Ver más", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }

                    FilledTonalButton(
                        onClick = onClickIA,
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("IA", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
