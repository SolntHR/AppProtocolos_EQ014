package com.example.appprotocolos_eq014.ui.screens.inicio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appprotocolos_eq014.model.Equipo
import com.example.appprotocolos_eq014.model.InicioUiState
import com.example.appprotocolos_eq014.ui.components.CampoTexto
import com.example.appprotocolos_eq014.ui.components.TarjetaSeccion

import androidx.compose.ui.tooling.preview.Preview
import com.example.appprotocolos_eq014.ui.theme.AppProtocolos_EQ014Theme

@Composable
fun InicioCliente(
    uiState: InicioUiState,
    onBusquedaCambia: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        // Encabezado
        Text(text = "PORTAL CLIENTE", style = MaterialTheme.typography.labelSmall)
        Text(
            text = "Mis equipos",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Consulta de protocolos y resultados",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Buscador reutilizando tu componente
        CampoTexto(
            valor = uiState.busqueda,
            onValorCambia = onBusquedaCambia,
            etiqueta = "Buscar por TAG (ej.: TR-01)",
            icono = android.R.drawable.ic_menu_search // O tu propio R.drawable.ic_buscar
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lista dinámica de equipos
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(uiState.equiposCliente) { equipo ->
                ItemEquipoCliente(equipo = equipo)
            }
        }
    }
}

@Composable
fun ItemEquipoCliente(equipo: Equipo) {
    TarjetaSeccion(titulo = equipo.tag) {
        Text(
            text = "${equipo.tipoEquipoNombre} ${equipo.marca ?: ""} · Serie: ${equipo.numeroSerie ?: "N/A"}",
            style = MaterialTheme.typography.bodyMedium
        )
        // Aquí iteraremos sobre los protocolos de este equipo más adelante
    }
}


@Preview(showBackground = true)
@Composable
private fun InicioClientePreview() {
    AppProtocolos_EQ014Theme {
        InicioCliente(
            uiState = InicioUiState(
                rolUsuario = "CLIENTE",
                equiposCliente = listOf(
                    Equipo(id = 1, tag = "TR-01", tipoEquipoNombre = "Transformador", marca = "Rhona", numeroSerie = "RH-18733"),
                    Equipo(id = 2, tag = "TR-02", tipoEquipoNombre = "Transformador", marca = "Rhona", numeroSerie = "RH-21904")
                )
            ),
            onBusquedaCambia = {}
        )
    }
}