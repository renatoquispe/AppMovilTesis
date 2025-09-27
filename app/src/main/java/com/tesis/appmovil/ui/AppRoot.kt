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
        "registerBusinessFlow",
        "businessDetail/{idNegocio}",
        "chatbot"
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

