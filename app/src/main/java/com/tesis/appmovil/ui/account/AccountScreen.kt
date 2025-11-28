package com.tesis.appmovil.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tesis.appmovil.ui.components.BottomNavBar
import com.tesis.appmovil.viewmodel.AuthViewModel

@Composable
fun AccountScreen(
    navController: NavController,
    authVM: AuthViewModel = viewModel(),
    negocioId: Int = 0,
    userName: String = "Nombres y Apellidos",
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onFaqClick: () -> Unit = {},
    onSupportClick: () -> Unit = {},
    // Si el caller pasa esta lambda, la ejecutamos. Si es null, hacemos el logout/navegación por defecto aquí.
    onLogoutClick: (() -> Unit)? = null
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    // Función que realiza el logout y navega a Home (comportamiento por defecto)
    fun performLogoutAndGoHome() {
        try {
            authVM.logout(context) // asegúrate que tu AuthViewModel tenga este método (o cámbialo)
        } catch (e: Exception) {
            // Opcional: manejar excepción
        }

        navController.navigate("home") {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                navController = navController,
                negocioId = negocioId,
                selected = "cuenta"
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))
            Text(userName, style = MaterialTheme.typography.titleMedium)
            Text("Cuenta personal", style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(24.dp))

            // Opciones
            AccountOption(
                icon = Icons.Outlined.Person,
                text = "Perfil",
                onClick = onProfileClick
            )
            AccountOption(
                icon = Icons.Outlined.Settings,
                text = "Ajustes",
                onClick = onSettingsClick
            )
            AccountOption(
                icon = Icons.Outlined.HelpOutline,
                text = "Preguntas frecuentes",
                onClick = onFaqClick
            )
            AccountOption(
                icon = Icons.Outlined.SupportAgent,
                text = "Soporte",
                onClick = onSupportClick
            )

            // Opción Cerrar sesión (abre diálogo de confirmación)
            AccountOption(
                icon = Icons.Outlined.ExitToApp,
                text = "Cerrar sesión",
                onClick = { showLogoutDialog = true }
            )
        }
    }

    // Dialogo de confirmación para cerrar sesión
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que quieres cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    // Si el caller pasó onLogoutClick, lo ejecutamos; si no, hacemos el comportamiento por defecto
                    if (onLogoutClick != null) {
                        onLogoutClick()
                    } else {
                        performLogoutAndGoHome()
                    }
                }) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun AccountOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = text, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AccountScreenPreview() {
    val navController = rememberNavController()
    AccountScreen(
        navController = navController,
        userName = "Juan Pérez",
        onProfileClick = {},
        onSettingsClick = {},
        onFaqClick = {},
        onSupportClick = {}
        // no pasamos onLogoutClick, usará el comportamiento por defecto
    )
}
