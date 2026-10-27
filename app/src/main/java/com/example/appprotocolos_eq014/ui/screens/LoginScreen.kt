package com.example.appprotocolos_eq014.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import com.example.appprotocolos_eq014.ui.components.BotonCuentasPrueba
import com.example.appprotocolos_eq014.ui.components.CONTRASENA_PRUEBA
import com.example.appprotocolos_eq014.ui.components.MenuCuentasPrueba
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.model.LoginUiState
import com.example.appprotocolos_eq014.ui.components.BotonPrincipal
import com.example.appprotocolos_eq014.ui.components.CampoContrasena
import com.example.appprotocolos_eq014.ui.components.CampoTexto
import com.example.appprotocolos_eq014.ui.components.EncabezadoInduztek
import com.example.appprotocolos_eq014.ui.theme.AppProtocolos_EQ014Theme
import com.example.appprotocolos_eq014.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginExitoso: () -> Unit,
    onOlvidasteContrasena: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.loginExitoso) {
        if (uiState.loginExitoso) {
            onLoginExitoso()
        }
    }

    LoginContent(
        uiState = uiState,
        onCorreoCambia = viewModel::onCorreoCambia,
        onContrasenaCambia = viewModel::onContrasenaCambia,
        onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
        onIngresar = viewModel::onIngresar,
        onOlvidasteContrasena = onOlvidasteContrasena
    )
}

@Composable
fun LoginContent(
    uiState: LoginUiState,
    onCorreoCambia: (String) -> Unit,
    onContrasenaCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onIngresar: () -> Unit,
    onOlvidasteContrasena: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
    ) {
        EncabezadoInduztek(
            titulo = stringResource(R.string.login_bienvenido),
            subtitulo = stringResource(R.string.login_subtitulo)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            var mostrarCuentasPrueba by remember { mutableStateOf(false) }
            Box {
                CampoTexto(
                    valor = uiState.correo,
                    onValorCambia = onCorreoCambia,
                    etiqueta = stringResource(R.string.campo_correo),
                    icono = R.drawable.ic_correo,
                    error = uiState.errorCorreo,
                    tipoTeclado = KeyboardType.Email,
                    iconoFinal = {
                        BotonCuentasPrueba(
                            expandido = mostrarCuentasPrueba,
                            onClick = { mostrarCuentasPrueba = !mostrarCuentasPrueba }
                        )
                    }
                )
                MenuCuentasPrueba(
                    expandido = mostrarCuentasPrueba,
                    onCerrar = { mostrarCuentasPrueba = false },
                    onSeleccionar = { cuenta ->
                        onCorreoCambia(cuenta.correo)
                        onContrasenaCambia(CONTRASENA_PRUEBA)
                        mostrarCuentasPrueba = false
                    }
                )
            }

            CampoContrasena(
                valor = uiState.contrasena,
                onValorCambia = onContrasenaCambia,
                etiqueta = stringResource(R.string.campo_contrasena),
                visible = uiState.contrasenaVisible,
                onAlternarVisible = onAlternarVisibilidad,
                error = uiState.errorContrasena
            )

            BotonPrincipal(
                texto = stringResource(R.string.btn_ingresar),
                onClick = onIngresar,
                habilitado = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            TextButton(onClick = onOlvidasteContrasena) {
                Text(
                    text = stringResource(R.string.olvido_contrasena),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    AppProtocolos_EQ014Theme {
        LoginContent(
            uiState = LoginUiState(),
            onCorreoCambia = {},
            onContrasenaCambia = {},
            onAlternarVisibilidad = {},
            onIngresar = {},
            onOlvidasteContrasena = {}
        )
    }
}
