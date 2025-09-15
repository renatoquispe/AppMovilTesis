package com.tesis.appmovil.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tesis.appmovil.models.UserRole
import com.tesis.appmovil.ui.auth.ChooseRoleScreen
import com.tesis.appmovil.ui.auth.LoginScreen
import com.tesis.appmovil.ui.auth.RegisterScreen
import com.tesis.appmovil.ui.home.BusinessDetailScreen
import com.tesis.appmovil.ui.home.HomeScreen
import com.tesis.appmovil.ui.search.BuscarScreen     // <- usa la pantalla con controles
import com.tesis.appmovil.viewmodel.AuthViewModel
import com.tesis.appmovil.viewmodel.HomeViewModel

// ----------------- Rutas -----------------
sealed class Dest(
    val route: String,
    val label: String = "",
    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    // flujo auth
    object Login : Dest("login")
    object Register : Dest("register")
    object ChooseRole : Dest("chooseRole")

    // flujo principal con bottom bar
    object Home : Dest("home", "Inicio", Icons.Outlined.Home)
    object Search : Dest("search", "Buscar", Icons.Outlined.Search)
    object Account : Dest("account", "Cuenta", Icons.Outlined.AccountCircle)
}

/**
 * Root del app: navega Login -> ChooseRole -> Main (tabs persistentes)
 */
@Composable
fun AppRoot() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Dest.Login.route) {

        // 1) Login
        composable(Dest.Login.route) {
            val vm: AuthViewModel = viewModel()
            LoginScreen(
                vm = vm,
                onSuccess = {
                    nav.navigate(Dest.ChooseRole.route) {
                        popUpTo(Dest.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { nav.navigate(Dest.Register.route) }
            )
        }

        // 2) Register
        composable(Dest.Register.route) {
            val vm: AuthViewModel = viewModel()
            RegisterScreen(
                vm = vm,
                onSuccess = {
                    nav.navigate(Dest.ChooseRole.route) {
                        popUpTo(Dest.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    nav.navigate(Dest.Login.route) {
                        popUpTo(Dest.Register.route) { inclusive = true }
                    }
                }
            )
        }

        // 3) ChooseRole
        composable(Dest.ChooseRole.route) {
            val vm: AuthViewModel = viewModel()
            ChooseRoleScreen(
                onClient = {
                    vm.chooseRole(UserRole.CLIENT)
                    nav.navigate("main") { popUpTo(Dest.ChooseRole.route) { inclusive = true } }
                },
                onProfessional = {
                    vm.chooseRole(UserRole.PROFESSIONAL)
                    nav.navigate("main") { popUpTo(Dest.ChooseRole.route) { inclusive = true } }
                }
            )
        }

        // 4) MAIN: pestañas persistentes (sin NavHost interno)
        composable("main") {
            MainWithBottomBar()
        }
    }
}

/**
 * Bottom bar con 3 pestañas **persistentes**.
 * Mantenemos todas montadas y solo cambiamos visibilidad con alpha/zIndex.
 * Así el mapa NO se destruye al cambiar de pestaña.
 */
@Composable
fun MainWithBottomBar() {
    val innerNav = rememberNavController()
    val items = listOf(Dest.Home, Dest.Search, Dest.Account)
    val backStack by innerNav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { d ->
                    NavigationBarItem(
                        selected = current == d.route,
                        onClick = {
                            innerNav.navigate(d.route) {
                                popUpTo(innerNav.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { d.icon?.let { Icon(it, contentDescription = d.label) } },
                        label = { Text(d.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = innerNav,
            startDestination = Dest.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            // HOME con navegación al detalle
            composable(Dest.Home.route) {
                val vm: HomeViewModel = viewModel()
                HomeScreen(vm, innerNav)   // 👈 aquí le pasas innerNav
            }

            // BUSINESS DETAIL
            composable(
                route = "businessDetail/{businessId}",
                arguments = listOf(navArgument("businessId") { type = NavType.IntType })
            ) { backStackEntry ->
                val businessId = backStackEntry.arguments?.getInt("businessId") ?: 0
                BusinessDetailScreen(
                    navController = innerNav,
                    businessId = businessId
                )
            }

            // SEARCH
            composable(Dest.Search.route) {
                BuscarScreen()
            }

            // ACCOUNT
            composable(Dest.Account.route) {
                Surface(Modifier.fillMaxSize()) {
                    Text(
                        "Cuenta (próximamente)",
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

