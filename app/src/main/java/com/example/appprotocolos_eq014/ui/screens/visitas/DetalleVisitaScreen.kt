package com.example.appprotocolos_eq014.ui.screens.visitas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.model.catalogos.EstadoProtocolo
import com.example.appprotocolos_eq014.model.catalogos.Rol
import com.example.appprotocolos_eq014.model.entidades.Equipo
import com.example.appprotocolos_eq014.model.entidades.Protocolo
import com.example.appprotocolos_eq014.ui.components.*
import com.example.appprotocolos_eq014.util.Fechas
import com.example.appprotocolos_eq014.viewmodel.DetalleVisitaViewModel

@Composable
fun DetalleVisitaScreen(
    viewModel: DetalleVisitaViewModel,
    onVolver: () -> Unit,
    onAbrirEquipo: (Long) -> Unit,
    onAbrirProtocolo: (String) -> Unit,
    onRegistrar: (Long, Long?) -> Unit,
    onEditarProtocolo: (String) -> Unit,
    onAgregarEquipo: (Long) -> Unit,
    onVerActa: (Long) -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var responsablesVisibles by rememberSaveable { mutableStateOf(false) }
    RecargarAlVolver(viewModel::cargar)
    Column(Modifier.fillMaxSize()) {
        BarraSuperior("Detalle de visita", onVolver)
        val visita = estado.visita
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            when {
                estado.cargando -> item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                estado.error != null || visita == null -> item {
                    EstadoListado("Visita no disponible", estado.error ?: "No se encontró esta visita.", "Reintentar", viewModel::cargar)
                }
                else -> {
                    item {
                        TarjetaSeccion(visita.numeroOt) {
                            Text(visita.clienteNombre, style = MaterialTheme.typography.titleMedium)
                            Text("${visita.instalacionNombre} · ${Fechas.fecha(visita.fecha)}", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            EtiquetaEstado(if (visita.cerrada) "Cerrada" else "Abierta", if (visita.cerrada) TonoEstado.EXITO else TonoEstado.AZUL)
                            Text(visita.tipoVisitaTexto, style = MaterialTheme.typography.bodySmall)
                            TextButton(onClick = { responsablesVisibles = !responsablesVisibles }) {
                                Text(if (responsablesVisibles) "Ocultar responsables" else "Ver responsables")
                            }
                            AnimatedVisibility(responsablesVisibles) {
                                Column {
                                    DatoFicha("Ingeniero a cargo", visita.ingenieroNombre)
                                    DatoFicha("Técnicos", visita.tecnicos)
                                }
                            }
                        }
                    }
                    val porEquipo = visita.protocolos.associateBy { it.equipoId }
                    val enVisita = estado.equipos.filter { it.id in porEquipo }
                    val sinProtocolo = estado.equipos.filter { it.id !in porEquipo }
                    if (enVisita.isNotEmpty()) item {
                        Text("Equipos de esta visita (${enVisita.size})", style = MaterialTheme.typography.titleLarge)
                        Text("Abre la ficha del equipo o continúa con su protocolo.", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (enVisita.isEmpty() && !estado.puedeRegistrar) item {
                        EstadoListado("Sin protocolos disponibles", "No hay protocolos visibles para esta visita.")
                    }
                    items(enVisita, key = { "visita_${it.id}" }) { equipo ->
                        val protocolo = porEquipo.getValue(equipo.id)
                        val puedeEditar = estado.puedeRegistrar && protocolo.usuarioRegistraId == estado.usuarioId &&
                            protocolo.estado in setOf(EstadoProtocolo.BORRADOR, EstadoProtocolo.OBSERVADO)
                        EquipoEnVisita(equipo, protocolo, estado.rol == Rol.CLIENTE,
                            onFicha = { onAbrirEquipo(equipo.id) },
                            accion = if (estado.rol == Rol.CLIENTE) null else if (puedeEditar) "Continuar mediciones" else "Ver protocolo",
                            onAccion = { if (puedeEditar) onEditarProtocolo(protocolo.id) else onAbrirProtocolo(protocolo.id) })
                    }
                    if (estado.puedeRegistrar && sinProtocolo.isNotEmpty()) {
                        item {
                            Text(if (enVisita.isEmpty()) "Equipos de la instalación (${sinProtocolo.size})" else "Otros equipos de la instalación",
                                style = MaterialTheme.typography.titleMedium)
                            Text("Selecciona un equipo para registrar sus mediciones.", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        items(sinProtocolo, key = { "otro_${it.id}" }) { equipo ->
                            EquipoEnVisita(equipo, null, false, { onAbrirEquipo(equipo.id) }, "Registrar mediciones",
                                { onRegistrar(visita.id, equipo.id) })
                        }
                    }
                    if (estado.puedeRegistrar) item {
                        BotonSecundario("Agregar equipo a la instalación", { onAgregarEquipo(visita.instalacionId) },
                            Modifier.fillMaxWidth(), R.drawable.ic_mas)
                    }
                    item {
                        if (estado.puedeRegistrar && !estado.puedeCerrar && estado.rol in setOf(Rol.TECNICO, Rol.INGENIERO)) {
                            EstadoListado("Antes de cerrar", if (visita.protocolos.isEmpty()) "Registra al menos un protocolo."
                                else "Completa o corrige los protocolos pendientes. La recepción se registra en el acta.")
                        }
                        if (estado.rol in setOf(Rol.TECNICO, Rol.INGENIERO) && !visita.cerrada) {
                            BotonPrincipal("Cerrar visita y ver acta", { onVerActa(visita.id) },
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp), habilitado = estado.puedeCerrar)
                        } else {
                            BotonPrincipal("Ver acta de la visita", { onVerActa(visita.id) }, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EquipoEnVisita(
    equipo: Equipo, protocolo: Protocolo?, esCliente: Boolean,
    onFicha: () -> Unit, accion: String?, onAccion: () -> Unit
) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_equipos), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f)) {
                    Text(equipo.tag, style = MaterialTheme.typography.titleMedium)
                    Text(listOfNotNull(equipo.tipoEquipoNombre, equipo.marca).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = onFicha) { Text("Ficha") }
            }
            if (protocolo != null) {
                EtiquetaEstado(if (esCliente) protocolo.estado.etiquetaCliente else protocolo.estado.etiqueta,
                    when (protocolo.estado) {
                        EstadoProtocolo.FORMALIZADO -> TonoEstado.EXITO
                        EstadoProtocolo.OBSERVADO -> TonoEstado.AVISO
                        EstadoProtocolo.REGISTRADO_TERRENO -> TonoEstado.AZUL
                        else -> TonoEstado.NEUTRO
                    })
            }
            if (accion != null) BotonSecundario(accion, onAccion, Modifier.fillMaxWidth())
        }
    }
}
