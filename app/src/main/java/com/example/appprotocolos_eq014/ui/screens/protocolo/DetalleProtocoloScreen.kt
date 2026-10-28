package com.example.appprotocolos_eq014.ui.screens.protocolo

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appprotocolos_eq014.model.EstadoProtocolo
import com.example.appprotocolos_eq014.model.Fechas
import com.example.appprotocolos_eq014.model.ProtocoloDetalle
import com.example.appprotocolos_eq014.model.PruebaDetalle
import com.example.appprotocolos_eq014.model.formatoCl
import com.example.appprotocolos_eq014.ui.components.BarraSuperior
import com.example.appprotocolos_eq014.ui.components.BotonPrincipal
import com.example.appprotocolos_eq014.ui.components.BotonSecundario
import com.example.appprotocolos_eq014.ui.components.EtiquetaProtocoloListado
import com.example.appprotocolos_eq014.ui.components.EtiquetaResultadoProtocolo
import com.example.appprotocolos_eq014.ui.components.MensajeAvisoProtocolo
import com.example.appprotocolos_eq014.ui.components.MensajeCentradoProtocolo
import com.example.appprotocolos_eq014.ui.components.TarjetaSeccion
import com.example.appprotocolos_eq014.ui.styles.EstilosCampo
import com.example.appprotocolos_eq014.viewmodel.ProtocolosViewModel

// Detalle de un protocolo para el personal Induztek (RF-10, RF-12, RNF-07)
@Composable
fun DetalleProtocoloScreen(
    viewModel: ProtocolosViewModel,
    idProtocolo: String,
    onVolver: () -> Unit,
    onEditar: (String) -> Unit,
    onAbrirProtocolo: (String) -> Unit
) {
    val state by viewModel.detalle.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var accion by remember { mutableStateOf<EstadoProtocolo?>(null) }

    LaunchedEffect(idProtocolo) { viewModel.cargarDetalle(idProtocolo) }
    LaunchedEffect(state.mensaje) {
        state.mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onMensajeMostrado()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        BarraSuperior(titulo = state.detalle?.resumen?.numero ?: "Protocolo", onVolver = onVolver)

        val d = state.detalle
        if (state.cargando && d == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }
        if (d == null) {
            MensajeCentradoProtocolo(state.error ?: "Protocolo no encontrado.")
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            state.error?.let { MensajeAvisoProtocolo(it) }

            Cabecera(d)

            // Acciones segun permisos (RN-04, RN-17)
            if (state.puedeEditar) {
                BotonPrincipal(
                    texto = if (d.resumen.estado == EstadoProtocolo.OBSERVADO) "Corregir protocolo" else "Continuar protocolo",
                    onClick = { onEditar(d.resumen.id) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (state.puedeRevisar) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BotonSecundario("Observar", onClick = { accion = EstadoProtocolo.OBSERVADO }, modifier = Modifier.weight(1f))
                    BotonPrincipal(
                        "Formalizar", onClick = { accion = EstadoProtocolo.FORMALIZADO },
                        habilitado = !state.procesando, modifier = Modifier.weight(1f)
                    )
                }
            }

            if (d.pruebas.isEmpty()) {
                TarjetaSeccion(titulo = "Pruebas") { Text("Este protocolo aún no tiene pruebas registradas.") }
            }
            d.pruebas.groupBy { it.tipo }.forEach { (tipo, pruebas) ->
                TarjetaSeccion(titulo = tipo.nombre) {
                    pruebas.firstNotNullOfOrNull { it.instrumento }?.let {
                        Text("Instrumento: $it", style = MaterialTheme.typography.bodySmall)
                    }
                    pruebas.forEachIndexed { i, prueba ->
                        if (i > 0) HorizontalDivider()
                        BloquePrueba(prueba)
                    }
                }
            }

            if (!d.observaciones.isNullOrBlank() || !d.descripcionMantenimiento.isNullOrBlank()) {
                TarjetaSeccion(titulo = "Observaciones") {
                    d.descripcionMantenimiento?.takeIf { it.isNotBlank() }?.let { Text("Mantenimiento: $it") }
                    d.observaciones?.takeIf { it.isNotBlank() }?.let { Text(it) }
                }
            }

            // RNF-07: trazabilidad
            if (d.historialEstados.isNotEmpty()) {
                TarjetaSeccion(titulo = "Historial de estados") {
                    d.historialEstados.forEach { h ->
                        Column {
                            Text("${h.anterior?.etiqueta ?: "Nuevo"} → ${h.nuevo.etiqueta}", fontWeight = FontWeight.SemiBold)
                            Text(
                                "${h.usuario} · ${Fechas.fechaHora(h.fechaHora)}" + (h.motivo?.let { " · $it" } ?: ""),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // RF-10: historial del equipo, del mas reciente al mas antiguo
            TarjetaSeccion(titulo = "Historial del equipo ${d.resumen.tagEquipo}") {
                d.historialEquipo.forEach { p ->
                    TarjetaProtocoloListado(
                        protocolo = p,
                        onClick = { if (p.id != d.resumen.id) onAbrirProtocolo(p.id) }
                    )
                }
            }

            if (state.puedeAnular) {
                TextButton(onClick = { accion = EstadoProtocolo.ANULADO }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Anular protocolo", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    accion?.let { nuevo ->
        DialogoMotivo(
            nuevo = nuevo,
            motivoObligatorio = nuevo != EstadoProtocolo.FORMALIZADO || state.detalle?.tieneVariacionSignificativa == true,
            onConfirmar = { motivo ->
                accion = null
                viewModel.cambiarEstado(nuevo, motivo)
            },
            onCancelar = { accion = null }
        )
    }
}

@Composable
private fun Cabecera(d: ProtocoloDetalle) {
    val r = d.resumen
    TarjetaSeccion(titulo = "${r.tagEquipo} · ${r.tipoEquipo}") {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EtiquetaProtocoloListado(r)
            EtiquetaResultadoProtocolo(r.resultadoGlobal)
        }
        Text("${r.cliente} · ${r.instalacion}")
        Text("${d.numeroOt} · ${Fechas.fechaHora(r.fechaHora)}", style = MaterialTheme.typography.bodySmall)
        Text("Técnico: ${r.tecnico}", style = MaterialTheme.typography.bodySmall)
        Text(
            "T° ambiente: ${d.temperatura?.let { "${it.formatoCl(1)} °C" } ?: "-"} · Humedad: ${d.humedad?.let { "${it.formatoCl(0)} %" } ?: "-"}",
            style = MaterialTheme.typography.bodySmall
        )
        r.reemplazaANumero?.let { Text("Reemplaza al protocolo nº $it", fontWeight = FontWeight.SemiBold) }
        if (r.atrasado) MensajeAvisoProtocolo("Atrasado: supera los 2 días hábiles sin formalizar (RN-16).")
        if (d.tieneVariacionSignificativa) {
            MensajeAvisoProtocolo("Tiene variaciones significativas respecto a la prueba anterior (RN-09).", esError = false)
        }
    }
}

@Composable
private fun BloquePrueba(prueba: PruebaDetalle) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(prueba.elemento, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (prueba.realizada) EtiquetaResultadoProtocolo(prueba.resultado)
        }
        if (!prueba.realizada) {
            Text(
                "No realizada" + (prueba.motivoNoRealizada?.let { ": $it" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        prueba.mediciones.forEach { m ->
            val valorTexto = if (m.unidad == "-") m.valor.formatoCl(2) else "${m.valor.formatoCl(2)} ${m.unidad}"
            Row {
                Text(m.nombre, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                Text(valorTexto, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
        }
        prueba.calculo?.let { Text(it, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold) }
        if (prueba.ingresadoManualmente) {
            Text("Resultado indicado manualmente (RN-08c)", style = MaterialTheme.typography.labelSmall)
        }
        prueba.comentario?.takeIf { it.isNotBlank() }?.let {
            Text("Comentario: $it", style = MaterialTheme.typography.bodySmall)
        }
        prueba.comparaciones.forEach { LineaComparacion(it) }
    }
}

@Composable
private fun DialogoMotivo(
    nuevo: EstadoProtocolo,
    motivoObligatorio: Boolean,
    onConfirmar: (String?) -> Unit,
    onCancelar: () -> Unit
) {
    var motivo by remember { mutableStateOf("") }
    val titulo = when (nuevo) {
        EstadoProtocolo.FORMALIZADO -> "Formalizar protocolo"
        EstadoProtocolo.OBSERVADO -> "Devolver con observaciones"
        else -> "Anular protocolo"
    }
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    when (nuevo) {
                        EstadoProtocolo.FORMALIZADO -> "Quedará como documento oficial con tu nombre y la fecha. Ya no se podrá modificar."
                        EstadoProtocolo.OBSERVADO -> "El técnico podrá corregirlo. Indica el motivo."
                        else -> "Un protocolo anulado no se muestra al cliente. Indica el motivo."
                    }
                )
                OutlinedTextField(
                    value = motivo,
                    onValueChange = { motivo = it },
                    label = { Text(if (motivoObligatorio) "Motivo / comentario *" else "Comentario (opcional)") },
                    minLines = 2,
                    shape = EstilosCampo.forma,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirmar(motivo.ifBlank { null }) },
                enabled = !motivoObligatorio || motivo.isNotBlank()
            ) { Text("Confirmar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}
