package com.tesis.appmovil.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.tesis.appmovil.models.UserRole
import com.tesis.appmovil.ui.auth.ChooseRoleScreen
import com.tesis.appmovil.ui.auth.LoginScreen
import com.tesis.appmovil.ui.auth.RegisterScreen

import com.tesis.appmovil.ui.business.*

import com.tesis.appmovil.ui.business.BusinessContactInfoScreen
import com.tesis.appmovil.ui.business.BusinessDocumentsScreen
import com.tesis.appmovil.ui.business.BusinessImagesScreen
import com.tesis.appmovil.ui.business.BusinessLocationScreen
import com.tesis.appmovil.ui.business.BusinessScheduleScreen

import com.tesis.appmovil.ui.home.BusinessDetailScreen
import com.tesis.appmovil.ui.home.HomeScreen
import com.tesis.appmovil.ui.search.BuscarScreen
import com.tesis.appmovil.ui.servicios.CreateServiceScreen
import com.tesis.appmovil.ui.servicios.EditServiceScreen
import com.tesis.appmovil.ui.servicios.ServiciosScreen
import com.tesis.appmovil.viewmodel.*

/** Destinos de la barra inferior */
sealed class Dest(
    val route: String,
    val label: String = "",
    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    // flujo auth/onboarding
    object Login            : Dest("login")
    object Register         : Dest("register")
    object ChooseRole       : Dest("chooseRole")
    object RegisterBusiness : Dest("registerBusiness")
    object BusinessContact  : Dest("businessContact")
    object BusinessSchedule : Dest("businessSchedule")
    object BusinessImages   : Dest("businessImages")
    object BusinessLocation : Dest("businessLocation")


    object BusinessDocuments : Dest("businessDocuments")


    object Home     : Dest("home",     "Inicio", Icons.Outlined.Home)
    object Search   : Dest("search",   "Buscar", Icons.Outlined.Search)
    object Business : Dest("business", "Negocio", Icons.Default.Store)


    object BusinessReady : Dest("register/ready")


}

@Composable
fun AppRoot() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = "main") {
        composable("main") {
            MainWithBottomBar()
            val vmNegocio: NegocioViewModel = viewModel()
            val ui by vmNegocio.ui.collectAsState()

            LaunchedEffect(Unit) {
                vmNegocio.obtenerMiNegocio()
            }

            ui.negocio?.let { negocio ->
                LaunchedEffect(negocio.id_negocio) {
                    nav.navigate("main") {
                        popUpTo("splash") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        // 1) MAIN: pestañas persistentes con BottomBar
        composable("main") {
            MainWithBottomBar()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainWithBottomBar() {
    val innerNav = rememberNavController()
    val items = listOf(Dest.Home, Dest.Search, Dest.Business)
    val backStack by innerNav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    // Rutas donde ocultar bottom bar
    val hideBottomBarRoutes = listOf(
        Dest.Login.route,
        Dest.Register.route,
        Dest.RegisterBusiness.route,
        Dest.BusinessContact.route,

        Dest.BusinessSchedule.route,
        Dest.BusinessImages.route,
        Dest.BusinessLocation.route,
        Dest.Business.route,
        Dest.BusinessSchedule.route,
        Dest.BusinessImages.route,
        Dest.BusinessLocation.route,
        Dest.BusinessDocuments.route,
        Dest.BusinessReady.route,
        "createService/{negocioId}",
        "editService/{idServicio}",
        "servicios/{negocioId}"




    )
    val showBottomBar = current !in hideBottomBarRoutes

    // Rutas donde mostrar icono “Cerrar”
    val showCloseIconRoutes = listOf(
        Dest.Login.route,
        Dest.RegisterBusiness.route
    )

    Scaffold(
        topBar = {
            if (current in showCloseIconRoutes) {
                CenterAlignedTopAppBar(
                    title = {},
                    actions = {
                        IconButton(onClick = {
                            innerNav.navigate(Dest.Home.route) {
                                popUpTo(Dest.Home.route) { inclusive = true }
                            }
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
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
                            selected   = current == d.route,
                            onClick    = {
                                innerNav.navigate(d.route) {
                                    popUpTo(innerNav.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            icon       = { d.icon?.let { Icon(it, contentDescription = d.label) } },
                            label      = { Text(d.label) }
                        )
                    }
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
                val vm: ServicioViewModel = viewModel()
                HomeScreen(vm, innerNav)   // 👈 sigue recibiendo innerNav
            }

            // SEARCH (pasa los parámetros requeridos)
//            RENATO
            composable(Dest.Search.route) {
                val vmNegocios: HomeNegocioViewModel = viewModel()
                val vmServicios: ServicioViewModel = viewModel()
                BuscarScreen(
                    vmNegocios = vmNegocios,
                    vmServicios = vmServicios,
                    onClickNegocio = { id ->
                        if (id > 0) innerNav.navigate("businessDetail/$id")
                    }
                )
            }


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
                        innerNav.navigate(Dest.BusinessImages.route)
                    },
                    onBack = {
                        innerNav.popBackStack() // vuelve a la pantalla anterior (BusinessContact)
                    }
                )
            }
            composable(Dest.BusinessImages.route) {
                BusinessImagesScreen(
                    onContinue = {
                        innerNav.navigate(Dest.BusinessLocation.route)

                    },
                    onBack = {
                        innerNav.popBackStack() // vuelve a la pantalla anterior (BusinessSchedule)
                    }
                )
            }

            composable(Dest.BusinessLocation.route) {
                BusinessLocationScreen(
                    onLocationSelected = { latLng ->
                        // Aquí guardas la ubicación seleccionada
                        println("📍 Ubicación guardada: $latLng")
                        // Puedes navegar a la siguiente pantalla o volver atrás
                        innerNav.navigate(Dest.BusinessDocuments.route)
                    },
                    onBack = {
                        innerNav.popBackStack() // vuelve a la pantalla anterior
                    }
                )
            }
            composable(Dest.BusinessDocuments.route) {
                BusinessDocumentsScreen(
                    onContinue = {
                        // Cuando se suben documentos y se hace click en continuar
                        innerNav.navigate(Dest.BusinessReady.route)
                    },
                    onSkip = {
                        // Cuando se omite la subida de documentos
                        innerNav.navigate(Dest.BusinessReady.route)
                    },
                    onBack = {
                        innerNav.popBackStack()
                    }
                )
            }

            composable(Dest.BusinessReady.route) {
                com.tesis.appmovil.ui.business.BusinessReadyScreen(
                    onPublish = {
                        // Si necesitas, aquí llamas a tu ViewModel para publicar
                        // viewModel.publishBusiness()

                        // Luego manda al Home dentro del mismo NavHost interno
                        innerNav.navigate(Dest.Home.route) {
                            popUpTo(Dest.Home.route) { inclusive = true }
                            launchSingleTop = true
                        }
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

            composable(
                route = "businessDetail/{idNegocio}",
                arguments = listOf(navArgument("idNegocio") { type = NavType.IntType })
            ) { backStackEntry ->
                val idNegocio = backStackEntry.arguments?.getInt("idNegocio") ?: 0
                val vm: NegocioViewModel = viewModel()
                BusinessDetailScreen(idNegocio = idNegocio, vm = vm, onBack = { innerNav.popBackStack() })
            }

            // AÑADE AQUÍ la nueva ruta para editar servicio:
            composable(
                route = "editService/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                val vm: ServicioViewModel = viewModel()
                EditServiceScreen(servicioId = id, vm = vm, navController = innerNav)
            }

            composable(
                "servicios/{negocioId}",
                arguments = listOf(navArgument("negocioId") { type = NavType.IntType })
            ) { back ->
                val negocioId = back.arguments?.getInt("negocioId") ?: 0
                val vmSrv: ServicioViewModel = viewModel()
                ServiciosScreen(vmSrv, innerNav, negocioId) {
                    innerNav.navigate("createService/$negocioId")
                }
            }
            composable(
                "createService/{negocioId}",
                arguments = listOf(navArgument("negocioId") { type = NavType.IntType })
            ) { back ->
                val negocioId = back.arguments?.getInt("negocioId") ?: 0
                val vmSrv: ServicioViewModel = viewModel()
                CreateServiceScreen(negocioId, vmSrv, innerNav)
            }
            composable(
                "editService/{idServicio}",
                arguments = listOf(navArgument("idServicio") { type = NavType.IntType })
            ) { back ->
                val idSrv: Int = back.arguments?.getInt("idServicio") ?: 0
                val vmSrv: ServicioViewModel = viewModel()
                EditServiceScreen(idSrv, vmSrv, innerNav)
            }
        }
    }
}
