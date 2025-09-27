package com.tesis.appmovil.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navigation
import androidx.navigation.navArgument
import com.tesis.appmovil.ui.auth.LoginScreen
import com.tesis.appmovil.ui.auth.RegisterScreen
import com.tesis.appmovil.ui.business.*
import com.tesis.appmovil.ui.chat.ChatBotScreen
import com.tesis.appmovil.ui.home.BusinessDetailScreen
import com.tesis.appmovil.ui.home.HomeScreen
import com.tesis.appmovil.ui.search.BuscarScreen
import com.tesis.appmovil.ui.servicios.EditServiceScreen
import com.tesis.appmovil.viewmodel.*

// ---- Lottie para el FAB del chatbot (igual al de Home) ----
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.tesis.appmovil.R
// -----------------------------------------------------------

// ----------------- Rutas -----------------
sealed class Dest(
    val route: String,
    val label: String = "",
    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    // flujo auth
    object Login : Dest("login")
    object Register : Dest("register")

    // flujo negocio (registro)
    object RegisterBusiness : Dest("registerBusiness")
    object BusinessContact : Dest("businessContact")
    object BusinessSchedule : Dest("businessSchedule")
    object BusinessImages : Dest("businessImages")
    object BusinessLocation : Dest("businessLocation")
    object BusinessDocuments : Dest("businessDocuments")
    object BusinessReady : Dest("businessReady")

    // flujo principal con bottom bar
    object Home : Dest("home", "Inicio", Icons.Outlined.Home)
    object Search : Dest("search", "Buscar", Icons.Outlined.Search)
    object Business : Dest("business", "Negocio", Icons.Default.Store)
}

/**
 * Root del app
 */
@Composable
fun AppRoot() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "main") {
        composable("main") { MainWithBottomBar() }
    }
}

/**
 * Bottom bar con 3 pestañas persistentes - con FAB global del ChatBot
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainWithBottomBar() {
    val innerNav = rememberNavController()
    val items = listOf(Dest.Home, Dest.Search, Dest.Business)
    val backStack by innerNav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    val authViewModel: AuthViewModel = viewModel()

    // Ocultar bottom bar en ciertas rutas
    val hideBottomBarRoutes = listOf(
        Dest.Login.route,
        Dest.Register.route,
        Dest.Business.route,
        Dest.RegisterBusiness.route,
        Dest.BusinessContact.route,
        Dest.BusinessSchedule.route,
        Dest.BusinessImages.route,
        Dest.BusinessLocation.route,
        Dest.BusinessDocuments.route,
        Dest.BusinessReady.route,
        "registerBusinessFlow"
    )
    val showBottomBar = current !in hideBottomBarRoutes

    // Mostrar FAB del chatbot solo en Home y Buscar
    val showChatFab = current == Dest.Home.route || current == Dest.Search.route

    Scaffold(
        topBar = {
            if (current in listOf(
                    Dest.Business.route,
                    Dest.Register.route,
                    Dest.RegisterBusiness.route
                )
            ) {
                TopAppBar(
                    title = {},
                    actions = {
                        IconButton(onClick = {
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
                                // 🔽 Cierra el ChatBot si estaba abierto (en cualquier pestaña)
                                innerNav.popBackStack("chatbot", inclusive = true)

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
        },
        floatingActionButton = {
            if (showChatFab) {
                FloatingActionButton(
                    onClick = { innerNav.navigate("chatbot") { launchSingleTop = true } },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    val comp by rememberLottieComposition(
                        LottieCompositionSpec.RawRes(R.raw.bellabot)
                    )
                    val progress by animateLottieCompositionAsState(
                        composition = comp,
                        iterations = LottieConstants.IterateForever
                    )
                    LottieAnimation(composition = comp, progress = { progress }, modifier = Modifier.size(64.dp))
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        NavHost(
            navController = innerNav,
            startDestination = Dest.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            // HOME
            composable(Dest.Home.route) {
                val vm: ServicioViewModel = viewModel()
                HomeScreen(vm, innerNav)
            }

            // SEARCH
            composable(Dest.Search.route) {
                val vmNegocios: HomeNegocioViewModel = viewModel()
                val vmServicios: ServicioViewModel = viewModel()
                BuscarScreen(vmNegocios, vmServicios) { id ->
                    if (id > 0) innerNav.navigate("businessDetail/$id")
                }
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
                })
            }

            // BUSINESS - ESTA ES LA PANTALLA QUE MUESTRA EL LOGIN
            composable(Dest.Business.route) {
                LoginScreen(
                    vm = authViewModel,
                    onSuccess = { innerNav.navigate("registerBusinessFlow") },
                    onNavigateToRegister = { innerNav.navigate(Dest.Register.route) }
                )
            }

            // REGISTER
            composable(Dest.Register.route) {
                RegisterScreen(
                    vm = authViewModel,
                    onSuccess = {
                        innerNav.navigate(Dest.Login.route) {
                            popUpTo(Dest.Register.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        innerNav.navigate(Dest.Login.route) {
                            popUpTo(Dest.Register.route) { inclusive = true }
                        }
                    }
                )
            }

            // LOGIN
            composable(Dest.Login.route) {
                LoginScreen(
                    vm = authViewModel,
                    onSuccess = {
                        if (!authViewModel.uiState.value.token.isNullOrEmpty()) {
                            innerNav.navigate("registerBusinessFlow") {
                                popUpTo(Dest.Login.route) { inclusive = true }
                            }
                        }
                    },
                    onNavigateToRegister = { innerNav.navigate(Dest.Register.route) }
                )
            }

            // CHATBOT (ruta pública)
            composable("chatbot") { ChatBotScreen() }

            // SUBNAVEGACIÓN DEL REGISTRO DE NEGOCIO
            navigation(
                route = "registerBusinessFlow",
                startDestination = Dest.RegisterBusiness.route
            ) {
                composable(Dest.RegisterBusiness.route) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        innerNav.getBackStackEntry("registerBusinessFlow")
                    }
                    val negocioVM: NegocioViewModel = viewModel(parentEntry)
                    RegisterBusinessScreen(
                        authViewModel = authViewModel,
                        negocioViewModel = negocioVM,
                        onContinue = { innerNav.navigate(Dest.BusinessContact.route) },
                        onBack = {
                            innerNav.navigate(Dest.Home.route) {
                                popUpTo(Dest.Home.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Dest.BusinessContact.route) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        innerNav.getBackStackEntry("registerBusinessFlow")
                    }
                    val negocioVM: NegocioViewModel = viewModel(parentEntry)
                    BusinessContactInfoScreen(
                        negocioViewModel = negocioVM,
                        onContinue = { innerNav.navigate(Dest.BusinessSchedule.route) },
                        onBack = { innerNav.popBackStack() }
                    )
                }

                composable(Dest.BusinessSchedule.route) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        innerNav.getBackStackEntry("registerBusinessFlow")
                    }
                    val negocioVM: NegocioViewModel = viewModel(parentEntry)
                    BusinessScheduleScreen(
                        negocioViewModel = negocioVM,
                        onContinue = { innerNav.navigate(Dest.BusinessImages.route) },
                        onBack = { innerNav.popBackStack() }
                    )
                }

                composable(Dest.BusinessImages.route) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        innerNav.getBackStackEntry("registerBusinessFlow")
                    }
                    val negocioVM: NegocioViewModel = viewModel(parentEntry)
                    BusinessImagesScreen(
                        negocioViewModel = negocioVM,
                        onContinue = { innerNav.navigate(Dest.BusinessLocation.route) },
                        onBack = { innerNav.popBackStack() }
                    )
                }

                composable(Dest.BusinessLocation.route) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        innerNav.getBackStackEntry("registerBusinessFlow")
                    }
                    val negocioVM: NegocioViewModel = viewModel(parentEntry)
                    BusinessLocationScreen(
                        negocioViewModel = negocioVM,
                        onLocationSelected = { innerNav.navigate(Dest.BusinessDocuments.route) },
                        onBack = { innerNav.popBackStack() }
                    )
                }

                composable(Dest.BusinessDocuments.route) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        innerNav.getBackStackEntry("registerBusinessFlow")
                    }
                    val negocioVM: NegocioViewModel = viewModel(parentEntry)
                    BusinessDocumentsScreen(
                        negocioViewModel = negocioVM,
                        onContinue = { innerNav.navigate(Dest.BusinessReady.route) },
                        onSkip = { innerNav.navigate(Dest.BusinessReady.route) },
                        onBack = { innerNav.popBackStack() }
                    )
                }

                composable(Dest.BusinessReady.route) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        innerNav.getBackStackEntry("registerBusinessFlow")
                    }
                    val negocioVM: NegocioViewModel = viewModel(parentEntry)
                    BusinessReadyScreen(
                        negocioViewModel = negocioVM,
                        onPublish = {
                            innerNav.navigate(Dest.Home.route) {
                                popUpTo(Dest.Home.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            // EDIT SERVICE
            composable(
                route = "editService/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                val vm: ServicioViewModel = viewModel()
                EditServiceScreen(servicioId = id, vm = vm, navController = innerNav)
            }
        }
    }
}


//package com.tesis.appmovil.ui
//
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material.icons.filled.Store
//import androidx.compose.material.icons.outlined.Home
//import androidx.compose.material.icons.outlined.Search
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavType
//import androidx.navigation.compose.*
//import androidx.navigation.navigation
//import androidx.navigation.navArgument
//import com.tesis.appmovil.ui.auth.LoginScreen
//import com.tesis.appmovil.ui.auth.RegisterScreen
//import com.tesis.appmovil.ui.business.*
//import com.tesis.appmovil.ui.home.BusinessDetailScreen
//import com.tesis.appmovil.ui.home.HomeScreen
//import com.tesis.appmovil.ui.search.BuscarScreen
//import com.tesis.appmovil.ui.servicios.EditServiceScreen
//import com.tesis.appmovil.viewmodel.*
//
//// ----------------- Rutas -----------------
//sealed class Dest(
//    val route: String,
//    val label: String = "",
//    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null
//) {
//    // flujo auth
//    object Login : Dest("login")
//    object Register : Dest("register")
//
//    // flujo negocio (registro)
//    object RegisterBusiness : Dest("registerBusiness")
//    object BusinessContact : Dest("businessContact")
//    object BusinessSchedule : Dest("businessSchedule")
//    object BusinessImages : Dest("businessImages")
//    object BusinessLocation : Dest("businessLocation")
//    object BusinessDocuments : Dest("businessDocuments")
//    object BusinessReady : Dest("businessReady")
//
//    // flujo principal con bottom bar
//    object Home : Dest("home", "Inicio", Icons.Outlined.Home)
//    object Search : Dest("search", "Buscar", Icons.Outlined.Search)
//    object Business : Dest("business", "Negocio", Icons.Default.Store)
//}
//
///**
// * Root del app
// */
//@Composable
//fun AppRoot() {
//    val nav = rememberNavController()
//
//    NavHost(navController = nav, startDestination = "main") {
//        composable("main") {
//            MainWithBottomBar()
//        }
//    }
//}
//
///**
// * Bottom bar con 3 pestañas persistentes - VERSIÓN CORREGIDA
// */
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun MainWithBottomBar() {
//    val innerNav = rememberNavController()
//    val items = listOf(Dest.Home, Dest.Search, Dest.Business)
//    val backStack by innerNav.currentBackStackEntryAsState()
//    val current = backStack?.destination?.route
//
//    val authViewModel: AuthViewModel = viewModel()
//
//    // Ocultar bottom bar en ciertas rutas
//    val hideBottomBarRoutes = listOf(
//        Dest.Login.route,
//        Dest.Register.route,
//        "registerBusinessFlow"
//    )
//    val showBottomBar = current !in hideBottomBarRoutes
//
//    Scaffold(
//        topBar = {
//            if (current in listOf(
//                    Dest.Login.route,
//                    Dest.Register.route,
//                    Dest.RegisterBusiness.route
//                )
//            ) {
//                TopAppBar(
//                    title = {},
//                    actions = {
//                        IconButton(onClick = {
//                            innerNav.navigate(Dest.Home.route) {
//                                popUpTo(Dest.Home.route) { inclusive = true }
//                            }
//                        }) {
//                            Icon(
//                                imageVector = Icons.Default.Close,
//                                contentDescription = "Cerrar"
//                            )
//                        }
//                    }
//                )
//            }
//        },
//        bottomBar = {
//            if (showBottomBar) {
//                NavigationBar {
//                    items.forEach { d ->
//                        NavigationBarItem(
//                            selected = current == d.route,
//                            onClick = {
//                                innerNav.navigate(d.route) {
//                                    popUpTo(innerNav.graph.startDestinationId) { saveState = true }
//                                    launchSingleTop = true
//                                    restoreState = true
//                                }
//                            },
//                            icon = { d.icon?.let { Icon(it, contentDescription = d.label) } },
//                            label = { Text(d.label) }
//                        )
//                    }
//                }
//            }
//        }
//    ) { padding ->
//        NavHost(
//            navController = innerNav,
//            startDestination = Dest.Home.route,
//            modifier = Modifier.padding(padding)
//        ) {
//            // HOME
//            composable(Dest.Home.route) {
//                val vm: ServicioViewModel = viewModel()
//                HomeScreen(vm, innerNav)
//            }
//
//            // SEARCH
//            composable(Dest.Search.route) {
//                val vmNegocios: HomeNegocioViewModel = viewModel()
//                val vmServicios: ServicioViewModel = viewModel()
//                BuscarScreen(vmNegocios, vmServicios) { id ->
//                    if (id > 0) innerNav.navigate("businessDetail/$id")
//                }
//            }
//
//            // BUSINESS DETAIL
//            composable(
//                route = "businessDetail/{idNegocio}",
//                arguments = listOf(navArgument("idNegocio") { type = NavType.IntType })
//            ) { backStackEntry ->
//                val idNegocio = backStackEntry.arguments?.getInt("idNegocio") ?: 0
//                val vm: NegocioViewModel = viewModel()
//                BusinessDetailScreen(idNegocio = idNegocio, vm = vm, onBack = {
//                    innerNav.popBackStack()
//                })
//            }
//
//            // BUSINESS LOGIN
//            composable(Dest.Business.route) {
//                LoginScreen(
//                    vm = authViewModel,
//                    onSuccess = { innerNav.navigate("registerBusinessFlow") },
//                    onNavigateToRegister = { innerNav.navigate(Dest.Register.route) }
//                )
//            }
//
//            // SUBNAVEGACIÓN DEL REGISTRO DE NEGOCIO - CORREGIDO
//            navigation(
//                route = "registerBusinessFlow",
//                startDestination = Dest.RegisterBusiness.route
//            ) {
//                composable(Dest.RegisterBusiness.route) { backStackEntry ->
//                    val parentEntry = remember(backStackEntry) {
//                        innerNav.getBackStackEntry("registerBusinessFlow")
//                    }
//                    val negocioVM: NegocioViewModel = viewModel(parentEntry)
//
//                    RegisterBusinessScreen(
//                        authViewModel = authViewModel,
//                        negocioViewModel = negocioVM,
//                        onContinue = { innerNav.navigate(Dest.BusinessContact.route) },
//                        onBack = {
//                            innerNav.navigate(Dest.Home.route) {
//                                popUpTo(Dest.Home.route) { inclusive = true }
//                            }
//                        }
//                    )
//                }
//
//                composable(Dest.BusinessContact.route) { backStackEntry ->
//                    val parentEntry = remember(backStackEntry) {
//                        innerNav.getBackStackEntry("registerBusinessFlow")
//                    }
//                    val negocioVM: NegocioViewModel = viewModel(parentEntry)
//
//                    BusinessContactInfoScreen(
//                        negocioViewModel = negocioVM,
//                        onContinue = { innerNav.navigate(Dest.BusinessSchedule.route) },
//                        onBack = { innerNav.popBackStack() }
//                    )
//                }
//
//                composable(Dest.BusinessSchedule.route) { backStackEntry -> // ← AGREGAR backStackEntry
//                    val parentEntry = remember(backStackEntry) {
//                        innerNav.getBackStackEntry("registerBusinessFlow")
//                    }
//                    val negocioVM: NegocioViewModel = viewModel(parentEntry) // ← DEFINIR negocioVM aquí
//
//                    BusinessScheduleScreen(
//                        negocioViewModel = negocioVM, // ← PASA el ViewModel
//                        onContinue = { innerNav.navigate(Dest.BusinessImages.route) },
//                        onBack = { innerNav.popBackStack() }
//                    )
//                }
//
//                composable(Dest.BusinessImages.route) { backStackEntry -> // ← AGREGAR backStackEntry
//                    val parentEntry = remember(backStackEntry) {
//                        innerNav.getBackStackEntry("registerBusinessFlow")
//                    }
//                    val negocioVM: NegocioViewModel = viewModel(parentEntry) // ← DEFINIR negocioVM aquí
//
//                    BusinessImagesScreen(
//                        negocioViewModel = negocioVM, // ← PASA el ViewModel (si la pantalla lo necesita)
//                        onContinue = { innerNav.navigate(Dest.BusinessLocation.route) },
//                        onBack = { innerNav.popBackStack() }
//                    )
//                }
//
//                composable(Dest.BusinessLocation.route) { backStackEntry -> // ← AGREGAR backStackEntry
//                    val parentEntry = remember(backStackEntry) {
//                        innerNav.getBackStackEntry("registerBusinessFlow")
//                    }
//                    val negocioVM: NegocioViewModel = viewModel(parentEntry) // ← DEFINIR negocioVM aquí
//
//                    BusinessLocationScreen(
//                        negocioViewModel = negocioVM, // ← PASA el ViewModel (si la pantalla lo necesita)
//                        onLocationSelected = { innerNav.navigate(Dest.BusinessDocuments.route) },
//                        onBack = { innerNav.popBackStack() }
//                    )
//                }
//
//                composable(Dest.BusinessDocuments.route) { backStackEntry -> // ← AGREGAR backStackEntry
//                    val parentEntry = remember(backStackEntry) {
//                        innerNav.getBackStackEntry("registerBusinessFlow")
//                    }
//                    val negocioVM: NegocioViewModel = viewModel(parentEntry) // ← DEFINIR negocioVM aquí
//
//                    BusinessDocumentsScreen(
//                        negocioViewModel = negocioVM, // ← PASA el ViewModel (si la pantalla lo necesita)
//                        onContinue = { innerNav.navigate(Dest.BusinessReady.route) },
//                        onSkip = { innerNav.navigate(Dest.BusinessReady.route) },
//                        onBack = { innerNav.popBackStack() }
//                    )
//                }
//
//                composable(Dest.BusinessReady.route) { backStackEntry -> // ← AGREGAR backStackEntry
//                    val parentEntry = remember(backStackEntry) {
//                        innerNav.getBackStackEntry("registerBusinessFlow")
//                    }
//                    val negocioVM: NegocioViewModel = viewModel(parentEntry) // ← DEFINIR negocioVM aquí
//
//                    BusinessReadyScreen(
//                        negocioViewModel = negocioVM, // ← PASA el ViewModel (si la pantalla lo necesita)
//                        onPublish = {
//                            innerNav.navigate(Dest.Home.route) {
//                                popUpTo(Dest.Home.route) { inclusive = true }
//                                launchSingleTop = true
//                            }
//                        }
//                    )
//                }
//            }
//
//            // REGISTER USER
////            composable(Dest.Register.route) {
////                RegisterScreen(
////                    vm = authViewModel,
////                    onSuccess = { innerNav.popBackStack(Dest.Business.route, false) },
////                    onNavigateToLogin = { innerNav.popBackStack() }
////                )
////            }
//            // REGISTER USER - CORREGIDO
//            // REGISTER USER - CORREGIDO
//            composable(Dest.Register.route) {
//                RegisterScreen(
//                    vm = authViewModel,
//                    onSuccess = {
//                        // Después de registrar usuario, ir al LOGIN para que inicie sesión
//                        innerNav.navigate(Dest.Login.route) {
//                            // Esto limpia el stack hasta la ruta de login
//                            popUpTo(0) { inclusive = true }
//                        }
//                    },
//                    onNavigateToLogin = { innerNav.popBackStack() }
//                )
//            }
//
//            // EDIT SERVICE
//            composable(
//                route = "editService/{id}",
//                arguments = listOf(navArgument("id") { type = NavType.IntType })
//            ) { backStackEntry ->
//                val id = backStackEntry.arguments?.getInt("id") ?: 0
//                val vm: ServicioViewModel = viewModel()
//                EditServiceScreen(servicioId = id, vm = vm, navController = innerNav)
//            }
//        }
//    }
//}
//package com.tesis.appmovil.ui
//
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material.icons.filled.Store
//import androidx.compose.material.icons.outlined.Home
//import androidx.compose.material.icons.outlined.Search
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavType
//import androidx.navigation.compose.*
//import androidx.navigation.navigation
//import androidx.navigation.navArgument
//import com.tesis.appmovil.ui.auth.LoginScreen
//import com.tesis.appmovil.ui.auth.RegisterScreen
//import com.tesis.appmovil.ui.business.*
//import com.tesis.appmovil.ui.home.BusinessDetailScreen
//import com.tesis.appmovil.ui.home.HomeScreen
//import com.tesis.appmovil.ui.search.BuscarScreen
//import com.tesis.appmovil.ui.servicios.EditServiceScreen
//import com.tesis.appmovil.viewmodel.*
//
//// ----------------- Rutas -----------------
//sealed class Dest(
//    val route: String,
//    val label: String = "",
//    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null
//) {
//    // flujo auth
//    object Login : Dest("login")
//    object Register : Dest("register")
//
//    // flujo negocio (registro)
//    object RegisterBusiness : Dest("registerBusiness")
//    object BusinessContact : Dest("businessContact")
//    object BusinessSchedule : Dest("businessSchedule")
//    object BusinessImages : Dest("businessImages")
//    object BusinessLocation : Dest("businessLocation")
//    object BusinessDocuments : Dest("businessDocuments")
//    object BusinessReady : Dest("businessReady")
//
//    // flujo principal con bottom bar
//    object Home : Dest("home", "Inicio", Icons.Outlined.Home)
//    object Search : Dest("search", "Buscar", Icons.Outlined.Search)
//    object Business : Dest("business", "Negocio", Icons.Default.Store)
//}
//
///**
// * Root del app
// */
//@Composable
//fun AppRoot() {
//    val nav = rememberNavController()
//
//    NavHost(navController = nav, startDestination = "main") {
//        composable("main") {
//            MainWithBottomBar()
//        }
//    }
//}
//
///**
// * Bottom bar con 3 pestañas persistentes
// */
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun MainWithBottomBar() {
//    val innerNav = rememberNavController()
//    val items = listOf(Dest.Home, Dest.Search, Dest.Business)
//    val backStack by innerNav.currentBackStackEntryAsState()
//    val current = backStack?.destination?.route
//
//    val authViewModel: AuthViewModel = viewModel()
//
//    // Ocultar bottom bar en ciertas rutas
//    val hideBottomBarRoutes = listOf(
//        Dest.Login.route,
//        Dest.Register.route,
//        "registerBusinessFlow"
//    )
//    val showBottomBar = current !in hideBottomBarRoutes
//
//    Scaffold(
//        topBar = {
//            if (current in listOf(
//                    Dest.Login.route,
//                    Dest.Register.route,
//                    Dest.RegisterBusiness.route
//                )
//            ) {
//                TopAppBar(
//                    title = {},
//                    actions = {
//                        IconButton(onClick = {
//                            innerNav.navigate(Dest.Home.route) {
//                                popUpTo(Dest.Home.route) { inclusive = true }
//                            }
//                        }) {
//                            Icon(
//                                imageVector = Icons.Default.Close,
//                                contentDescription = "Cerrar"
//                            )
//                        }
//                    }
//                )
//            }
//        },
//        bottomBar = {
//            if (showBottomBar) {
//                NavigationBar {
//                    items.forEach { d ->
//                        NavigationBarItem(
//                            selected = current == d.route,
//                            onClick = {
//                                innerNav.navigate(d.route) {
//                                    popUpTo(innerNav.graph.startDestinationId) { saveState = true }
//                                    launchSingleTop = true
//                                    restoreState = true
//                                }
//                            },
//                            icon = { d.icon?.let { Icon(it, contentDescription = d.label) } },
//                            label = { Text(d.label) }
//                        )
//                    }
//                }
//            }
//        }
//    ) { padding ->
//        NavHost(
//            navController = innerNav,
//            startDestination = Dest.Home.route,
//            modifier = Modifier.padding(padding)
//        ) {
//            // HOME
//            composable(Dest.Home.route) {
//                val vm: ServicioViewModel = viewModel()
//                HomeScreen(vm, innerNav)
//            }
//
//            // SEARCH
//            composable(Dest.Search.route) {
//                val vmNegocios: HomeNegocioViewModel = viewModel()
//                val vmServicios: ServicioViewModel = viewModel()
//                BuscarScreen(vmNegocios, vmServicios) { id ->
//                    if (id > 0) innerNav.navigate("businessDetail/$id")
//                }
//            }
//
//            // BUSINESS DETAIL
//            composable(
//                route = "businessDetail/{idNegocio}",
//                arguments = listOf(navArgument("idNegocio") { type = NavType.IntType })
//            ) { backStackEntry ->
//                val idNegocio = backStackEntry.arguments?.getInt("idNegocio") ?: 0
//                val vm: NegocioViewModel = viewModel()
//                BusinessDetailScreen(idNegocio = idNegocio, vm = vm, onBack = {
//                    innerNav.popBackStack()
//                })
//            }
//
//            // BUSINESS LOGIN
//            composable(Dest.Business.route) {
//                LoginScreen(
//                    vm = authViewModel,
//                    onSuccess = { innerNav.navigate("registerBusinessFlow") },
//                    onNavigateToRegister = { innerNav.navigate(Dest.Register.route) }
//                )
//            }
//
//            // SUBNAVEGACIÓN DEL REGISTRO DE NEGOCIO
//            navigation(
//                route = "registerBusinessFlow",
//                startDestination = Dest.RegisterBusiness.route
//            ) {
//                composable(Dest.RegisterBusiness.route) { backStackEntry ->
//                    val parentEntry = remember(backStackEntry) {
//                        innerNav.getBackStackEntry("registerBusinessFlow")
//                    }
//                    val negocioVM: NegocioViewModel = viewModel(parentEntry)
//
//                    RegisterBusinessScreen(
//                        authViewModel = authViewModel,
//                        negocioViewModel = negocioVM,
//                        onContinue = { innerNav.navigate(Dest.BusinessContact.route) },
//                        onBack = {
//                            innerNav.navigate(Dest.Home.route) {
//                                popUpTo(Dest.Home.route) { inclusive = true }
//                            }
//                        }
//                    )
//                }
//
//                composable(Dest.BusinessContact.route) { backStackEntry ->
//                    val parentEntry = remember(backStackEntry) {
//                        innerNav.getBackStackEntry("registerBusinessFlow")
//                    }
//                    val negocioVM: NegocioViewModel = viewModel(parentEntry)
//
//                    BusinessContactInfoScreen(
//                        negocioViewModel = negocioVM,
//                        onContinue = { innerNav.navigate(Dest.BusinessSchedule.route) },
//                        onBack = { innerNav.popBackStack() }
//                    )
//                }
//
//                composable(Dest.BusinessSchedule.route) {
//                    BusinessScheduleScreen(
//                        negocioViewModel = negocioVM,
//                        onContinue = { innerNav.navigate(Dest.BusinessImages.route) },
//                        onBack = { innerNav.popBackStack() }
//                    )
//                }
//
//                composable(Dest.BusinessImages.route) {
//                    BusinessImagesScreen(
//                        onContinue = { innerNav.navigate(Dest.BusinessLocation.route) },
//                        onBack = { innerNav.popBackStack() }
//                    )
//                }
//
//                composable(Dest.BusinessLocation.route) {
//                    BusinessLocationScreen(
//                        onLocationSelected = { innerNav.navigate(Dest.BusinessDocuments.route) },
//                        onBack = { innerNav.popBackStack() }
//                    )
//                }
//
//                composable(Dest.BusinessDocuments.route) {
//                    BusinessDocumentsScreen(
//                        onContinue = { innerNav.navigate(Dest.BusinessReady.route) },
//                        onSkip = { innerNav.navigate(Dest.BusinessReady.route) },
//                        onBack = { innerNav.popBackStack() }
//                    )
//                }
//
//                composable(Dest.BusinessReady.route) {
//                    BusinessReadyScreen(
//                        onPublish = {
//                            innerNav.navigate(Dest.Home.route) {
//                                popUpTo(Dest.Home.route) { inclusive = true }
//                                launchSingleTop = true
//                            }
//                        }
//                    )
//                }
//            }
//
//            // REGISTER USER
//            composable(Dest.Register.route) {
//                RegisterScreen(
//                    vm = authViewModel,
//                    onSuccess = { innerNav.popBackStack(Dest.Business.route, false) },
//                    onNavigateToLogin = { innerNav.popBackStack() }
//                )
//            }
//
//            // EDIT SERVICE
//            composable(
//                route = "editService/{id}",
//                arguments = listOf(navArgument("id") { type = NavType.IntType })
//            ) { backStackEntry ->
//                val id = backStackEntry.arguments?.getInt("id") ?: 0
//                val vm: ServicioViewModel = viewModel()
//                EditServiceScreen(servicioId = id, vm = vm, navController = innerNav)
//            }
//        }
//    }
//}

//package com.tesis.appmovil.ui
//
//import androidx.compose.foundation.layout.padding
//import com.tesis.appmovil.ui.business.RegisterBusinessScreen
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material.icons.filled.Store
//import androidx.compose.material.icons.outlined.AccountCircle
//import androidx.compose.material.icons.outlined.Home
//import androidx.compose.material.icons.outlined.Search
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.NavigationBar
//import androidx.compose.material3.NavigationBarItem
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.saveable.rememberSaveable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.alpha
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.zIndex
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavType
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.currentBackStackEntryAsState
//import androidx.navigation.compose.rememberNavController
//import androidx.navigation.navArgument
//import com.tesis.appmovil.models.UserRole
//import com.tesis.appmovil.ui.auth.ChooseRoleScreen
//import com.tesis.appmovil.ui.auth.LoginScreen
//import com.tesis.appmovil.ui.auth.RegisterScreen
//import com.tesis.appmovil.ui.business.BusinessContactInfoScreen
//import com.tesis.appmovil.ui.business.BusinessDocumentsScreen
//import com.tesis.appmovil.ui.business.BusinessImagesScreen
//import com.tesis.appmovil.ui.business.BusinessLocationScreen
//import com.tesis.appmovil.ui.business.BusinessScheduleScreen
//import com.tesis.appmovil.ui.home.BusinessDetailScreen
//import com.tesis.appmovil.ui.home.HomeScreen
//import com.tesis.appmovil.ui.search.BuscarScreen     // <- usa la pantalla con controles
//import com.tesis.appmovil.ui.servicios.EditServiceScreen
//import com.tesis.appmovil.viewmodel.AuthViewModel
//import com.tesis.appmovil.viewmodel.HomeNegocioViewModel
//import com.tesis.appmovil.viewmodel.HomeViewModel
//import com.tesis.appmovil.viewmodel.NegocioViewModel
//import com.tesis.appmovil.viewmodel.ServicioViewModel
//
//// ----------------- Rutas -----------------
//sealed class Dest(
//    val route: String,
//    val label: String = "",
//    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null
//) {
//    // flujo auth
//    object Login : Dest("login")
//    object Register : Dest("register")
//
//
//
//    // flujo registro negocio
//
//    object RegisterBusiness : Dest("registerBusiness")
//    object BusinessContact : Dest("businessContact")
//    object BusinessSchedule : Dest("businessSchedule")
//    object BusinessImages : Dest("businessImages")
//    object BusinessLocation : Dest("businessLocation")
//    object BusinessDocuments : Dest("businessDocuments")
//    object BusinessReady : Dest("bussinessReady")
//
//
//
//    // flujo principal con bottom bar
//    object Home : Dest("home", "Inicio", Icons.Outlined.Home)
//    object Search : Dest("search", "Buscar", Icons.Outlined.Search)
//    object Business : Dest("business", "Negocio", Icons.Default.Store)
//
//
//}
//
///* ----------------- Root ----------------- */
//@Composable
//fun AppRoot() {
//    val nav = rememberNavController()
//
//    NavHost(navController = nav, startDestination = "main") {
//        composable("main") {
//            MainWithBottomBar()
//        }
//    }
//}
//
///* ----------------- BottomBar ----------------- */
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun MainWithBottomBar() {
//    val innerNav = rememberNavController()
//    val items = listOf(Dest.Home, Dest.Search, Dest.Business)
//    val backStack by innerNav.currentBackStackEntryAsState()
//    val current = backStack?.destination?.route
//
//    val authViewModel: AuthViewModel = viewModel()
//
//
//    val negocioViewModel: NegocioViewModel = viewModel()
//
//
//    // Rutas donde NO queremos mostrar el bottom bar
//    val hideBottomBarRoutes = listOf(
//        Dest.Login.route,
//        Dest.Register.route,
//        Dest.RegisterBusiness.route,
//        Dest.BusinessContact.route,
//        Dest.Business.route,
//        Dest.BusinessSchedule.route,
//        Dest.BusinessImages.route,
//        Dest.BusinessLocation.route,
//        Dest.BusinessDocuments.route,
//        Dest.BusinessReady.route
//
//    )
//    val showBottomBar = current !in hideBottomBarRoutes
//
//    // Rutas donde sí queremos mostrar el botón de cerrar
//    val showCloseIconRoutes = listOf(
//        Dest.Login.route,
//        Dest.Register.route,
//        Dest.RegisterBusiness.route,
//        Dest.Business.route
//    )
//
//    Scaffold(
//        topBar = {
//            if (current in showCloseIconRoutes) {
//                androidx.compose.material3.TopAppBar(
//                    title = {},
//                    actions = {
//                        IconButton(onClick = {
//                            // Al presionar la X → vuelve al Home
//                            innerNav.navigate(Dest.Home.route) {
//                                popUpTo(Dest.Home.route) { inclusive = true }
//                            }
//                        }) {
//                            Icon(
//                                imageVector = Icons.Default.Close,
//                                contentDescription = "Cerrar"
//                            )
//                        }
//                    }
//                )
//            }
//        },
//        bottomBar = {
//            if (showBottomBar) {
//                NavigationBar {
//                    items.forEach { d ->
//                        NavigationBarItem(
//                            selected = current == d.route,
//                            onClick = {
//                                innerNav.navigate(d.route) {
//                                    popUpTo(innerNav.graph.startDestinationId) { saveState = true }
//                                    launchSingleTop = true
//                                    restoreState = true
//                                }
//                            },
//                            icon = { d.icon?.let { Icon(it, contentDescription = d.label) } },
//                            label = { Text(d.label) }
//                        )
//                    }
//                }
//            }
//        }
//    )
//    { padding ->
//        NavHost(
//            navController = innerNav,
//            startDestination = Dest.Home.route,
//            modifier = Modifier.padding(padding)
//        ) {
//            /* ---------- Home ---------- */
//            composable(Dest.Home.route) {
//                val vm: ServicioViewModel = viewModel()
//                HomeScreen(vm, innerNav)   // 👈 sigue recibiendo innerNav
//            }
//
//            /* ---------- Search ---------- */
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
//
//
//            /* ---------- Business Detail ---------- */
//            composable(
//                route = "businessDetail/{idNegocio}",
//                arguments = listOf(navArgument("idNegocio") { type = NavType.IntType })
//            ) { backStackEntry ->
//                val idNegocio = backStackEntry.arguments?.getInt("idNegocio") ?: 0
//                val vm: NegocioViewModel = viewModel()
//                BusinessDetailScreen(idNegocio = idNegocio, vm = vm, onBack = {
//                    innerNav.popBackStack()
//                }
//                )
//            }
//
//
//            /* ---------- Login Business ---------- */
//            composable(Dest.Business.route) {
//                LoginScreen(
//                    vm = authViewModel,  // ← MISMA instancia
//                    onSuccess = {
//                        innerNav.navigate(Dest.RegisterBusiness.route)
//                    },
//                    onNavigateToRegister = {
//                        innerNav.navigate(Dest.Register.route)
//                    }
//                )
//            }
//
//            /* ---------- Register User ---------- */
//            composable(Dest.Register.route) {
//                RegisterScreen(
//                    vm = authViewModel,  // ← MISMA instancia
//                    onSuccess = {
//                        innerNav.popBackStack(Dest.Business.route, false)
//                    },
//                    onNavigateToLogin = {
//                        innerNav.popBackStack()
//                    }
//                )
//            }
//            composable(Dest.RegisterBusiness.route) {
//                RegisterBusinessScreen(
//                    authViewModel = authViewModel,  // ← PASAR el ViewModel
//                    negocioViewModel = negocioViewModel,
//                    onContinue = {
//                        innerNav.navigate(Dest.BusinessContact.route)
//                    },
//                    onBack = {
//                        innerNav.navigate(Dest.Home.route) {
//                            popUpTo(Dest.Home.route) { inclusive = true }
//                        }
//                    }
//                )
//            }
//
//            /* ---------- Subflujo: Registro de Negocio ---------- */
//
//            navigation(
//                route = "registerBusinessFlow",
//                startDestination = Dest.RegisterBusiness.route
//            ) {
//                composable(Dest.RegisterBusiness.route) {
//                    val negocioVM: NegocioViewModel =
//                        viewModel(innerNav.getBackStackEntry("registerBusinessFlow"))
//                    RegisterBusinessScreen(
//                        authViewModel = authViewModel,
//                        negocioViewModel = negocioVM,
//                        onContinue = { innerNav.navigate(Dest.BusinessContact.route) },
//                        onBack = {
//                            innerNav.navigate(Dest.Home.route) {
//                                popUpTo(Dest.Home.route) { inclusive = true }
//                            }
//                        }
//                    )
//                }
//
//                composable(Dest.BusinessContact.route) {
//                    val negocioVM: NegocioViewModel =
//                        viewModel(innerNav.getBackStackEntry("registerBusinessFlow"))
//                    BusinessContactInfoScreen(
//                        negocioViewModel = negocioVM,
//                        onContinue = { innerNav.navigate(Dest.BusinessSchedule.route) },
//                        onBack = { innerNav.popBackStack() }
//                    )
//                }
//
//                composable(Dest.BusinessSchedule.route) {
//                    val negocioVM: NegocioViewModel =
//                        viewModel(innerNav.getBackStackEntry("registerBusinessFlow"))
//                    BusinessScheduleScreen(
//                        negocioViewModel = negocioVM,
//                        onContinue = { innerNav.navigate(Dest.BusinessImages.route) },
//                        onBack = { innerNav.popBackStack() }
//                    )
//                }
//
//                composable(Dest.BusinessImages.route) {
//                    val negocioVM: NegocioViewModel =
//                        viewModel(innerNav.getBackStackEntry("registerBusinessFlow"))
//                    BusinessImagesScreen(
//                        negocioViewModel = negocioVM,
//                        onContinue = { innerNav.navigate(Dest.BusinessLocation.route) },
//                        onBack = { innerNav.popBackStack() }
//                    )
//                }
//
//                composable(Dest.BusinessLocation.route) {
//                    val negocioVM: NegocioViewModel =
//                        viewModel(innerNav.getBackStackEntry("registerBusinessFlow"))
//                    BusinessLocationScreen(
//                        negocioViewModel = negocioVM,
//                        onLocationSelected = {
//                            innerNav.navigate(Dest.BusinessDocuments.route)
//                        },
//                        onBack = { innerNav.popBackStack() }
//                    )
//                }
//
//                composable(Dest.BusinessDocuments.route) {
//                    val negocioVM: NegocioViewModel =
//                        viewModel(innerNav.getBackStackEntry("registerBusinessFlow"))
//                    BusinessDocumentsScreen(
//                        negocioViewModel = negocioVM,
//                        onContinue = { innerNav.navigate(Dest.BusinessReady.route) },
//                        onSkip = { innerNav.navigate(Dest.BusinessReady.route) },
//                        onBack = { innerNav.popBackStack() }
//                    )
//                }
//
//                composable(Dest.BusinessReady.route) {
//                    val negocioVM: NegocioViewModel =
//                        viewModel(innerNav.getBackStackEntry("registerBusinessFlow"))
//                    BusinessReadyScreen(
//                        negocioViewModel = negocioVM,
//                        onPublish = {
//                            innerNav.navigate(Dest.Home.route) {
//                                popUpTo(Dest.Home.route) { inclusive = true }
//                                launchSingleTop = true
//                            }
//                        }
//                    )
//                }
//            }
//
//
//            composable(Dest.BusinessContact.route) {
//                BusinessContactInfoScreen(
//                    negocioViewModel = negocioViewModel,
//                    onContinue = {
//                        // Por ahora al terminar → vuelve al inicio
//                        innerNav.navigate(Dest.BusinessSchedule.route)
////                        {
////                            popUpTo(Dest.Home.route) { inclusive = true }
////                        }
//                    },
//                    onBack = {
//                        innerNav.popBackStack() // vuelve a RegisterBusiness
//                    }
//                )
//            }
//            composable(Dest.BusinessSchedule.route) {
//                BusinessScheduleScreen(
//                    onContinue = {
//                        innerNav.navigate(Dest.BusinessImages.route)
//                    },
//                    onBack = {
//                        innerNav.popBackStack() // vuelve a la pantalla anterior (BusinessContact)
//                    }
//                )
//            }
//            composable(Dest.BusinessImages.route) {
//                BusinessImagesScreen(
//                    onContinue = {
//                        innerNav.navigate(Dest.BusinessLocation.route)
//
//                    },
//                    onBack = {
//                        innerNav.popBackStack() // vuelve a la pantalla anterior (BusinessSchedule)
//                    }
//                )
//            }
//
//            composable(Dest.BusinessLocation.route) {
//                BusinessLocationScreen(
//                    onLocationSelected = { latLng ->
//                        // Aquí guardas la ubicación seleccionada
//                        println("📍 Ubicación guardada: $latLng")
//                        // Puedes navegar a la siguiente pantalla o volver atrás
//                        innerNav.navigate(Dest.BusinessDocuments.route)
//                    },
//                    onBack = {
//                        innerNav.popBackStack() // vuelve a la pantalla anterior
//                    }
//                )
//            }
//            composable(Dest.BusinessDocuments.route) {
//                BusinessDocumentsScreen(
//                    onContinue = {
//                        // Cuando se suben documentos y se hace click en continuar
//                        innerNav.navigate(Dest.BusinessReady.route)
//                    },
//                    onSkip = {
//                        // Cuando se omite la subida de documentos
//                        innerNav.navigate(Dest.BusinessReady.route)
//                    },
//                    onBack = {
//                        innerNav.popBackStack()
//                    }
//                )
//            }
//
//            composable(Dest.BusinessReady.route) {
//                com.tesis.appmovil.ui.business.BusinessReadyScreen(
//                    onPublish = {
//                        // Si necesitas, aquí llamas a tu ViewModel para publicar
//                        // viewModel.publishBusiness()
//
//                        // Luego manda al Home dentro del mismo NavHost interno
//                        innerNav.navigate(Dest.Home.route) {
//                            popUpTo(Dest.Home.route) { inclusive = true }
//                            launchSingleTop = true
//                        }
//                    }
//                )
//            }
//
//
//
//            composable(
//                route = "businessDetail/{idNegocio}",
//                arguments = listOf(navArgument("idNegocio") { type = NavType.IntType })
//            ) { backStackEntry ->
//                val idNegocio = backStackEntry.arguments?.getInt("idNegocio") ?: 0
//                val vm: NegocioViewModel = viewModel()
//                BusinessDetailScreen(idNegocio = idNegocio, vm = vm, onBack = { innerNav.popBackStack() })
//            }
//
//            // AÑADE AQUÍ la nueva ruta para editar servicio:
//            composable(
//                route = "editService/{id}",
//                arguments = listOf(navArgument("id") { type = NavType.IntType })
//            ) { backStackEntry ->
//                val id = backStackEntry.arguments?.getInt("id") ?: 0
//                val vm: ServicioViewModel = viewModel()
//                EditServiceScreen(servicioId = id, vm = vm, navController = innerNav)
//            }
//
//
//        }
//    }
//}
//
