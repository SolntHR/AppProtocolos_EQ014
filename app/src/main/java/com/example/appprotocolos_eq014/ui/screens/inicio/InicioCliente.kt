package com.example.appprotocolos_eq014.ui.screens.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.model.EquipoCliente
import com.example.appprotocolos_eq014.model.InicioUiState
import com.example.appprotocolos_eq014.model.ProtocoloCliente
import com.example.appprotocolos_eq014.ui.components.CampoTexto
import com.example.appprotocolos_eq014.ui.components.EtiquetaEstado
import com.example.appprotocolos_eq014.ui.components.TonoEstado
import com.example.appprotocolos_eq014.ui.theme.AppProtocolos_EQ014Theme

@Composable
fun InicioCliente(
    uiState: InicioUiState,
    onBusquedaCambia: (String) -> Unit,
    onAbrirEquipo: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(
                    text = uiState.empresa.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.titulo_mis_equipos),
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = stringResource(R.string.subtitulo_consulta),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            CampoTexto(
                valor = uiState.busqueda,
                onValorCambia = onBusquedaCambia,
                etiqueta = stringResource(R.string.campo_buscar_tag),
                icono = R.drawable.ic_buscar
            )
        }

        if (uiState.equiposFiltrados.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.cliente_sin_coincidencias, uiState.busqueda),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }

        items(uiState.equiposFiltrados, key = { it.id }) { equipo ->
            TarjetaEquipoCliente(
                equipo = equipo,
                onClick = { onAbrirEquipo(equipo.id) },
                modifier = Modifier.animateItem()
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_ojo),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.cliente_solo_consulta),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun TarjetaEquipoCliente(
    equipo: EquipoCliente,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_rayo),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = equipo.tag, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = equipo.descripcion,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = stringResource(R.string.cliente_ver_historial),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (equipo.protocolos.isEmpty()) {
                Text(
                    text = stringResource(R.string.cliente_sin_protocolos),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            equipo.protocolos.forEach { protocolo ->
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                FilaProtocoloCliente(protocolo)
            }
        }
    }
}

@Composable
private fun FilaProtocoloCliente(protocolo: ProtocoloCliente) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = protocolo.numero, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = protocolo.fecha, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        EtiquetaEstado(
            texto = protocolo.estado,
            tono = if (protocolo.formalizado) TonoEstado.EXITO else TonoEstado.AVISO
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InicioClientePreview() {
    AppProtocolos_EQ014Theme {
        InicioCliente(
            uiState = InicioUiState(
                isLoading = false,
                rolUsuario = "CLIENTE",
                empresa = "Envasadora del Pacífico S.A.",
                equiposCliente = listOf(
                    EquipoCliente(
                        1, "TR-01", "Transformador Rhona · Planta Lampa",
                        listOf(
                            ProtocoloCliente("PT-2026-0401", "26-09-2026", "Pendiente de formalización", false),
                            ProtocoloCliente("PT-2025-0288", "13-09-2025", "Formalizado", true)
                        )
                    )
                )
            ),
            onBusquedaCambia = {},
            onAbrirEquipo = {}
        )
    }
}
