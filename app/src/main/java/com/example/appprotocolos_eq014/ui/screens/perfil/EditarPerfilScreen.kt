package com.example.appprotocolos_eq014.ui.screens.perfil

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.model.EditarPerfilUiState
import com.example.appprotocolos_eq014.ui.components.BarraSuperior
import com.example.appprotocolos_eq014.ui.components.BotonPrincipal
import com.example.appprotocolos_eq014.ui.components.BotonSecundario
import com.example.appprotocolos_eq014.ui.components.CampoBloqueado
import com.example.appprotocolos_eq014.ui.components.CampoTexto
import com.example.appprotocolos_eq014.ui.theme.AppProtocolos_EQ014Theme
import com.example.appprotocolos_eq014.viewmodel.EditarPerfilViewModel

@Composable
fun EditarPerfilScreen(
    viewModel: EditarPerfilViewModel,
    onVolver: () -> Unit,
    onGuardado: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.guardadoExitoso) {
        if (uiState.guardadoExitoso) {
            onGuardado()
        }
    }

    EditarPerfilContent(
        uiState = uiState,
        onNombreCambia = viewModel::onNombreCambia,
        onRutCambia = viewModel::onRutCambia,
        onCargoCambia = viewModel::onCargoCambia,
        onTelefonoCambia = viewModel::onTelefonoCambia,
        onGuardar = viewModel::onGuardar,
        onVolver = onVolver
    )
}

@Composable
fun EditarPerfilContent(
    uiState: EditarPerfilUiState,
    onNombreCambia: (String) -> Unit,
    onRutCambia: (String) -> Unit,
    onCargoCambia: (String) -> Unit,
    onTelefonoCambia: (String) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit
) {
    val ayudaJefe = stringResource(R.string.ayuda_solo_jefe)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BarraSuperior(titulo = stringResource(R.string.perfil_editar), onVolver = onVolver)

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CampoTexto(
                valor = uiState.nombre,
                onValorCambia = onNombreCambia,
                etiqueta = stringResource(R.string.campo_nombre_completo),
                icono = R.drawable.ic_persona,
                error = uiState.errorNombre
            )

            if (uiState.puedeEditarDatosProtegidos) {
                CampoTexto(
                    valor = uiState.rut,
                    onValorCambia = onRutCambia,
                    etiqueta = stringResource(R.string.campo_rut),
                    icono = R.drawable.ic_persona,
                    error = uiState.errorRut
                )
            } else {
                CampoBloqueado(
                    etiqueta = stringResource(R.string.campo_rut_bloqueado),
                    valor = uiState.rut,
                    ayuda = ayudaJefe
                )
            }

            if (uiState.esCliente) {
                CampoBloqueado(
                    etiqueta = stringResource(R.string.campo_empresa),
                    valor = uiState.empresa,
                    ayuda = ayudaJefe
                )
                if (uiState.puedeEditarDatosProtegidos) {
                    CampoTexto(
                        valor = uiState.cargo,
                        onValorCambia = onCargoCambia,
                        etiqueta = stringResource(R.string.campo_cargo),
                        icono = R.drawable.ic_persona,
                        error = uiState.errorCargo
                    )
                } else {
                    CampoBloqueado(
                        etiqueta = stringResource(R.string.campo_cargo_bloqueado),
                        valor = uiState.cargo,
                        ayuda = ayudaJefe
                    )
                }
            } else {
                CampoBloqueado(
                    etiqueta = stringResource(R.string.campo_perfil),
                    valor = uiState.rol,
                    ayuda = stringResource(R.string.ayuda_perfil_dominio)
                )
            }

            CampoTexto(
                valor = uiState.telefono,
                onValorCambia = onTelefonoCambia,
                etiqueta = stringResource(R.string.campo_telefono),
                icono = R.drawable.ic_telefono,
                error = uiState.errorTelefono,
                tipoTeclado = KeyboardType.Phone
            )

            CampoBloqueado(
                etiqueta = stringResource(R.string.campo_correo),
                valor = uiState.correo,
                ayuda = stringResource(R.string.ayuda_correo_fijo)
            )

            AnimatedVisibility(
                visible = uiState.errorGeneral != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = uiState.errorGeneral.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            BotonPrincipal(
                texto = stringResource(R.string.btn_guardar_cambios),
                onClick = onGuardar,
                habilitado = !uiState.guardando,
                icono = R.drawable.ic_check,
                modifier = Modifier.fillMaxWidth()
            )

            BotonSecundario(
                texto = stringResource(R.string.dialogo_cancelar),
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun EditarPerfilPreview() {
    AppProtocolos_EQ014Theme {
        EditarPerfilContent(
            uiState = EditarPerfilUiState(
                isLoading = false,
                nombre = "Matías Godoy Herrera",
                rut = "17.284.519-3",
                rol = "Técnico de terreno",
                telefono = "+56 9 8123",
                correo = "mgodoy@induztek.ejemplo",
                errorTelefono = "Formato: +56 9 XXXX XXXX"
            ),
            onNombreCambia = {},
            onRutCambia = {},
            onCargoCambia = {},
            onTelefonoCambia = {},
            onGuardar = {},
            onVolver = {}
        )
    }
}
