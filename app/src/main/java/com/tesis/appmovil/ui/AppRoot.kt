package com.tesis.appmovil.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tesis.appmovil.ui.home.HomeScreen
import com.tesis.appmovil.viewmodel.HomeViewModel

sealed class Dest(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Dest("home", "Inicio", Icons.Outlined.Home)
    object Search : Dest("search", "Buscar", Icons.Outlined.Search)
    object Account : Dest("account", "Cuenta", Icons.Outlined.AccountCircle)
}

@Composable
fun AppRoot() {
    val nav = rememberNavController()
    val items = listOf(Dest.Home, Dest.Search, Dest.Account)
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { d ->
                    NavigationBarItem(
                        selected = current == d.route,
                        onClick = {
                            nav.navigate(d.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(d.icon, contentDescription = d.label) },
                        label = { Text(d.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController = nav, startDestination = Dest.Home.route, modifier = Modifier.padding(padding)) {
            composable(Dest.Home.route) {
                val vm: HomeViewModel = viewModel()
                HomeScreen(vm)
            }
            composable(Dest.Search.route) { Placeholder("Buscar (próximamente)") }
            composable(Dest.Account.route) { Placeholder("Cuenta (próximamente)") }
        }
    }
}

@Composable
private fun Placeholder(text: String) {
    Surface(Modifier.fillMaxSize()) {
        Text(text, modifier = Modifier
            .fillMaxSize()
            .padding(24.dp), textAlign = TextAlign.Center)
    }
}

