package com.example.appprotocolos_eq014.ui.screens.inicio

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.appprotocolos_eq014.viewmodel.InicioViewModel

@Composable
fun InicioScreen(
    viewModel: InicioViewModel,
    onNavigateToNuevoProtocolo: () -> Unit,
    onNavigateToGestionUsuarios: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator()
        }
    } else {
        when (uiState.rolUsuario) {
            "CLIENTE" -> {
                InicioCliente(
                    uiState = uiState,
                    onBusquedaCambia = { viewModel.onBusquedaCambia(it) }
                )
            }
            "TECNICO", "INGENIERO" -> {
                InicioPersonalTerreno(
                    uiState = uiState,
                    onNuevoProtocoloClick = onNavigateToNuevoProtocolo
                )
            }
            "JEFE_TECNICO" -> {
                InicioJefeTecnico(
                    uiState = uiState,
                    onAgregarUsuarioClick = onNavigateToGestionUsuarios
                )
            }
            else -> {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}