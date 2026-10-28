package com.example.appprotocolos_eq014.ui.screens.historial

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.model.FilaComparacion
import com.example.appprotocolos_eq014.model.FilaHistorial
import com.example.appprotocolos_eq014.model.HistorialUiState
import com.example.appprotocolos_eq014.model.ResultadoPrueba
import com.example.appprotocolos_eq014.ui.components.BarraSuperior
import com.example.appprotocolos_eq014.ui.components.BotonSecundario
import com.example.appprotocolos_eq014.ui.components.CampoTexto
import com.example.appprotocolos_eq014.ui.components.EtiquetaEstado
import com.example.appprotocolos_eq014.ui.components.EtiquetaResultado
import com.example.appprotocolos_eq014.ui.components.TituloSeccion
import com.example.appprotocolos_eq014.ui.components.TonoEstado
import com.example.appprotocolos_eq014.ui.components.tonoDe
import com.example.appprotocolos_eq014.ui.theme.AppProtocolos_EQ014Theme
import com.example.appprotocolos_eq014.viewmodel.HistorialViewModel

@Composable
fun HistorialScreen(
    viewModel: HistorialViewModel,
    onVolver: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HistorialContent(
        uiState = uiState,
        onVolver = onVolver,
        onComentarioCambia = viewModel::onComentarioCambia,
        onGuardarComentario = viewModel::onGuardarComentario
    )
}

@Composable
fun HistorialContent(
    uiState: HistorialUiState,
    onVolver: () -> Unit,
    onComentarioCambia: (String) -> Unit,
    onGuardarComentario: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BarraSuperior(titulo = stringResource(R.string.titulo_historial_equipo), onVolver = onVolver)

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        val visible = remember { MutableTransitionState(false).apply { targetState = true } }
        AnimatedVisibility(visibleState = visible, enter = fadeIn() + slideInVertically { it / 12 }) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                uiState.error?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }

                EncabezadoEquipo(tag = uiState.tag, descripcion = uiState.descripcion)

                TituloSeccion(stringResource(R.string.seccion_protocolos))
                if (uiState.protocolos.isEmpty()) {
                    Text(
                        text = stringResource(R.string.historial_sin_protocolos),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                uiState.protocolos.forEach { fila -> FilaLineaTiempo(fila) }

                if (uiState.comparacion.isNotEmpty()) {
                    TituloSeccion(stringResource(R.string.seccion_comparacion))
                    TarjetaComparacion(
                        filas = uiState.comparacion,
                        mostrarComentario = uiState.haySignificativa && !uiState.esCliente,
                        comentario = uiState.comentario,
                        errorComentario = uiState.errorComentario,
                        comentarioGuardado = uiState.comentarioGuardado,
                        onComentarioCambia = onComentarioCambia,
                        onGuardarComentario = onGuardarComentario
                    )
                }
            }
        }
    }
}

@Composable
private fun EncabezadoEquipo(tag: String, descripcion: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_rayo),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
        }
        Column {
            Text(text = tag, style = MaterialTheme.typography.headlineMedium)
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FilaLineaTiempo(fila: FilaHistorial) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(tonoDe(fila.resultado).texto)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${fila.numero} · ${fila.fecha}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                fila.detalle?.let {
                    Text(text = it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                EtiquetaEstado(
                    texto = fila.estado,
                    tono = if (fila.formalizado) TonoEstado.EXITO else TonoEstado.AZUL
                )
            }
            EtiquetaResultado(fila.resultado)
        }
    }
}

@Composable
private fun TarjetaComparacion(
    filas: List<FilaComparacion>,
    mostrarComentario: Boolean,
    comentario: String,
    errorComentario: String?,
    comentarioGuardado: Boolean,
    onComentarioCambia: (String) -> Unit,
    onGuardarComentario: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(R.string.comparacion_titulo),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Row {
                listOf(R.string.comparacion_protocolo, R.string.comparacion_valor, R.string.comparacion_variacion)
                    .forEach { etiqueta ->
                        Text(
                            text = stringResource(etiqueta),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
            }
            filas.forEachIndexed { indice, fila ->
                if (indice > 0) HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(fila.fecha, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(fila.numero, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(fila.valor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(fila.anterior ?: "—", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        if (fila.variacion == null) {
                            Text(
                                text = stringResource(R.string.comparacion_sin_previo),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = fila.variacion,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (fila.significativa) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                            if (fila.significativa) {
                                Text(
                                    text = stringResource(R.string.comparacion_significativa),
                                    fontSize = 10.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            if (mostrarComentario) {
                AvisoComentario(comentarioGuardado)
                CampoTexto(
                    valor = comentario,
                    onValorCambia = onComentarioCambia,
                    etiqueta = stringResource(R.string.campo_comentario_tecnico),
                    icono = R.drawable.ic_lapiz,
                    error = errorComentario
                )
                BotonSecundario(
                    texto = stringResource(R.string.btn_guardar_comentario),
                    onClick = onGuardarComentario,
                    icono = R.drawable.ic_check,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun AvisoComentario(guardado: Boolean) {
    val tono = if (guardado) TonoEstado.EXITO else TonoEstado.AVISO
    val fondo by animateColorAsState(tono.fondo, label = "fondo_aviso")
    val texto by animateColorAsState(tono.texto, label = "texto_aviso")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(fondo)
            .padding(12.dp)
    ) {
        Text(
            text = stringResource(if (guardado) R.string.comentario_registrado else R.string.comentario_requerido),
            color = texto,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        if (!guardado) {
            Text(
                text = stringResource(R.string.comentario_detalle),
                color = texto,
                fontSize = 12.sp
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HistorialPreview() {
    AppProtocolos_EQ014Theme {
        HistorialContent(
            uiState = HistorialUiState(
                isLoading = false,
                tag = "TR-01",
                descripcion = "Transformador Rhona · RH-18733 · 1.500 kVA · Planta Lampa",
                protocolos = listOf(
                    FilaHistorial("PT-2026-0401", "26-09-2026", "Registrado en terreno", false, ResultadoPrueba.OBSERVADO, "6.100 MΩ / 10.980 MΩ · IP 1,80"),
                    FilaHistorial("PT-2025-0288", "13-09-2025", "Formalizado", true, ResultadoPrueba.APROBADO, "9.800 MΩ / 21.560 MΩ · IP 2,20")
                ),
                comparacion = listOf(
                    FilaComparacion("PT-2026-0401", "26-09-2026", "6.100 MΩ", "Ant. 9.800 MΩ", "-37,8 %", true),
                    FilaComparacion("PT-2025-0288", "13-09-2025", "9.800 MΩ", null, null, false)
                ),
                haySignificativa = true
            ),
            onVolver = {},
            onComentarioCambia = {},
            onGuardarComentario = {}
        )
    }
}
