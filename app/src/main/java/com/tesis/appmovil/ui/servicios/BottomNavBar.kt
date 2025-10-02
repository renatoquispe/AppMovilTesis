package com.tesis.appmovil.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun BottomNavBar(
    navController: NavController,
    negocioId: Int,
    selected: String // "servicios", "negocio", "cuenta"
) {
    Surface(shadowElevation = 8.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PillItem(
                icon = Icons.Outlined.Home,
                label = "Servicios",
                selected = selected == "servicios"
            ) { navController.navigate("servicios/$negocioId") }

            PillItem(
                icon = Icons.Outlined.Store,
                label = "Negocio",
                selected = selected == "negocio"
            ) { navController.navigate("businessProfile/$negocioId") }

            PillItem(
                icon = Icons.Filled.Person,
                label = "Cuenta",
                selected = selected == "cuenta"
            ) { navController.navigate("cuenta") }
        }
    }
}

@Composable
private fun PillItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
    val fg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .background(bg, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = fg)
        Spacer(Modifier.width(8.dp))
        Text(label, color = fg, style = MaterialTheme.typography.labelMedium)
    }
}
