package com.example.appprotocolos_eq014.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.appprotocolos_eq014.data.SesionPreferencias
import com.example.appprotocolos_eq014.ui.screens.LoginScreen
import com.example.appprotocolos_eq014.ui.screens.inicio.InicioScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.appprotocolos_eq014.ui.screens.usuarios.FichaUsuarioScreen
import com.example.appprotocolos_eq014.ui.screens.usuarios.GestionUsuariosScreen
import com.example.appprotocolos_eq014.viewmodel.ARG_ID_USUARIO
import com.example.appprotocolos_eq014.viewmodel.FichaUsuarioViewModel
import com.example.appprotocolos_eq014.viewmodel.GestionUsuariosViewModel
import com.example.appprotocolos_eq014.ui.screens.perfil.EditarPerfilScreen
import com.example.appprotocolos_eq014.ui.screens.perfil.PerfilScreen
import com.example.appprotocolos_eq014.viewmodel.EditarPerfilViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appprotocolos_eq014.viewmodel.AppViewModelFactory
import com.example.appprotocolos_eq014.viewmodel.InicioViewModel
import com.example.appprotocolos_eq014.viewmodel.LoginViewModel
import com.example.appprotocolos_eq014.viewmodel.PerfilViewModel

object Rutas {
    const val LOGIN = "login"
    const val INICIO = "inicio"
    const val PERFIL = "perfil"
    const val EDITAR_PERFIL = "editar_perfil"
    const val FICHA_USUARIO = "ficha_usuario/{$ARG_ID_USUARIO}"

    fun fichaUsuario(idUsuario: Long) = "ficha_usuario/$idUsuario"
}

private const val CLAVE_PERFIL_ACTUALIZADO = "perfil_actualizado"
private const val CLAVE_USUARIO_GUARDADO = "usuario_guardado"

private val rutasConBarraInferior = setOf(Rutas.INICIO, Rutas.PERFIL)

@Composable
fun AppNavegacion(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route
    val contexto = LocalContext.current
    val esCliente = remember(rutaActual) {
        SesionPreferencias(contexto).leerRolUsuario() == "CLIENTE"
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (rutaActual != null && rutaActual in rutasConBarraInferior) {
                BarraNavegacionInferior(
                    destinos = destinosInferiores(esCliente),
                    rutaActual = rutaActual,
                    onNavegar = { ruta -> navController.navegarPestana(ruta) }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Rutas.LOGIN,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn() + slideInHorizontally { it / 8 } },
            exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() },
            popExitTransition = { fadeOut() }
        ) {
            composable(Rutas.LOGIN) {
                val loginViewModel: LoginViewModel = viewModel(factory = AppViewModelFactory.Factory)
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginExitoso = {
                        navController.navigate(Rutas.INICIO) {
                            popUpTo(Rutas.LOGIN) { inclusive = true }
                        }
                    }
                )
            }
            composable(Rutas.INICIO) { entrada ->
                val inicioViewModel: InicioViewModel = viewModel(factory = AppViewModelFactory.Factory)
                InicioScreen(
                    viewModel = inicioViewModel,
                    onNavigateToNuevoProtocolo = {},
                    contenidoJefeTecnico = {
                        val gestionViewModel: GestionUsuariosViewModel =
                            viewModel(factory = AppViewModelFactory.Factory)
                        val resultadoGuardado by entrada.savedStateHandle
                            .getStateFlow<Boolean?>(CLAVE_USUARIO_GUARDADO, null)
                            .collectAsStateWithLifecycle()
                        GestionUsuariosScreen(
                            viewModel = gestionViewModel,
                            onAgregarUsuario = {
                                navController.navigate(Rutas.fichaUsuario(FichaUsuarioViewModel.NUEVO))
                            },
                            onAbrirUsuario = { id -> navController.navigate(Rutas.fichaUsuario(id)) },
                            resultadoGuardado = resultadoGuardado,
                            onResultadoConsumido = {
                                entrada.savedStateHandle[CLAVE_USUARIO_GUARDADO] = null
                            }
                        )
                    }
                )
            }
            composable(
                route = Rutas.FICHA_USUARIO,
                arguments = listOf(navArgument(ARG_ID_USUARIO) { type = NavType.LongType })
            ) {
                val fichaViewModel: FichaUsuarioViewModel = viewModel(factory = AppViewModelFactory.Factory)
                FichaUsuarioScreen(
                    viewModel = fichaViewModel,
                    onVolver = { navController.popBackStack() },
                    onGuardado = { esNuevo ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(CLAVE_USUARIO_GUARDADO, esNuevo)
                        navController.popBackStack()
                    }
                )
            }
            composable(Rutas.PERFIL) { entrada ->
                val perfilViewModel: PerfilViewModel = viewModel(factory = AppViewModelFactory.Factory)
                val perfilActualizado by entrada.savedStateHandle
                    .getStateFlow(CLAVE_PERFIL_ACTUALIZADO, false)
                    .collectAsStateWithLifecycle()
                PerfilScreen(
                    viewModel = perfilViewModel,
                    onSesionCerrada = {
                        navController.navigate(Rutas.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                    onEditarPerfil = { navController.navigate(Rutas.EDITAR_PERFIL) },
                    perfilActualizado = perfilActualizado,
                    onPerfilActualizadoConsumido = {
                        entrada.savedStateHandle[CLAVE_PERFIL_ACTUALIZADO] = false
                    }
                )
            }
            composable(Rutas.EDITAR_PERFIL) {
                val editarPerfilViewModel: EditarPerfilViewModel = viewModel(factory = AppViewModelFactory.Factory)
                EditarPerfilScreen(
                    viewModel = editarPerfilViewModel,
                    onVolver = { navController.popBackStack() },
                    onGuardado = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(CLAVE_PERFIL_ACTUALIZADO, true)
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

private fun NavHostController.navegarPestana(ruta: String) {
    navigate(ruta) {
        popUpTo(Rutas.INICIO) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
