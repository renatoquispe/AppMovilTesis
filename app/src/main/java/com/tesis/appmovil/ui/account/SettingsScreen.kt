package com.tesis.appmovil.ui.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController? = null,
    onBackClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {
        // Navegar a la pantalla de cambiar contraseña
        navController?.navigate("changePassword")
    },
    onToggleDarkMode: (Boolean) -> Unit = {}
) {
    // Estado para el modo oscuro (podrías conectar esto con tu ViewModel después)
    var isDarkModeEnabled by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ajustes") },
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

            // Opción: Cambiar contraseña
            SettingsOption(
                icon = Icons.Outlined.Lock,
                title = "Cambiar contraseña",
                onClick = {
                    navController?.navigate("changePassword") ?: onChangePasswordClick()
                }
            )
        }
    }
}

@Composable
private fun SettingsOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    showSwitch: Boolean = false,
    isSwitchChecked: Boolean = false,
    onSwitchChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !showSwitch && onClick != null) { onClick?.invoke() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            if (showSwitch) {
                Switch(
                    checked = isSwitchChecked,
                    onCheckedChange = onSwitchChange
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsScreen()
    }
}