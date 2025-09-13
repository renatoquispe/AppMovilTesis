package com.tesis.appmovil.ui

import android.content.Intent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tesis.appmovil.MapsActivity
import com.tesis.appmovil.models.UserRole
import com.tesis.appmovil.ui.account.AccountScreen
import com.tesis.appmovil.ui.account.ChangePasswordScreen
import com.tesis.appmovil.ui.account.EditProfileScreen
import com.tesis.appmovil.ui.account.FAQScreen
import com.tesis.appmovil.ui.account.SettingsScreen
import com.tesis.appmovil.ui.account.SupportScreen
import com.tesis.appmovil.ui.auth.ChooseRoleScreen
import com.tesis.appmovil.ui.auth.LoginScreen
import com.tesis.appmovil.ui.auth.RegisterScreen
import com.tesis.appmovil.ui.home.HomeScreen
import com.tesis.appmovil.viewmodel.AuthViewModel
import com.tesis.appmovil.viewmodel.HomeViewModel

sealed class Dest(val route: String, val label: String = "", val icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    // flujo auth
    object Login : Dest("login")

    object Register : Dest("register")
    object ChooseRole : Dest("chooseRole")

    // flujo principal del home (Scaffold con bottom bar)
    object Home : Dest("home", "Inicio", Icons.Outlined.Home)
    object Search : Dest("search", "Buscar", Icons.Outlined.Search)
    object Account : Dest("account", "Cuenta", Icons.Outlined.AccountCircle)
}

@Composable
fun AppRoot() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Dest.Login.route) {
        // 1. Login
        composable(Dest.Login.route) {
            val vm: AuthViewModel = viewModel()
            LoginScreen(
                vm = vm,
                onSuccess = {
                    nav.navigate(Dest.ChooseRole.route) {
                        popUpTo(Dest.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    nav.navigate(Dest.Register.route)
                }
            )
        }

        composable(Dest.Register.route) {
            val vm: AuthViewModel = viewModel()
            RegisterScreen(
                vm = vm,
                onSuccess = {
                    nav.navigate(Dest.ChooseRole.route) {
                        popUpTo(Dest.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {   // 👈 aquí agregas lo que hace al pulsar “Iniciar Sesión”
                    nav.navigate(Dest.Login.route) {
                        popUpTo(Dest.Register.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. ChooseRole
        composable(Dest.ChooseRole.route) {
            val vm: AuthViewModel = viewModel()
            ChooseRoleScreen(
                onClient = {
                    vm.chooseRole(UserRole.CLIENT)
                    nav.navigate("main") {
                        popUpTo(Dest.ChooseRole.route) { inclusive = true }
                    }
                },
                onProfessional = {
                    vm.chooseRole(UserRole.PROFESSIONAL)
                    nav.navigate("main") {
                        popUpTo(Dest.ChooseRole.route) { inclusive = true }
                    }
                }
            )
        }

        // 3. Main con bottom bar
        composable("main") {
            // 👇 usa un NavController NUEVO para el bottom bar
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
                    composable(Dest.Home.route) {
                        val vm: HomeViewModel = viewModel()
                        HomeScreen(vm)
                    }
                    composable(Dest.Search.route) {
                        val context = LocalContext.current
                        LaunchedEffect(Unit) {
                            val intent = Intent(context, MapsActivity::class.java)
                            context.startActivity(intent)
                        }
                    }

//                    composable(Dest.Account.route) {
//                        AccountScreen(
//                            userName = "Juan Pérez", // aquí luego puedes pasar el nombre real desde AuthRepo
//                            onProfileClick = { /* navegar a pantalla perfil */ },
//                            onSettingsClick = { /* ajustes */ },
//                            onFaqClick = { /* preguntas frecuentes */ },
//                            onSupportClick = { /* soporte */ },
//                            onLogoutClick = { /* cerrar sesión */ }
//                        )
//                    }
//                    composable(Dest.Account.route) { Placeholder("Cuenta (próximamente)") }
                    composable(Dest.Account.route) {
                        AccountScreen(
                            userName = "Juan Pérez",
                            onProfileClick = {
                                innerNav.navigate("editProfile")   // 👈 aquí navegamos a la nueva pantalla
                            },
                            onSettingsClick = {
                                innerNav.navigate("settings")  // 👈 Navegar a ajustes
                            },
                            onFaqClick = {
                                innerNav.navigate("faq")
                            },
                            onSupportClick = {
                                innerNav.navigate("support")
                            },
                            onLogoutClick = { }
                        )
                    }
                    composable("editProfile") {
                        EditProfileScreen(nav = innerNav)
                    }
                    composable("settings") {
                        SettingsScreen(navController = innerNav)
                    }
                    composable("changePassword") {
                        ChangePasswordScreen(navController = innerNav)
                    }
                    composable("faq") {
                        FAQScreen(navController = innerNav)
                    }
                    composable("support") {
                        SupportScreen(navController = innerNav)
                    }



                }
            }
        }
    }
}


@Composable
private fun Placeholder(text: String) {
    Surface(Modifier.fillMaxSize()) {
        Text(
            text,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            textAlign = TextAlign.Center
        )
    }
}
