package com.example.appprotocolos_eq014.ui.screens.protocolo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.model.EstadoProtocolo
import com.example.appprotocolos_eq014.model.Fechas
import com.example.appprotocolos_eq014.model.ProtocoloListado
import com.example.appprotocolos_eq014.model.Rol
import com.example.appprotocolos_eq014.model.formatoCl
import com.example.appprotocolos_eq014.ui.components.BotonSecundario
import com.example.appprotocolos_eq014.ui.components.CampoTexto
import com.example.appprotocolos_eq014.ui.components.EtiquetaEstado
import com.example.appprotocolos_eq014.ui.components.EtiquetaProtocoloListado
import com.example.appprotocolos_eq014.ui.components.EtiquetaResultadoProtocolo
import com.example.appprotocolos_eq014.ui.components.MensajeAvisoProtocolo
import com.example.appprotocolos_eq014.ui.components.MensajeCentradoProtocolo
import com.example.appprotocolos_eq014.ui.components.TituloSeccion
import com.example.appprotocolos_eq014.ui.components.TonoEstado
import com.example.appprotocolos_eq014.viewmodel.ProtocolosViewModel

// Pestaña "Protocolos" del personal Induztek (RF-13, RN-16)
@Composable
fun ListaProtocolosScreen(
    viewModel: ProtocolosViewModel,
    onAbrirProtocolo: (String) -> Unit,
    onNuevoProtocolo: () -> Unit,
    onNuevoEquipo: () -> Unit = {},
    onNuevaInstalacion: () -> Unit = {}
) {
    val state by viewModel.lista.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.cargarLista() }
    val lista = state.filtrados

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TituloSeccion("Protocolos")
                    // RF-21: indicadores simples
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Indicador("Pendientes de formalizar", state.pendientesFormalizar.toString(), Modifier.weight(1f))
                        Indicador("Atrasados", state.atrasados.toString(), Modifier.weight(1f))
                        Indicador(
                            "Días prom. a formalizar",
                            state.promedioDiasFormalizacion?.let { it.formatoCl(1) } ?: "—",
                            Modifier.weight(1f)
                        )
                    }
                    // RF-01 / RF-02: catalogo de instalaciones y equipos
                    if (Rol.puedeCrearProtocolos(state.rolUsuario)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BotonSecundario(
                                texto = "Equipo", onClick = onNuevoEquipo,
                                icono = R.drawable.ic_mas, modifier = Modifier.weight(1f)
                            )
                            BotonSecundario(
                                texto = "Instalación", onClick = onNuevaInstalacion,
                                icono = R.drawable.ic_mas, modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    CampoTexto(
                        valor = state.busqueda,
                        onValorCambia = viewModel::onBusqueda,
                        etiqueta = "Buscar por nº, TAG, cliente o técnico",
                        icono = R.drawable.ic_buscar
                    )
                    // Filtro por estado (RF-13)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = state.filtroEstado == null,
                            onClick = { viewModel.onFiltroEstado(null) },
                            label = { Text("Todos") }
                        )
                        EstadoProtocolo.entries.forEach { estado ->
                            FilterChip(
                                selected = state.filtroEstado == estado,
                                onClick = { viewModel.onFiltroEstado(if (state.filtroEstado == estado) null else estado) },
                                label = { Text(estado.etiqueta) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (state.rolUsuario == Rol.TECNICO || state.rolUsuario == Rol.INGENIERO) {
                            FilterChip(
                                selected = state.soloMios,
                                onClick = { viewModel.onSoloMios(!state.soloMios) },
                                label = { Text("Solo míos") }
                            )
                        }
                        FilterChip(
                            selected = state.soloAtrasados,
                            onClick = { viewModel.onSoloAtrasados(!state.soloAtrasados) },
                            label = { Text("Atrasados (> 2 días hábiles)") }
                        )
                    }
                    Text(
                        "${lista.size} protocolos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    state.error?.let { MensajeAvisoProtocolo(it) }
                }
            }

            if (state.cargando) {
                item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (lista.isEmpty()) {
                item { MensajeCentradoProtocolo("No hay protocolos con estos filtros.") }
            }

            items(lista, key = { it.id }) { p ->
                TarjetaProtocoloListado(protocolo = p, onClick = { onAbrirProtocolo(p.id) })
            }
        }

        if (Rol.puedeCrearProtocolos(state.rolUsuario)) {
            ExtendedFloatingActionButton(
                onClick = onNuevoProtocolo,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                icon = { Icon(painterResource(R.drawable.ic_mas), contentDescription = null) },
                text = { Text("Nuevo protocolo", fontWeight = FontWeight.Bold) },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            )
        }
    }
}

@Composable
private fun Indicador(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(valor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Text(titulo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun TarjetaProtocoloListado(protocolo: ProtocoloListado, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    protocolo.numero,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                EtiquetaProtocoloListado(protocolo)
            }
            Text("${protocolo.tagEquipo} · ${protocolo.tipoEquipo}", fontWeight = FontWeight.SemiBold)
            Text(
                "${protocolo.cliente} · ${protocolo.instalacion}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${Fechas.fechaHora(protocolo.fechaHora)} · ${protocolo.tecnico}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                if (protocolo.atrasado) {
                    EtiquetaEstado("Atrasado", TonoEstado.ERROR, Modifier.padding(end = 6.dp))
                }
                EtiquetaResultadoProtocolo(protocolo.resultadoGlobal)
            }
        }
    }
}
