package com.example.appprotocolos_eq014.ui.screens.visitas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.model.entidades.Visita
import com.example.appprotocolos_eq014.ui.components.BarraSuperior
import com.example.appprotocolos_eq014.ui.components.BotonPrincipal
import com.example.appprotocolos_eq014.ui.components.BotonSecundario
import com.example.appprotocolos_eq014.ui.components.CampoTexto
import com.example.appprotocolos_eq014.ui.components.MensajeAvisoProtocolo
import com.example.appprotocolos_eq014.ui.components.SelectorDesplegable
import com.example.appprotocolos_eq014.ui.components.SelectorSegmentado
import com.example.appprotocolos_eq014.ui.components.TarjetaSeccion
import com.example.appprotocolos_eq014.util.ValidacionesVisita as V
import com.example.appprotocolos_eq014.viewmodel.FormularioVisitaViewModel

@Composable
fun FormularioVisitaScreen(
    viewModel: FormularioVisitaViewModel,
    onVolver: () -> Unit,
    onGuardada: (idVisita: Long) -> Unit
) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(s.visitaCreadaId) {
        val id = s.visitaCreadaId ?: return@LaunchedEffect
        Toast.makeText(context, if (s.editando) "Visita actualizada" else "Visita asignada", Toast.LENGTH_SHORT).show()
        onGuardada(id)
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        BarraSuperior(titulo = if (s.editando) "Editar visita" else "Asignar visita", onVolver = onVolver)
        if (s.cargando) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            s.error?.let { MensajeAvisoProtocolo(it) }
            TarjetaSeccion(titulo = "Datos de la visita") {
                CampoTexto(
                    valor = s.numeroOt, onValorCambia = viewModel::onNumeroOt,
                    etiqueta = "Número de OT * (ej.: OT-2026-1300)", icono = R.drawable.ic_protocolo,
                    error = s.errores[V.CAMPO_OT]
                )
                SelectorDesplegable(
                    etiqueta = "Instalación *",
                    opciones = s.instalaciones,
                    seleccion = s.instalacion,
                    textoDe = { it.nombre },
                    onSeleccion = viewModel::onInstalacion,
                    habilitado = !s.instalacionBloqueada,
                    error = s.errores[V.CAMPO_INSTALACION],
                    textoVacio = "Selecciona la instalación"
                )
                if (s.instalacionBloqueada) {
                    Text(
                        "La instalación no se puede cambiar porque la visita ya tiene protocolos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                CampoTexto(
                    valor = s.fecha, onValorCambia = viewModel::onFecha,
                    etiqueta = "Fecha * (dd-mm-aaaa)", icono = R.drawable.ic_inicio,
                    error = s.errores[V.CAMPO_FECHA], tipoTeclado = KeyboardType.Number
                )
                SelectorSegmentado(
                    etiqueta = "Tipo de visita",
                    opciones = listOf(Visita.SOLO_PRUEBAS, Visita.PRUEBAS_Y_MANTENIMIENTO),
                    seleccionada = s.tipoVisita,
                    textoDe = { if (it == Visita.SOLO_PRUEBAS) "Solo pruebas" else "Pruebas y mant." },
                    onSeleccionar = viewModel::onTipoVisita
                )
            }
            TarjetaSeccion(titulo = "Equipo de trabajo") {
                SelectorDesplegable(
                    etiqueta = "Ingeniero a cargo *",
                    opciones = s.ingenieros,
                    seleccion = s.ingeniero,
                    textoDe = { it.nombre },
                    onSeleccion = viewModel::onIngeniero,
                    error = s.errores[V.CAMPO_INGENIERO],
                    textoVacio = "Selecciona el ingeniero"
                )
                Text(
                    if (s.participantes.isEmpty()) "Técnicos en terreno *"
                    else "Técnicos en terreno * (${s.participantes.size} elegidos)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CampoTexto(
                    valor = s.busquedaTecnicos,
                    onValorCambia = viewModel::onBusquedaTecnicos,
                    etiqueta = "Buscar técnico por nombre",
                    icono = R.drawable.ic_buscar
                )
                s.personalVisible.forEach { persona ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.onParticipante(persona.id) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = persona.id in s.participantes,
                            onCheckedChange = { viewModel.onParticipante(persona.id) }
                        )
                        Text(persona.nombre, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (s.personalVisible.isEmpty()) {
                    Text(
                        "Ningún técnico coincide con la búsqueda.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                s.errores[V.CAMPO_PARTICIPANTES]?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
            Text("* Campos obligatorios", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BotonSecundario(texto = "Cancelar", onClick = onVolver, modifier = Modifier.weight(1f))
            BotonPrincipal(
                texto = if (s.guardando) "Guardando..." else if (s.editando) "Guardar cambios" else "Asignar visita",
                onClick = viewModel::guardar,
                habilitado = !s.guardando,
                modifier = Modifier.weight(1f)
            )
        }
    }
}