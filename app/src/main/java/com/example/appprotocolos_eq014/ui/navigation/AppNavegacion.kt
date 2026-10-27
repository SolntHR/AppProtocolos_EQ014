package com.example.appprotocolos_eq014.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appprotocolos_eq014.ui.screens.LoginScreen
import com.example.appprotocolos_eq014.ui.screens.inicio.InicioScreen
import com.example.appprotocolos_eq014.viewmodel.AppViewModelFactory
import com.example.appprotocolos_eq014.viewmodel.InicioViewModel
import com.example.appprotocolos_eq014.viewmodel.LoginViewModel

object Rutas {
    const val LOGIN = "login"
    const val INICIO = "inicio"
}

@Composable
fun AppNavegacion(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN,
        modifier = modifier
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
        composable(Rutas.INICIO) {
            val inicioViewModel: InicioViewModel = viewModel(factory = AppViewModelFactory.Factory)
            InicioScreen(
                viewModel = inicioViewModel,
                onNavigateToNuevoProtocolo = {},
                onNavigateToGestionUsuarios = {}
            )
        }
    }
}
