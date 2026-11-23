package com.example.appprotocolos_eq014.ui.screens.equipos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.model.catalogos.Rol
import com.example.appprotocolos_eq014.ui.components.*
import com.example.appprotocolos_eq014.viewmodel.EquiposViewModel

@Composable
fun EquiposScreen(viewModel: EquiposViewModel, onAbrirEquipo: (Long) -> Unit, onNuevoEquipo: () -> Unit) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    RecargarAlVolver(viewModel::cargar)
    Column(Modifier.fillMaxSize()) {
        CabeceraPagina("Equipos", if (estado.rol == Rol.CLIENTE) "Equipos de tu empresa" else "Catálogo por cliente e instalación")
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { CampoTexto(estado.busqueda, viewModel::buscar, "Buscar TAG o cliente", R.drawable.ic_buscar) }
            if (Rol.puedeCrearProtocolos(estado.rol)) item {
                BotonSecundario("Registrar equipo", onNuevoEquipo, Modifier.fillMaxWidth(), R.drawable.ic_mas)
            }
            if (estado.equipos.isNotEmpty()) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FiltroEquipos("Tipo", estado.equipos.distinctBy { it.tipoEquipoId }.map { it.tipoEquipoId to it.tipoEquipoNombre },
                            estado.tipoId, viewModel::filtrarTipo, Modifier.weight(1f))
                        FiltroEquipos("Instalación", estado.equipos.distinctBy { it.instalacionId }.map {
                            it.instalacionId to "${it.instalacionNombre} · ${it.clienteNombre}"
                        }, estado.instalacionId, viewModel::filtrarInstalacion, Modifier.weight(1f))
                    }
                }
            }
            when {
                estado.cargando -> item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                estado.error != null -> item { EstadoListado("No se pudieron cargar los equipos", estado.error.orEmpty(), "Reintentar", viewModel::cargar) }
                estado.filtrados.isEmpty() -> item {
                    EstadoListado("Sin equipos para mostrar", if (estado.equipos.isEmpty()) "Los equipos registrados aparecerán aquí."
                        else "No hay coincidencias con la búsqueda y los filtros.",
                        if (estado.equipos.isEmpty()) null else "Limpiar filtros", viewModel::limpiarFiltros)
                }
                else -> {
                    item { Text("${estado.filtrados.size} equipos · selecciona uno para ver su ficha",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    estado.filtrados.groupBy { it.instalacionId }.forEach { (idInstalacion, equipos) ->
                        item(key = "instalacion_$idInstalacion") {
                            Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(equipos.first().instalacionNombre, style = MaterialTheme.typography.titleMedium)
                                Text(equipos.first().clienteNombre, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        items(equipos, key = { "equipo_${it.id}" }) { equipo ->
                            TarjetaAcceso(equipo.tag, listOfNotNull(equipo.tipoEquipoNombre, equipo.marca, equipo.modelo).joinToString(" · "),
                                R.drawable.ic_equipos, { onAbrirEquipo(equipo.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltroEquipos(
    etiqueta: String, opciones: List<Pair<Long, String>>, seleccion: Long?, onSeleccion: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var abierto by remember { mutableStateOf(false) }
    val todas = if (etiqueta == "Instalación") "Todas" else "Todos"
    val actual = opciones.firstOrNull { it.first == seleccion }?.second ?: todas
    Box(modifier) {
        OutlinedButton(onClick = { abierto = true }, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)) {
            Text("$etiqueta: $actual", style = MaterialTheme.typography.labelMedium, maxLines = 1,
                overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Icon(painterResource(R.drawable.ic_volver), contentDescription = null, modifier = Modifier.size(16.dp).rotate(270f))
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            DropdownMenuItem(text = { Text(todas) }, onClick = { abierto = false; onSeleccion(null) })
            opciones.forEach { opcion ->
                DropdownMenuItem(text = { Text(opcion.second) }, onClick = { abierto = false; onSeleccion(opcion.first) })
            }
        }
    }
}
