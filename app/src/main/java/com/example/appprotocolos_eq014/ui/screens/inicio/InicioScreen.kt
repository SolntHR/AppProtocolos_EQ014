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
import com.example.appprotocolos_eq014.ui.components.RecargarAlVolver
import com.example.appprotocolos_eq014.ui.components.EstadoListado
import com.example.appprotocolos_eq014.model.catalogos.Rol

@Composable
fun InicioScreen(
    viewModel: InicioViewModel,
    onAbrirVisita: (Long) -> Unit,
    onAbrirEquipo: (Long) -> Unit,
    onNuevoProtocolo: () -> Unit,
    onAbrirPerfil: () -> Unit,
    onVerVisitas: () -> Unit,
    onVerEquipos: () -> Unit,
    onVerProtocolos: () -> Unit,
    onGestionUsuarios: () -> Unit,
    onInstrumentos: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RecargarAlVolver(viewModel::cargarDatosInicio)

    if (uiState.isLoading) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator()
        }
    } else if (uiState.error != null) {
        EstadoListado("No se pudo cargar el inicio", uiState.error.orEmpty(), "Reintentar", viewModel::cargarDatosInicio)
    } else {
        when (uiState.rolUsuario) {
            "CLIENTE" -> {
                InicioCliente(
                    uiState = uiState,
                    onAbrirEquipo = onAbrirEquipo,
                    onAbrirActa = onAbrirVisita,
                    onAbrirPerfil = onAbrirPerfil,
                    onVerEquipos = onVerEquipos,
                    onVerVisitas = onVerVisitas
                )
            }
            "TECNICO", "INGENIERO", "JEFE_TECNICO" -> {
                InicioPersonalTerreno(
                    uiState = uiState,
                    onAbrirVisita = onAbrirVisita,
                    onNuevoProtocolo = onNuevoProtocolo,
                    onAbrirPerfil = onAbrirPerfil,
                    onVerVisitas = onVerVisitas,
                    onVerEquipos = onVerEquipos,
                    onVerProtocolos = onVerProtocolos,
                    onGestionUsuarios = if (uiState.rolUsuario == Rol.JEFE_TECNICO) onGestionUsuarios else null,
                    onInstrumentos = if (uiState.rolUsuario == Rol.JEFE_TECNICO) onInstrumentos else null
                )
            }
            else -> {
                EstadoListado("Perfil no disponible", "Tu cuenta no tiene un perfil de acceso válido.")
            }
        }
    }
}
