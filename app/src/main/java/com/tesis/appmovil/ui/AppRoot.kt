package com.tesis.appmovil.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import com.tesis.appmovil.ui.business.RegisterBusinessScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.tesis.appmovil.ui.business.BusinessContactInfoScreen
import com.tesis.appmovil.ui.business.BusinessScheduleScreen
import com.tesis.appmovil.ui.home.BusinessDetailScreen
import com.tesis.appmovil.ui.home.HomeScreen
import com.tesis.appmovil.ui.search.BuscarScreen     // <- usa la pantalla con controles
import com.tesis.appmovil.viewmodel.AuthViewModel
import com.tesis.appmovil.viewmodel.HomeViewModel
import com.tesis.appmovil.viewmodel.NegocioViewModel
import com.tesis.appmovil.viewmodel.ServicioViewModel

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
    object BusinessContact : Dest("businessContact")
    object BusinessSchedule : Dest("businessSchedule")


    object RegisterBusiness : Dest("registerBusiness")


    // flujo principal con bottom bar
    object Home : Dest("home", "Inicio", Icons.Outlined.Home)
    object Search : Dest("search", "Buscar", Icons.Outlined.Search)
//    object Account : Dest("account", "Cuenta", Icons.Outlined.AccountCircle)
    object Business : Dest("business", "Negocio", Icons.Default.Store)

}

/**
 * Root del app: navega Login -> ChooseRole -> Main (tabs persistentes)
 */
@Composable
fun AppRoot() {
    val nav = rememberNavController()


//    NavHost(navController = nav, startDestination = Dest.Login.route) {
    NavHost(navController = nav, startDestination = "main") {
        composable("main") {
            MainWithBottomBar()
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainWithBottomBar() {
    val innerNav = rememberNavController()
    val items = listOf(Dest.Home, Dest.Search, Dest.Business)
    val backStack by innerNav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    // Rutas donde NO queremos mostrar el bottom bar
    val hideBottomBarRoutes = listOf(
        Dest.Login.route,
        Dest.Register.route,
        Dest.RegisterBusiness.route,
        Dest.Business.route,
        Dest.BusinessSchedule.route
    )
    val showBottomBar = current !in hideBottomBarRoutes
    // Rutas donde sí queremos mostrar el botón de cerrar
    val showCloseIconRoutes = listOf(
        Dest.Login.route,
        Dest.Register.route,
        Dest.RegisterBusiness.route,
        Dest.Business.route
    )

    Scaffold(
        topBar = {
            if (current in showCloseIconRoutes) {
                androidx.compose.material3.TopAppBar(
                    title = {},
                    actions = {
                        IconButton(onClick = {
                            // Al presionar la X → vuelve al Home
                            innerNav.navigate(Dest.Home.route) {
                                popUpTo(Dest.Home.route) { inclusive = true }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar"
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
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
        }
    )
    { padding ->
        NavHost(
            navController = innerNav,
            startDestination = Dest.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            // HOME con navegación al detalle
            composable(Dest.Home.route) {
                val vm: ServicioViewModel = viewModel()
                HomeScreen(vm, innerNav)   // 👈 sigue recibiendo innerNav
            }
            // SEARCH
//            composable(Dest.Search.route) {
//                BuscarScreen()
//            }
            // SEARCH (pasa los parámetros requeridos)
            //RENATO
//            composable(Dest.Search.route) {
//                val vmNegocios: HomeNegocioViewModel = viewModel()
//                val vmServicios: ServicioViewModel = viewModel()
//                BuscarScreen(
//                    vmNegocios = vmNegocios,
//                    vmServicios = vmServicios,
//                    onClickNegocio = { id ->
//                        if (id > 0) innerNav.navigate("businessDetail/$id")
//                    }
//                )
//            }


            // BUSINESS DETAIL
            composable(
                route = "businessDetail/{idNegocio}",
                arguments = listOf(navArgument("idNegocio") { type = NavType.IntType })
            ) { backStackEntry ->
                val idNegocio = backStackEntry.arguments?.getInt("idNegocio") ?: 0
                val vm: NegocioViewModel = viewModel()
                BusinessDetailScreen(idNegocio = idNegocio, vm = vm, onBack = {
                    innerNav.popBackStack()
                }
                )
            }


            // BUSINESS
            composable(Dest.Business.route) {
                val vm: AuthViewModel = viewModel()
                LoginScreen(
                    vm = vm,
                    onSuccess = {
                        innerNav.navigate(Dest.RegisterBusiness.route)
                    },
                    onNavigateToRegister = {
                        innerNav.navigate(Dest.Register.route)
                    }
                )
            }
            composable(Dest.RegisterBusiness.route) {
                RegisterBusinessScreen(
                    onContinue = {
                        innerNav.navigate(Dest.BusinessContact.route)
//                        innerNav.popBackStack(Dest.Business.route, false)
                    },
                    onBack = {
                        // botón de cerrar (la X arriba) → vuelve al inicio
                        innerNav.navigate(Dest.Home.route) {
                            popUpTo(Dest.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Dest.BusinessContact.route) {
                BusinessContactInfoScreen(
                    onContinue = {
                        // Por ahora al terminar → vuelve al inicio
                        innerNav.navigate(Dest.BusinessSchedule.route)
//                        {
//                            popUpTo(Dest.Home.route) { inclusive = true }
//                        }
                    },
                    onBack = {
                        innerNav.popBackStack() // vuelve a RegisterBusiness
                    }
                )
            }
            composable(Dest.BusinessSchedule.route) {
                BusinessScheduleScreen(
                    onContinue = {
                        innerNav.popBackStack()
                        // Cuando termine los horarios → vuelve al Home
//                        innerNav.navigate(Dest.Home.route) {
//                            popUpTo(Dest.Home.route) { inclusive = true }
//                        }
                    },
                    onBack = {
                        innerNav.popBackStack() // vuelve a la pantalla anterior (BusinessContact)
                    }
                )
            }


            composable(Dest.Register.route) {
                val vm: AuthViewModel = viewModel()
                RegisterScreen(
                    vm = vm,
                    onSuccess = {
                        // después de registrarse → vuelve al login de negocio
                        innerNav.popBackStack(Dest.Business.route, false)
                    },
                    onNavigateToLogin = {
                        innerNav.popBackStack() // vuelve al login
                    }
                )
            }




        }
    }
}

