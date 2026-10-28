package com.example.appprotocolos_eq014.ui.screens.acta

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.model.ActaUiState
import com.example.appprotocolos_eq014.model.ActaVisita
import com.example.appprotocolos_eq014.model.EstadoProtocolo
import com.example.appprotocolos_eq014.model.MedicionActa
import com.example.appprotocolos_eq014.model.ProtocoloActa
import com.example.appprotocolos_eq014.model.PruebaActa
import com.example.appprotocolos_eq014.model.RecepcionActa
import com.example.appprotocolos_eq014.model.ResultadoPrueba
import com.example.appprotocolos_eq014.ui.components.BarraSuperior
import com.example.appprotocolos_eq014.ui.components.BotonPrincipal
import com.example.appprotocolos_eq014.ui.components.CampoBloqueado
import com.example.appprotocolos_eq014.ui.components.CampoTexto
import com.example.appprotocolos_eq014.ui.components.EtiquetaEstado
import com.example.appprotocolos_eq014.ui.components.EtiquetaResultado
import com.example.appprotocolos_eq014.ui.components.TituloSeccion
import com.example.appprotocolos_eq014.ui.components.TonoEstado
import com.example.appprotocolos_eq014.ui.theme.AppProtocolos_EQ014Theme
import com.example.appprotocolos_eq014.ui.theme.Blanco
import com.example.appprotocolos_eq014.ui.theme.NaranjaAcento
import com.example.appprotocolos_eq014.util.Formato
import com.example.appprotocolos_eq014.viewmodel.ActaViewModel
import com.example.appprotocolos_eq014.permisos.SolicitarPermisoNotificaciones

@Composable
fun ActaScreen(
    viewModel: ActaViewModel,
    onVolver: () -> Unit,
    onAbrirEquipo: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SolicitarPermisoNotificaciones()
    ActaContent(
        uiState = uiState,
        onVolver = onVolver,
        onAbrirEquipo = onAbrirEquipo,
        onNombreCambia = viewModel::onNombreCambia,
        onRutCambia = viewModel::onRutCambia,
        onCargoCambia = viewModel::onCargoCambia,
        onRegistrar = viewModel::onRegistrarRecepcion
    )
}

@Composable
fun ActaContent(
    uiState: ActaUiState,
    onVolver: () -> Unit,
    onAbrirEquipo: (Long) -> Unit,
    onNombreCambia: (String) -> Unit,
    onRutCambia: (String) -> Unit,
    onCargoCambia: (String) -> Unit,
    onRegistrar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BarraSuperior(titulo = stringResource(R.string.titulo_acta), onVolver = onVolver)

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        val acta = uiState.acta
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (acta == null) {
                Text(text = uiState.errorGeneral.orEmpty(), color = MaterialTheme.colorScheme.error)
                return@Column
            }

            DocumentoActa(acta = acta, onAbrirEquipo = onAbrirEquipo)

            TituloSeccion(stringResource(R.string.acta_recibe_conforme))

            val recepcion = acta.recepcion
            if (recepcion != null) {
                DatosRecepcion(recepcion)
            } else if (uiState.puedeRegistrarRecepcion) {
                FormularioRecepcion(
                    uiState = uiState,
                    onNombreCambia = onNombreCambia,
                    onRutCambia = onRutCambia,
                    onCargoCambia = onCargoCambia
                )
            } else {
                Text(
                    text = stringResource(R.string.acta_sin_recepcion),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }

            TituloSeccion(stringResource(R.string.acta_firmas))
            Firma(
                etiqueta = stringResource(R.string.acta_firma_tecnico),
                nombre = acta.tecnicos,
                fecha = recepcion?.fechaHora?.let { Formato.fechaHora(it) }
            )
            Firma(
                etiqueta = stringResource(R.string.acta_firma_cliente),
                nombre = recepcion?.nombre ?: uiState.receptorNombre.trim().ifEmpty { null },
                fecha = recepcion?.fechaHora?.let { Formato.fechaHora(it) }
            )

            AnimatedVisibility(
                visible = uiState.errorGeneral != null,
                enter = fadeIn() + expandVertically()
            ) {
                Aviso(texto = uiState.errorGeneral.orEmpty(), tono = TonoEstado.ERROR)
            }

            AnimatedVisibility(
                visible = uiState.recienRegistrada,
                enter = fadeIn() + scaleIn(initialScale = 0.9f)
            ) {
                Aviso(
                    texto = stringResource(R.string.acta_registrada, recepcion?.nombre.orEmpty()),
                    tono = TonoEstado.EXITO
                )
            }

            if (uiState.puedeRegistrarRecepcion) {
                BotonPrincipal(
                    texto = stringResource(R.string.btn_registrar_recepcion),
                    onClick = onRegistrar,
                    habilitado = !uiState.guardando,
                    icono = R.drawable.ic_check,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun DocumentoActa(acta: ActaVisita, onAbrirEquipo: (Long) -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(NaranjaAcento),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_rayo),
                        contentDescription = null,
                        tint = Blanco,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.marca_induztek),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f)
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.acta_titulo_documento),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.End
                    )
                    Text(text = acta.numeroOt, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            SelloPendiente()

            val datos = listOf(
                stringResource(R.string.acta_cliente) to acta.cliente,
                stringResource(R.string.acta_instalacion) to acta.instalacion,
                stringResource(R.string.acta_fecha) to Formato.fecha(acta.fecha),
                stringResource(R.string.acta_tipo_visita) to acta.tipoVisita,
                stringResource(R.string.acta_tecnico) to acta.tecnicos,
                stringResource(R.string.acta_ingeniero) to acta.ingenieroCargo
            )
            datos.chunked(2).forEach { par ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    par.forEach { (etiqueta, valor) ->
                        Column(modifier = Modifier.weight(1f)) {
                            Text(etiqueta, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(valor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Text(
                text = stringResource(R.string.acta_equipos_resultados),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
            acta.protocolos.forEach { protocolo ->
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                ProtocoloDocumento(protocolo = protocolo, onClick = { onAbrirEquipo(protocolo.idEquipo) })
            }
        }
    }
}

@Composable
private fun SelloPendiente() {
    Text(
        text = stringResource(R.string.estado_pendiente).uppercase(),
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .border(1.5.dp, NaranjaAcento, RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp)
    )
}

@Composable
private fun ProtocoloDocumento(protocolo: ProtocoloActa, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${protocolo.numero} · ${protocolo.tag}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (protocolo.estado == EstadoProtocolo.BORRADOR) {
                EtiquetaEstado(texto = protocolo.estado.etiqueta, tono = TonoEstado.NEUTRO)
            } else {
                EtiquetaResultado(protocolo.resultadoGlobal)
            }
        }
        Text(
            text = protocolo.descripcionEquipo,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (protocolo.pruebas.isEmpty()) {
            Text(
                text = stringResource(R.string.acta_protocolo_sin_pruebas),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        protocolo.pruebas.forEach { prueba -> PruebaDocumento(prueba) }
        Text(
            text = stringResource(R.string.acta_ver_historial),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun PruebaDocumento(prueba: PruebaActa) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${prueba.nombre} · ${prueba.elemento}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            if (prueba.realizada) {
                EtiquetaResultado(prueba.resultado)
            } else {
                EtiquetaEstado(texto = stringResource(R.string.acta_no_realizada), tono = TonoEstado.NEUTRO)
            }
        }
        val detalle = if (prueba.realizada) {
            prueba.mediciones.joinToString(" · ") { describir(it) }
        } else {
            stringResource(R.string.acta_motivo, prueba.motivoNoRealizada.orEmpty())
        }
        if (detalle.isNotEmpty()) {
            Text(text = detalle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun describir(medicion: MedicionActa): String {
    val unidad = if (medicion.unidad == "-") "" else " ${medicion.unidad}"
    return "${medicion.parametro}: ${Formato.numeroAutomatico(medicion.valor)}$unidad"
}

@Composable
private fun FormularioRecepcion(
    uiState: ActaUiState,
    onNombreCambia: (String) -> Unit,
    onRutCambia: (String) -> Unit,
    onCargoCambia: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.acta_persona_responsable),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        CampoTexto(
            valor = uiState.receptorNombre,
            onValorCambia = onNombreCambia,
            etiqueta = stringResource(R.string.campo_nombre_completo),
            icono = R.drawable.ic_persona,
            error = uiState.errorNombre
        )
        CampoTexto(
            valor = uiState.receptorRut,
            onValorCambia = onRutCambia,
            etiqueta = stringResource(R.string.campo_rut),
            icono = R.drawable.ic_persona,
            error = uiState.errorRut
        )
        CampoTexto(
            valor = uiState.receptorCargo,
            onValorCambia = onCargoCambia,
            etiqueta = stringResource(R.string.campo_cargo),
            icono = R.drawable.ic_persona,
            error = uiState.errorCargo
        )
        CampoBloqueado(
            etiqueta = stringResource(R.string.acta_fecha_automatica),
            valor = uiState.fechaHora,
            ayuda = stringResource(R.string.acta_fecha_ayuda)
        )
    }
}

@Composable
private fun DatosRecepcion(recepcion: RecepcionActa) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                stringResource(R.string.campo_rut_bloqueado) to recepcion.rut,
                stringResource(R.string.campo_cargo_bloqueado) to recepcion.cargo,
                stringResource(R.string.acta_fecha_hora) to Formato.fechaHora(recepcion.fechaHora)
            ).let { listOf(stringResource(R.string.acta_nombre) to recepcion.nombre) + it }
                .forEach { (etiqueta, valor) ->
                    Column {
                        Text(etiqueta, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(valor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
        }
    }
}

@Composable
private fun Firma(etiqueta: String, nombre: String?, fecha: String?) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(etiqueta, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = nombre ?: stringResource(R.string.acta_firma_pendiente),
                    fontSize = 15.sp,
                    fontWeight = if (nombre != null) FontWeight.Bold else FontWeight.Normal,
                    color = if (nombre != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = fecha ?: stringResource(R.string.acta_sin_fecha),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (nombre != null && fecha != null) {
                EtiquetaEstado(texto = stringResource(R.string.acta_firmado), tono = TonoEstado.EXITO)
            }
        }
    }
}

@Composable
private fun Aviso(texto: String, tono: TonoEstado) {
    Text(
        text = texto,
        color = tono.texto,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(tono.fondo)
            .padding(12.dp)
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ActaPreview() {
    AppProtocolos_EQ014Theme {
        ActaContent(
            uiState = ActaUiState(
                isLoading = false,
                puedeRegistrarRecepcion = true,
                fechaHora = "27-10-2026 21:15",
                acta = ActaVisita(
                    idVisita = 1,
                    numeroOt = "OT-2026-1187",
                    cliente = "Envasadora del Pacífico S.A.",
                    instalacion = "Planta Lampa",
                    fecha = "2026-09-26",
                    tipoVisita = "Solo pruebas",
                    tecnicos = "Matías Godoy Herrera",
                    ingenieroCargo = "Andrea Contreras Silva",
                    protocolos = listOf(
                        ProtocoloActa(
                            "PT-2026-0401", 1, "TR-01", "Transformador Rhona · serie RH-18733",
                            EstadoProtocolo.REGISTRADO_TERRENO,
                            listOf(
                                PruebaActa(
                                    "Resistencia de aislación", "AT-T", true, null, ResultadoPrueba.OBSERVADO,
                                    listOf(MedicionActa("Lectura a 1 min", 6100.0, "MΩ"), MedicionActa("Lectura a 10 min", 10980.0, "MΩ"))
                                )
                            )
                        )
                    ),
                    recepcion = null
                )
            ),
            onVolver = {},
            onAbrirEquipo = {},
            onNombreCambia = {},
            onRutCambia = {},
            onCargoCambia = {},
            onRegistrar = {}
        )
    }
}
