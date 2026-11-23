package com.example.appprotocolos_eq014.ui.screens.visitas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.model.catalogos.Rol
import com.example.appprotocolos_eq014.model.entidades.Visita
import com.example.appprotocolos_eq014.model.uistate.FiltroVisita
import com.example.appprotocolos_eq014.model.uistate.esProgramada
import com.example.appprotocolos_eq014.ui.components.*
import com.example.appprotocolos_eq014.util.Fechas
import com.example.appprotocolos_eq014.viewmodel.VisitasViewModel

@Composable
fun VisitasScreen(viewModel: VisitasViewModel, onAbrirVisita: (Long) -> Unit, onVerProtocolos: () -> Unit) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    RecargarAlVolver(viewModel::cargar)
    Column(Modifier.fillMaxSize()) {
        CabeceraPagina(
            if (estado.rol == Rol.CLIENTE) "Actas de visitas" else "Visitas",
            if (estado.rol == Rol.CLIENTE) "Consulta los trabajos entregados a tu empresa" else "Órdenes de trabajo y avance de tus visitas"
        ) {
            if (Rol.puedeCrearProtocolos(estado.rol)) TextButton(onClick = onVerProtocolos) { Text("Protocolos") }
        }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                CampoTexto(estado.busqueda, viewModel::buscar, "Buscar visitas", R.drawable.ic_buscar)
                Text("OT, cliente, instalación o fecha", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            }
            if (estado.rol != Rol.CLIENTE) item {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FiltroVisita.entries.forEach { filtro ->
                        FilterChip(selected = estado.filtro == filtro, onClick = { viewModel.filtrar(filtro) }, label = { Text(filtro.etiqueta) })
                    }
                }
            }
            when {
                estado.cargando -> item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                estado.error != null -> item { EstadoListado("No se pudieron cargar las visitas", estado.error.orEmpty(), "Reintentar", viewModel::cargar) }
                estado.filtradas.isEmpty() -> item {
                    EstadoListado("Sin visitas para mostrar", if (estado.busqueda.isNotBlank() || estado.filtro != FiltroVisita.TODAS)
                        "Prueba con otra búsqueda o cambia el estado." else "Las visitas disponibles aparecerán aquí.")
                }
                else -> {
                    item { Text("${estado.filtradas.size} visitas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    items(estado.filtradas, key = { it.id }) { visita ->
                        TarjetaVisita(visita, { onAbrirVisita(visita.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaVisita(visita: Visita, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val programada = visita.esProgramada(Fechas.hoy())
    val estado = when { visita.cerrada -> "Cerrada"; programada -> "Programada"; else -> "En curso" }
    val tono = when { visita.cerrada -> TonoEstado.EXITO; programada -> TonoEstado.NEUTRO; else -> TonoEstado.AZUL }
    val progreso = if (visita.totalProtocolos == 0) 0f else
        (visita.protocolosRegistrados.toFloat() / visita.totalProtocolos).coerceIn(0f, 1f)
    Surface(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(Fechas.fecha(visita.fecha), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                EtiquetaEstado(estado, tono)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(visita.clienteNombre, style = MaterialTheme.typography.titleMedium)
                    Text(visita.instalacionNombre, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(visita.numeroOt, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                Icon(painterResource(R.drawable.ic_volver), contentDescription = null, modifier = Modifier.size(18.dp).rotate(180f),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (visita.cerrada) {
                Text("Acta disponible", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else if (visita.totalProtocolos == 0) {
                Text("Sin protocolos registrados", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text("${visita.protocolosRegistrados} de ${visita.totalProtocolos} protocolos registrados", style = MaterialTheme.typography.bodySmall)
                LinearProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
            }
        }
    }
}
