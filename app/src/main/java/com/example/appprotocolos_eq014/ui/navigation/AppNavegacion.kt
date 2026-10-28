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
import com.example.appprotocolos_eq014.viewmodel.ARG_ID_EQUIPO
import com.example.appprotocolos_eq014.viewmodel.ARG_ID_VISITA
import com.example.appprotocolos_eq014.viewmodel.ActaViewModel
import com.example.appprotocolos_eq014.viewmodel.InstrumentosViewModel
import com.example.appprotocolos_eq014.ui.screens.instrumentos.InstrumentosScreen
import com.example.appprotocolos_eq014.viewmodel.HistorialViewModel
import com.example.appprotocolos_eq014.viewmodel.RecuperarViewModel
import com.example.appprotocolos_eq014.ui.screens.RecuperarScreen
import com.example.appprotocolos_eq014.ui.screens.acta.ActaScreen
import com.example.appprotocolos_eq014.ui.screens.historial.HistorialScreen
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
import com.example.appprotocolos_eq014.viewmodel.FormularioProtocoloViewModel
import com.example.appprotocolos_eq014.viewmodel.ProtocolosViewModel
import com.example.appprotocolos_eq014.ui.screens.protocolo.DetalleProtocoloScreen
import com.example.appprotocolos_eq014.ui.screens.protocolo.FormularioProtocoloScreen
import com.example.appprotocolos_eq014.ui.screens.protocolo.ListaProtocolosScreen
import com.example.appprotocolos_eq014.ui.screens.catalogo.FormularioEquipoScreen
import com.example.appprotocolos_eq014.ui.screens.catalogo.FormularioInstalacionScreen
import com.example.appprotocolos_eq014.viewmodel.ARG_ID_INSTALACION
import com.example.appprotocolos_eq014.viewmodel.SIN_INSTALACION
import com.example.appprotocolos_eq014.viewmodel.FormularioEquipoViewModel
import com.example.appprotocolos_eq014.viewmodel.FormularioInstalacionViewModel

object Rutas {
    const val LOGIN = "login"
    const val INICIO = "inicio"
    const val PERFIL = "perfil"
    const val EDITAR_PERFIL = "editar_perfil"
    const val FICHA_USUARIO = "ficha_usuario/{$ARG_ID_USUARIO}"

    const val RECUPERAR = "recuperar"
    const val INSTRUMENTOS = "instrumentos"
    const val HISTORIAL = "historial/{$ARG_ID_EQUIPO}"
    const val ACTA = "acta/{$ARG_ID_VISITA}"

    // Registro de protocolos
    const val ARG_ID_PROTOCOLO = "idProtocolo"
    const val PROTOCOLOS = "protocolos"
    const val FORMULARIO = "formulario?$ARG_ID_PROTOCOLO={$ARG_ID_PROTOCOLO}"
    const val DETALLE_PROTOCOLO = "protocolo/{$ARG_ID_PROTOCOLO}"

    // Registro de instalaciones y equipos (RF-01, RF-02)
    const val EQUIPO_NUEVO = "equipo_nuevo?$ARG_ID_INSTALACION={$ARG_ID_INSTALACION}"
    const val INSTALACION_NUEVA = "instalacion_nueva"

    fun fichaUsuario(idUsuario: Long) = "ficha_usuario/$idUsuario"
    fun historial(idEquipo: Long) = "historial/$idEquipo"
    fun acta(idVisita: Long) = "acta/$idVisita"
    fun formulario(idProtocolo: String? = null) =
        if (idProtocolo == null) "formulario" else "formulario?$ARG_ID_PROTOCOLO=$idProtocolo"
    fun detalleProtocolo(idProtocolo: String) = "protocolo/$idProtocolo"
    fun equipoNuevo(idInstalacion: Long? = null) =
        if (idInstalacion == null) "equipo_nuevo" else "equipo_nuevo?$ARG_ID_INSTALACION=$idInstalacion"
}

private const val CLAVE_PERFIL_ACTUALIZADO = "perfil_actualizado"
private const val CLAVE_USUARIO_GUARDADO = "usuario_guardado"
private const val CLAVE_EQUIPO_CREADO = "equipo_creado"
private const val CLAVE_INSTALACION_CREADA = "instalacion_creada"

private val rutasConBarraInferior = setOf(Rutas.INICIO, Rutas.PROTOCOLOS, Rutas.INSTRUMENTOS, Rutas.PERFIL)

@Composable
fun AppNavegacion(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route
    val contexto = LocalContext.current
    val rolUsuario = remember(rutaActual) {
        SesionPreferencias(contexto).leerRolUsuario()
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (rutaActual != null && rutaActual in rutasConBarraInferior) {
                BarraNavegacionInferior(
                    destinos = destinosInferiores(rolUsuario),
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
                    },
                    onOlvidasteContrasena = { navController.navigate(Rutas.RECUPERAR) }
                )
            }
            composable(Rutas.INSTRUMENTOS) {
                val instrumentosViewModel: InstrumentosViewModel = viewModel(factory = AppViewModelFactory.Factory)
                InstrumentosScreen(viewModel = instrumentosViewModel)
            }
            composable(Rutas.RECUPERAR) {
                val recuperarViewModel: RecuperarViewModel = viewModel(factory = AppViewModelFactory.Factory)
                RecuperarScreen(
                    viewModel = recuperarViewModel,
                    onVolver = { navController.popBackStack() }
                )
            }
            composable(
                route = Rutas.HISTORIAL,
                arguments = listOf(navArgument(ARG_ID_EQUIPO) { type = NavType.LongType })
            ) {
                val historialViewModel: HistorialViewModel = viewModel(factory = AppViewModelFactory.Factory)
                HistorialScreen(
                    viewModel = historialViewModel,
                    onVolver = { navController.popBackStack() }
                )
            }
            composable(
                route = Rutas.ACTA,
                arguments = listOf(navArgument(ARG_ID_VISITA) { type = NavType.LongType })
            ) {
                val actaViewModel: ActaViewModel = viewModel(factory = AppViewModelFactory.Factory)
                ActaScreen(
                    viewModel = actaViewModel,
                    onVolver = { navController.popBackStack() },
                    onAbrirEquipo = { idEquipo -> navController.navigate(Rutas.historial(idEquipo)) }
                )
            }
            composable(Rutas.INICIO) { entrada ->
                val inicioViewModel: InicioViewModel = viewModel(factory = AppViewModelFactory.Factory)
                InicioScreen(
                    viewModel = inicioViewModel,
                    onAbrirVisita = { idVisita -> navController.navigate(Rutas.acta(idVisita)) },
                    onAbrirEquipo = { idEquipo -> navController.navigate(Rutas.historial(idEquipo)) },
                    onNuevoProtocolo = { navController.navigate(Rutas.formulario()) },
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
            // ---------- Registro de protocolos ----------
            composable(Rutas.PROTOCOLOS) {
                val protocolosViewModel: ProtocolosViewModel = viewModel(factory = AppViewModelFactory.Factory)
                ListaProtocolosScreen(
                    viewModel = protocolosViewModel,
                    onAbrirProtocolo = { id -> navController.navigate(Rutas.detalleProtocolo(id)) },
                    onNuevoProtocolo = { navController.navigate(Rutas.formulario()) },
                    onNuevoEquipo = { navController.navigate(Rutas.equipoNuevo()) },
                    onNuevaInstalacion = { navController.navigate(Rutas.INSTALACION_NUEVA) }
                )
            }
            composable(
                route = Rutas.FORMULARIO,
                arguments = listOf(navArgument(Rutas.ARG_ID_PROTOCOLO) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { entrada ->
                val formularioViewModel: FormularioProtocoloViewModel = viewModel(factory = AppViewModelFactory.Factory)
                val equipoCreado by entrada.savedStateHandle
                    .getStateFlow<Long?>(CLAVE_EQUIPO_CREADO, null)
                    .collectAsStateWithLifecycle()
                FormularioProtocoloScreen(
                    onRegistrarEquipo = { idInstalacion -> navController.navigate(Rutas.equipoNuevo(idInstalacion)) },
                    equipoCreado = equipoCreado,
                    onEquipoCreadoConsumido = { entrada.savedStateHandle[CLAVE_EQUIPO_CREADO] = null },
                    viewModel = formularioViewModel,
                    idVisita = null,
                    idProtocolo = entrada.arguments?.getString(Rutas.ARG_ID_PROTOCOLO),
                    onVolver = { navController.popBackStack() },
                    onGuardado = { id ->
                        // Se cierra el formulario y se abre el detalle del protocolo guardado
                        // Si se venia del detalle (edicion), tambien se reemplaza ese detalle
                        val desdeDetalle =
                            navController.previousBackStackEntry?.destination?.route == Rutas.DETALLE_PROTOCOLO
                        navController.navigate(Rutas.detalleProtocolo(id)) {
                            popUpTo(if (desdeDetalle) Rutas.DETALLE_PROTOCOLO else Rutas.FORMULARIO) { inclusive = true }
                        }
                    }
                )
            }
            composable(
                route = Rutas.DETALLE_PROTOCOLO,
                arguments = listOf(navArgument(Rutas.ARG_ID_PROTOCOLO) { type = NavType.StringType })
            ) { entrada ->
                val protocolosViewModel: ProtocolosViewModel = viewModel(factory = AppViewModelFactory.Factory)
                val id = entrada.arguments?.getString(Rutas.ARG_ID_PROTOCOLO).orEmpty()
                DetalleProtocoloScreen(
                    viewModel = protocolosViewModel,
                    idProtocolo = id,
                    onVolver = { navController.popBackStack() },
                    onEditar = { idEditar -> navController.navigate(Rutas.formulario(idEditar)) },
                    onAbrirProtocolo = { otro -> navController.navigate(Rutas.detalleProtocolo(otro)) }
                )
            }
            composable(
                route = Rutas.EQUIPO_NUEVO,
                arguments = listOf(navArgument(ARG_ID_INSTALACION) {
                    type = NavType.LongType
                    defaultValue = SIN_INSTALACION
                })
            ) { entrada ->
                val equipoViewModel: FormularioEquipoViewModel = viewModel(factory = AppViewModelFactory.Factory)
                val instalacionCreada by entrada.savedStateHandle
                    .getStateFlow<Long?>(CLAVE_INSTALACION_CREADA, null)
                    .collectAsStateWithLifecycle()
                FormularioEquipoScreen(
                    viewModel = equipoViewModel,
                    onVolver = { navController.popBackStack() },
                    onGuardado = { idEquipo ->
                        navController.previousBackStackEntry?.savedStateHandle?.set(CLAVE_EQUIPO_CREADO, idEquipo)
                        navController.popBackStack()
                    },
                    onNuevaInstalacion = { navController.navigate(Rutas.INSTALACION_NUEVA) },
                    instalacionCreada = instalacionCreada,
                    onInstalacionCreadaConsumida = { entrada.savedStateHandle[CLAVE_INSTALACION_CREADA] = null }
                )
            }
            composable(Rutas.INSTALACION_NUEVA) {
                val instalacionViewModel: FormularioInstalacionViewModel = viewModel(factory = AppViewModelFactory.Factory)
                FormularioInstalacionScreen(
                    viewModel = instalacionViewModel,
                    onVolver = { navController.popBackStack() },
                    onGuardado = { idInstalacion ->
                        navController.previousBackStackEntry?.savedStateHandle?.set(CLAVE_INSTALACION_CREADA, idInstalacion)
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
