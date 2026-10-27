package com.example.appprotocolos_eq014.ui.screens.inicio

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appprotocolos_eq014.model.InicioUiState
import com.example.appprotocolos_eq014.ui.components.BotonPrincipal

@Composable
fun InicioJefeTecnico(
    uiState: InicioUiState,
    onAgregarUsuarioClick: () -> Unit
) {
    var tabSeleccionado by remember { mutableStateOf(0) }
    val titulosTabs = listOf("Personal Induztek", "Usuarios cliente")

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Text(
                text = "Gestión de usuarios",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            TabRow(
                selectedTabIndex = tabSeleccionado,
                containerColor = MaterialTheme.colorScheme.surface,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[tabSeleccionado]),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            ) {
                titulosTabs.forEachIndexed { index, titulo ->
                    Tab(
                        selected = tabSeleccionado == index,
                        onClick = { tabSeleccionado = index },
                        text = { Text(titulo) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (tabSeleccionado == 0) {
                Text("Cargando lista de personal...", style = MaterialTheme.typography.bodyMedium)
            } else {
                Text("Cargando usuarios cliente...", style = MaterialTheme.typography.bodyMedium)
            }
        }

        BotonPrincipal(
            texto = "+ Agregar usuario",
            onClick = onAgregarUsuarioClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        )
    }
}