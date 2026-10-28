package com.example.appprotocolos_eq014.ui.screens.instrumentos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.model.EstadoCalibracion
import com.example.appprotocolos_eq014.model.InstrumentoCalibracion
import com.example.appprotocolos_eq014.model.InstrumentosUiState
import com.example.appprotocolos_eq014.ui.components.BotonPrincipal
import com.example.appprotocolos_eq014.ui.components.BotonSecundario
import com.example.appprotocolos_eq014.ui.components.CampoTexto
import com.example.appprotocolos_eq014.ui.components.EtiquetaEstado
import com.example.appprotocolos_eq014.ui.components.TonoEstado
import com.example.appprotocolos_eq014.ui.theme.AppProtocolos_EQ014Theme
import com.example.appprotocolos_eq014.util.Formato
import com.example.appprotocolos_eq014.viewmodel.InstrumentosViewModel

@Composable
fun InstrumentosScreen(viewModel: InstrumentosViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMensajeMostrado()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        InstrumentosContent(
            uiState = uiState,
            onRegistrarCalibracion = viewModel::onRegistrarCalibracion
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }

    uiState.instrumentoEditando?.let { instrumento ->
        HojaCalibracion(
            instrumento = instrumento,
            uiState = uiState,
            onFechaCalibracionCambia = viewModel::onFechaCalibracionCambia,
            onFechaVencimientoCambia = viewModel::onFechaVencimientoCambia,
            onGuardar = viewModel::onGuardarCalibracion,
            onCerrar = viewModel::onCerrarFormulario
        )
    }
}

@Composable
fun InstrumentosContent(
    uiState: InstrumentosUiState,
    onRegistrarCalibracion: (InstrumentoCalibracion) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 6.dp)) {
                Text(
                    text = stringResource(R.string.titulo_instrumentos),
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = stringResource(R.string.instrumentos_resumen, uiState.porVencerOVencidos),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (uiState.isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
        items(uiState.instrumentos, key = { it.id }) { instrumento ->
            TarjetaInstrumento(
                instrumento = instrumento,
                onRegistrar = { onRegistrarCalibracion(instrumento) },
                modifier = Modifier.animateItem()
            )
        }
        item {
            Text(
                text = stringResource(R.string.instrumentos_nota),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun TarjetaInstrumento(
    instrumento: InstrumentoCalibracion,
    onRegistrar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = instrumento.nombre, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = stringResource(R.string.instrumento_serie, instrumento.marcaModelo, instrumento.numeroSerie),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                EtiquetaEstado(texto = instrumento.estado.etiqueta, tono = tonoDe(instrumento.estado))
            }
            Text(text = textoVencimiento(instrumento), fontSize = 12.sp)
            TextButton(onClick = onRegistrar, contentPadding = PaddingValues(0.dp)) {
                Text(
                    text = stringResource(R.string.btn_registrar_calibracion),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun textoVencimiento(instrumento: InstrumentoCalibracion): String {
    val vencimiento = instrumento.fechaVencimiento ?: return stringResource(R.string.instrumento_sin_calibracion)
    val dias = instrumento.diasRestantes ?: 0
    val fecha = Formato.fecha(vencimiento)
    return when {
        dias < 0 -> stringResource(R.string.instrumento_vencido, fecha, -dias)
        else -> stringResource(R.string.instrumento_vigente, fecha, dias)
    }
}

private fun tonoDe(estado: EstadoCalibracion): TonoEstado = when (estado) {
    EstadoCalibracion.VIGENTE -> TonoEstado.EXITO
    EstadoCalibracion.POR_VENCER -> TonoEstado.AVISO
    EstadoCalibracion.VENCIDA -> TonoEstado.ERROR
    EstadoCalibracion.SIN_CALIBRACION -> TonoEstado.NEUTRO
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaCalibracion(
    instrumento: InstrumentoCalibracion,
    uiState: InstrumentosUiState,
    onFechaCalibracionCambia: (String) -> Unit,
    onFechaVencimientoCambia: (String) -> Unit,
    onGuardar: () -> Unit,
    onCerrar: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onCerrar,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(start = 18.dp, end = 18.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.btn_registrar_calibracion),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${instrumento.nombre} · ${instrumento.numeroSerie}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            CampoTexto(
                valor = uiState.fechaCalibracion,
                onValorCambia = onFechaCalibracionCambia,
                etiqueta = stringResource(R.string.campo_fecha_calibracion),
                icono = R.drawable.ic_check,
                error = uiState.errorFechaCalibracion
            )
            CampoTexto(
                valor = uiState.fechaVencimiento,
                onValorCambia = onFechaVencimientoCambia,
                etiqueta = stringResource(R.string.campo_fecha_vencimiento),
                icono = R.drawable.ic_check,
                error = uiState.errorFechaVencimiento
            )
            BotonPrincipal(
                texto = stringResource(R.string.btn_guardar_cambios),
                onClick = onGuardar,
                habilitado = !uiState.guardando,
                modifier = Modifier.fillMaxWidth()
            )
            BotonSecundario(
                texto = stringResource(R.string.dialogo_cancelar),
                onClick = onCerrar,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun InstrumentosPreview() {
    AppProtocolos_EQ014Theme {
        InstrumentosContent(
            uiState = InstrumentosUiState(
                isLoading = false,
                instrumentos = listOf(
                    InstrumentoCalibracion(4, "Micro-ohmímetro", "Megger DLRO10X", "DLR-10882", "2025-08-20", "2026-08-20", -68, EstadoCalibracion.VENCIDA),
                    InstrumentoCalibracion(1, "Megóhmetro 5 kV", "Megger MIT525", "MIT-0045821", "2026-03-15", "2027-03-15", 139, EstadoCalibracion.VIGENTE)
                )
            ),
            onRegistrarCalibracion = {}
        )
    }
}
