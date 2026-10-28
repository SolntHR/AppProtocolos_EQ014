package com.example.appprotocolos_eq014.ui.screens.inicio

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.model.InicioUiState
import com.example.appprotocolos_eq014.model.VisitaResumen
import com.example.appprotocolos_eq014.ui.components.AvatarIniciales
import com.example.appprotocolos_eq014.ui.components.EtiquetaEstado
import com.example.appprotocolos_eq014.ui.components.TituloSeccion
import com.example.appprotocolos_eq014.ui.components.TonoEstado
import com.example.appprotocolos_eq014.ui.theme.AppProtocolos_EQ014Theme
import com.example.appprotocolos_eq014.ui.theme.AzulOscuro
import com.example.appprotocolos_eq014.ui.theme.AzulPrimario
import com.example.appprotocolos_eq014.ui.theme.AzulTextoCabecera
import com.example.appprotocolos_eq014.ui.theme.Blanco
import com.example.appprotocolos_eq014.util.Formato

@Composable
fun InicioPersonalTerreno(
    uiState: InicioUiState,
    onAbrirVisita: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { EncabezadoInicio(uiState) }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                TituloSeccion(stringResource(R.string.titulo_mis_visitas))
                Text(
                    text = pluralStringResource(R.plurals.ordenes_asignadas, uiState.visitas.size, uiState.visitas.size),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (uiState.visitas.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.inicio_sin_visitas),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        items(uiState.visitas, key = { it.id }) { visita ->
            TarjetaVisita(
                visita = visita,
                onClick = { onAbrirVisita(visita.id) },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .animateItem()
            )
        }
    }
}

@Composable
private fun EncabezadoInicio(uiState: InicioUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(Brush.linearGradient(listOf(AzulPrimario, AzulOscuro)))
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uiState.fechaHoy,
                    style = MaterialTheme.typography.labelSmall,
                    color = AzulTextoCabecera
                )
                Text(
                    text = stringResource(R.string.saludo, uiState.primerNombre),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Blanco
                )
            }
            AvatarIniciales(nombre = uiState.nombreCompleto, tamano = 46.dp)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Blanco.copy(alpha = 0.12f))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = Blanco,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = stringResource(R.string.estado_guardado_local, uiState.rolNombre),
                    color = Blanco,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.datos_guardados),
                    color = AzulTextoCabecera,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun TarjetaVisita(
    visita: VisitaResumen,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val proporcion = if (visita.totalProtocolos == 0) 0f else visita.protocolosRegistrados.toFloat() / visita.totalProtocolos
    var iniciado by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { iniciado = true }
    val progreso by animateFloatAsState(
        targetValue = if (iniciado) proporcion else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "progreso_visita"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        if (visita.cerrada) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.secondary
                    )
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = visita.numeroOt,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    EtiquetaEstado(
                        texto = stringResource(if (visita.cerrada) R.string.visita_cerrada else R.string.etiqueta_en_curso),
                        tono = if (visita.cerrada) TonoEstado.NEUTRO else TonoEstado.AVISO
                    )
                }
                Text(text = visita.cliente, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "${visita.instalacion} · ${Formato.fecha(visita.fecha)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    Text(
                        text = stringResource(
                            R.string.visita_progreso,
                            visita.protocolosRegistrados,
                            visita.totalProtocolos
                        ),
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = "${(proporcion * 100).toInt()} %", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(
                    progress = { progreso },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_volver),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 10.dp)
                    .size(20.dp)
                    .rotate(180f)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun InicioPersonalTerrenoPreview() {
    AppProtocolos_EQ014Theme {
        InicioPersonalTerreno(
            uiState = InicioUiState(
                isLoading = false,
                primerNombre = "Matías",
                nombreCompleto = "Matías Godoy Herrera",
                rolNombre = "Técnico de terreno",
                fechaHoy = "MARTES, 27 OCT 2026",
                visitas = listOf(
                    VisitaResumen(1, "OT-2026-1187", "Envasadora del Pacífico S.A.", "Planta Lampa", "2026-09-26", false, 2, 2),
                    VisitaResumen(3, "OT-2024-0951", "Envasadora del Pacífico S.A.", "Planta Lampa", "2024-09-14", true, 1, 1)
                )
            ),
            onAbrirVisita = {}
        )
    }
}
