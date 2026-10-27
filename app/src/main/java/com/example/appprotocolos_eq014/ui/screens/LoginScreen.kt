package com.example.appprotocolos_eq014.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.ui.components.BotonPrincipal
import com.example.appprotocolos_eq014.ui.components.CampoContrasena
import com.example.appprotocolos_eq014.ui.components.CampoTexto
import com.example.appprotocolos_eq014.ui.components.ImagenEncabezado
import com.example.appprotocolos_eq014.viewmodel.LoginViewModel

import androidx.compose.ui.tooling.preview.Preview
import com.example.appprotocolos_eq014.ui.theme.AppProtocolos_EQ014Theme
import com.example.appprotocolos_eq014.model.LoginUiState

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginExitoso: () -> Unit
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
        onIngresar = viewModel::onIngresar
    )
}

@Composable
fun LoginContent(
    uiState: LoginUiState,
    onCorreoCambia: (String) -> Unit,
    onContrasenaCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onIngresar: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ImagenEncabezado(
            imagen = R.drawable.banner_registro,
            descripcion = "Logo Induztek",
            titulo = "Bienvenido",
            subtitulo = "Registra pruebas incluso sin conexión."
        )

        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            CampoTexto(
                valor = uiState.correo,
                onValorCambia = onCorreoCambia,
                etiqueta = "Correo electrónico",
                icono = R.drawable.ic_correo,
                error = uiState.errorCorreo
            )

            CampoContrasena(
                valor = uiState.contrasena,
                onValorCambia = onContrasenaCambia,
                etiqueta = "Contraseña",
                visible = uiState.contrasenaVisible,
                onAlternarVisible = onAlternarVisibilidad,
                error = uiState.errorContrasena
            )

            Spacer(modifier = Modifier.height(8.dp))

            BotonPrincipal(
                texto = "Ingresar",
                onClick = onIngresar,
                habilitado = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    AppProtocolos_EQ014Theme {
        LoginContent(
            uiState = LoginUiState(
                correo = "usuario@ejemplo.com",
                contrasena = "123456",
                contrasenaVisible = false,
                isLoading = false,
                errorCorreo = null,
                errorContrasena = null
            ),
            onCorreoCambia = {},
            onContrasenaCambia = {},
            onAlternarVisibilidad = {},
            onIngresar = {}
        )
    }
}
