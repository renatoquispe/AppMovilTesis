@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.tesis.appmovil.ui.search

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

/** Host del Fragment con Google Maps — prácticamente igual */
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
            if (existing == null) {
                fm.commit {
                    setReorderingAllowed(true)
                    add(container.id, BuscarFragment(), tag)
                }
                return@AndroidView
            }
            val isViewAttachedToThisContainer = existing.view?.parent == container
            if (isViewAttachedToThisContainer) return@AndroidView
            fm.commit {
                setReorderingAllowed(true)
                remove(existing)
                add(container.id, BuscarFragment(), tag)
            }
        }
    )
}

/** Pantalla principal mejorada (sencilla y estable) */
@Composable
fun BuscarScreen(vm: SearchViewModel = viewModel()) {
    Box(Modifier.fillMaxSize()) {
        // 1) Mapa (fragment)
        BuscarFragmentHost(Modifier.fillMaxSize())

        // 2) Barra de búsqueda superior (sin el botón de 3 rayas)
        TopSearchBar(
            query = vm.state.query,
            onQueryChange = vm::onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(top = 12.dp)
                .zIndex(1f)
                .align(Alignment.TopCenter)
        )

        // 3) Panel inferior con resultados
        ResultsBottomSheet(
            popular = vm.state.popular,
            onClickService = { /* TODO abrir detalle */ },
            onClickIA = { /* TODO IA */ },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f)    // ajusta este valor para mover la sheet
                .align(Alignment.BottomCenter)
                .padding(horizontal = 6.dp, vertical = 8.dp)
                .zIndex(0.9f)
        )
    }
}

/* ---------- TopSearchBar: sin menu y placeholder con color explícito ---------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopSearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Eliminado el botón de 3 rayas según pediste.

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp), // <- tamaño del texto de entrada
            placeholder = {
                Text(
                    "Busque el mejor servicio aquí",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp // <- tamaño del placeholder (coincide con textStyle)
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            )
        )
    }
}


/* ---------- ResultsBottomSheet (idéntico a lo propuesto antes) ---------- */
@Composable
private fun ResultsBottomSheet(
    popular: List<ServiceItem>,
    onClickService: (ServiceItem) -> Unit,
    onClickIA: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
        color = Color.White,
        tonalElevation = 12.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // handle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0x12000000))
                )
            }

            Text(
                text = "Servicios más buscados en tu zona",
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            if (popular.isNotEmpty()) {
                LargeFeaturedCard(
                    item = popular.first(),
                    onClick = { onClickService(popular.first()) },
                    onClickIA = onClickIA,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                )
            }

            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentPadding = PaddingValues(bottom = 18.dp)
            ) {
                items(if (popular.size > 1) popular.drop(1) else emptyList(), key = { it.id }) { item ->
                    CompactRow(item = item, onClick = { onClickService(item) }, onClickIA = onClickIA)
                }
            }
        }
    }
}

/* ---------- Tarjetas: LargeFeaturedCard / CompactRow (rating eliminado) ---------- */

@Composable
private fun LargeFeaturedCard(
    item: ServiceItem,
    onClick: () -> Unit,
    onClickIA: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(width = 140.dp, height = 96.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name.uppercase(), maxLines = 1, overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    )

                    Spacer(Modifier.height(6.dp))

                    Text("${if (item.isOpenNow) "Abierto ahora" else "Cerrado"} · ${item.schedule}",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Place, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(6.dp))
                        Text("5 km", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }

                    Spacer(Modifier.height(8.dp))

                    Row {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Corte/Haircut", style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(6.dp))
                            Text("Corte + Ritual de barba", style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(6.dp))
                            Text("Corte + lavado", style = MaterialTheme.typography.bodySmall)
                        }
                        Column {
                            Text(item.price1, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            Spacer(Modifier.height(8.dp))
                            Text(item.price2, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            Spacer(Modifier.height(8.dp))
                            Text("S/65", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    Text("Ver más", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary))
                }
            }

            // --- RATING ELIMINADO: si en un futuro quieres volver a mostrar rating,
            // puedes añadir una propiedad rating: Double? a ServiceItem y descomentar
            // el bloque que mostraba la estrella y el valor. Por ahora lo quitamos.

            FloatingActionButton(
                onClick = onClickIA,
                containerColor = Color(0xFFEBDEFB),
                contentColor = Color(0xFF4A2548),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .size(46.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-10).dp, y = (-10).dp),
                shape = CircleShape
            ) {
                Text("IA", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun CompactRow(item: ServiceItem, onClick: () -> Unit, onClickIA: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp), verticalAlignment = Alignment.CenterVertically) {

            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                modifier = Modifier
                    .size(74.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text("${if (item.isOpenNow) "Abierto" else "Cerrado"} · ${item.schedule}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Text(item.price1, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
            }

            Surface(
                onClick = onClickIA,
                shape = CircleShape,
                color = Color(0xFFEBDEFB),
                tonalElevation = 6.dp,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("IA", color = Color(0xFF4A2548), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
