package com.example.appprotocolos_eq014.ui.screens.inicio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.model.uistate.InicioUiState
import com.example.appprotocolos_eq014.ui.components.*
import com.example.appprotocolos_eq014.util.Fechas

@Composable
fun InicioCliente(
    uiState: InicioUiState,
    onAbrirEquipo: (Long) -> Unit,
    onAbrirActa: (Long) -> Unit,
    onAbrirPerfil: () -> Unit,
    onVerEquipos: () -> Unit,
    onVerVisitas: () -> Unit
) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { EncabezadoInicio(uiState, onAbrirPerfil) }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Tu información", style = MaterialTheme.typography.titleLarge)
                TarjetaAcceso("Mis equipos", "${uiState.equiposCliente.size} equipos · fichas y resultados", R.drawable.ic_equipos, onVerEquipos)
                TarjetaAcceso("Actas de visitas", "${uiState.actasCliente.size} actas disponibles", R.drawable.ic_protocolo, onVerVisitas)
                Text("Acceso de consulta a los equipos y actas de tu empresa.", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Equipos", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                TextButton(onClick = onVerEquipos) { Text("Ver todos") }
            }
        }
        items(uiState.equiposCliente.take(3), key = { "equipo_${it.id}" }) { equipo ->
            TarjetaAcceso(equipo.tag, equipo.descripcion, R.drawable.ic_equipos, { onAbrirEquipo(equipo.id) }, Modifier.padding(horizontal = 20.dp))
        }
        if (uiState.equiposCliente.isEmpty()) item {
            Box(Modifier.padding(horizontal = 20.dp)) { EstadoListado("Sin equipos", "Todavía no hay equipos registrados para tu empresa.") }
        }
        if (uiState.actasCliente.isNotEmpty()) {
            item { Text("Últimas actas", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) }
            items(uiState.actasCliente.take(3), key = { "acta_${it.id}" }) { visita ->
                TarjetaAcceso(visita.numeroOt, "${visita.instalacionNombre} · ${Fechas.fecha(visita.fecha)}", R.drawable.ic_protocolo,
                    { onAbrirActa(visita.id) }, Modifier.padding(horizontal = 20.dp))
            }
        }
    }
}
