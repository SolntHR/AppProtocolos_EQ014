package com.example.appprotocolos_eq014.ui.screens.equipos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appprotocolos_eq014.ui.components.*
import com.example.appprotocolos_eq014.util.Fechas
import com.example.appprotocolos_eq014.util.formatoCl
import com.example.appprotocolos_eq014.viewmodel.DetalleEquipoViewModel

@Composable
fun DetalleEquipoScreen(viewModel: DetalleEquipoViewModel, onVolver: () -> Unit, onVerHistorial: (Long) -> Unit) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        BarraSuperior("Ficha del equipo", onVolver)
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val equipo = estado.equipo
            when {
                estado.cargando -> item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                equipo == null || estado.error != null -> item {
                    EstadoListado("Equipo no disponible", estado.error ?: "No se encontró este equipo.", "Reintentar", viewModel::cargar)
                }
                else -> {
                    item {
                        Text(equipo.tag, style = MaterialTheme.typography.headlineMedium)
                        Text(equipo.tipoEquipoNombre, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        BotonSecundario("Ver historial de pruebas", { onVerHistorial(equipo.id) },
                            Modifier.fillMaxWidth().padding(top = 12.dp))
                    }
                    item {
                        TarjetaSeccion("Ubicación") {
                            DatoFicha("Cliente", equipo.clienteNombre)
                            HorizontalDivider()
                            DatoFicha("Instalación", equipo.instalacionNombre)
                        }
                    }
                    item {
                        TarjetaSeccion("Datos de placa") {
                            DatoFicha("Marca", equipo.marca)
                            DatoFicha("Modelo", equipo.modelo)
                            DatoFicha("Número de serie", equipo.numeroSerie)
                            DatoFicha("Año", equipo.anio?.toString())
                            equipo.potenciaKva?.let { DatoFicha("Potencia", "${it.formatoCl()} kVA") }
                            equipo.tensiones?.takeIf { it.isNotBlank() }?.let { DatoFicha("Tensiones", it) }
                            equipo.corriente?.let { DatoFicha("Corriente", "${it.formatoCl()} A") }
                            equipo.maxContactosUohm?.let { DatoFicha("Máximo de contactos", "${it.formatoCl()} µΩ") }
                        }
                    }
                    equipo.observaciones?.takeIf { it.isNotBlank() }?.let { observaciones ->
                        item { TarjetaSeccion("Observaciones") { Text(observaciones, style = MaterialTheme.typography.bodyMedium) } }
                    }
                    item {
                        TarjetaSeccion("Pruebas e historial") {
                            val ultimo = estado.protocolos.firstOrNull()
                            Text("${estado.protocolos.size} protocolos disponibles", style = MaterialTheme.typography.bodyMedium)
                            if (ultimo != null) {
                                DatoFicha("Última prueba", "${Fechas.fecha(ultimo.fechaHoraPrueba)} · ${ultimo.numero}")
                            } else Text("Aún no hay pruebas disponibles para este equipo.", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
