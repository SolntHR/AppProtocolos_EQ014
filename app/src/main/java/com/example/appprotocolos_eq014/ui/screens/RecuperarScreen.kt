package com.example.appprotocolos_eq014.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.model.uistate.MetodoRecuperacion
import com.example.appprotocolos_eq014.model.uistate.PasoRecuperacion
import com.example.appprotocolos_eq014.model.uistate.RecuperarUiState
import com.example.appprotocolos_eq014.ui.components.BarraSuperior
import com.example.appprotocolos_eq014.ui.components.BotonPrincipal
import com.example.appprotocolos_eq014.ui.components.CampoContrasena
import com.example.appprotocolos_eq014.ui.components.CampoTexto
import com.example.appprotocolos_eq014.ui.components.MensajeAvisoProtocolo
import com.example.appprotocolos_eq014.ui.components.SelectorDesplegable
import com.example.appprotocolos_eq014.ui.components.SelectorSegmentado
import com.example.appprotocolos_eq014.ui.components.TituloSeccion
import com.example.appprotocolos_eq014.ui.theme.AppProtocolos_EQ014Theme
import com.example.appprotocolos_eq014.viewmodel.RecuperarViewModel

@Composable
fun RecuperarScreen(
    viewModel: RecuperarViewModel,
    onVolver: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RecuperarContent(
        uiState = uiState,
        onVolver = onVolver,
        onMetodoCambia = viewModel::onMetodoCambia,
        onCorreoCambia = viewModel::onCorreoCambia,
        onCodigoCambia = viewModel::onCodigoCambia,
        onEnviarCodigo = viewModel::onEnviarCodigo,
        onVerificarCodigo = viewModel::onVerificarCodigo,
        onYaTengoCodigo = viewModel::onYaTengoCodigo,
        onPreguntaCambia = viewModel::onPreguntaCambia,
        onRespuestaCambia = viewModel::onRespuestaCambia,
        onVerificarPregunta = viewModel::onVerificarPregunta,
        onNuevaCambia = viewModel::onNuevaCambia,
        onConfirmacionCambia = viewModel::onConfirmacionCambia,
        onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
        onGuardarContrasena = viewModel::onGuardarContrasena
    )
}

@Composable
fun RecuperarContent(
    uiState: RecuperarUiState,
    onVolver: () -> Unit,
    onMetodoCambia: (MetodoRecuperacion) -> Unit,
    onCorreoCambia: (String) -> Unit,
    onCodigoCambia: (String) -> Unit,
    onEnviarCodigo: () -> Unit,
    onVerificarCodigo: () -> Unit,
    onYaTengoCodigo: () -> Unit,
    onPreguntaCambia: (String) -> Unit,
    onRespuestaCambia: (String) -> Unit,
    onVerificarPregunta: () -> Unit,
    onNuevaCambia: (String) -> Unit,
    onConfirmacionCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onGuardarContrasena: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BarraSuperior(titulo = stringResource(R.string.titulo_recuperar), onVolver = onVolver)

        if (uiState.cargando) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        AnimatedContent(
            targetState = uiState.paso,
            transitionSpec = { (fadeIn() + slideInHorizontally { it / 6 }) togetherWith fadeOut() },
            label = "paso_recuperacion"
        ) { paso ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (paso) {
                    PasoRecuperacion.VERIFICAR -> {
                        TituloSeccion(stringResource(R.string.olvido_contrasena))
                        SelectorSegmentado(
                            etiqueta = "¿Cómo quieres recuperarla?",
                            opciones = MetodoRecuperacion.entries,
                            seleccionada = uiState.metodo,
                            textoDe = { it.texto },
                            onSeleccionar = onMetodoCambia
                        )
                        uiState.errorGeneral?.let { MensajeAvisoProtocolo(it) }
                        CampoTexto(
                            valor = uiState.correo,
                            onValorCambia = onCorreoCambia,
                            etiqueta = stringResource(R.string.campo_correo),
                            icono = R.drawable.ic_correo,
                            error = uiState.errorCorreo,
                            tipoTeclado = KeyboardType.Email,
                            habilitado = !uiState.cargando
                        )

                        if (uiState.metodo == MetodoRecuperacion.CORREO) {
                            Explicacion(
                                "Escribe tu correo de acceso. El código de recuperación llegará a tu correo personal " +
                                    "y vence en 10 minutos. Si tienes un código de activación, puedes ingresarlo directamente."
                            )
                            if (uiState.codigoEnviado) {
                                uiState.mensajeCodigo?.let {
                                    MensajeAvisoProtocolo(it, esError = false)
                                }
                                CampoTexto(
                                    valor = uiState.codigo,
                                    onValorCambia = onCodigoCambia,
                                    etiqueta = "Código de 6 dígitos",
                                    icono = R.drawable.ic_candado,
                                    error = uiState.errorCodigo,
                                    tipoTeclado = KeyboardType.Number,
                                    habilitado = !uiState.cargando
                                )
                                BotonPrincipal(
                                    texto = "Verificar código",
                                    onClick = onVerificarCodigo,
                                    habilitado = !uiState.cargando,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                TextButton(
                                    onClick = onEnviarCodigo,
                                    enabled = !uiState.cargando,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Reenviar código")
                                }
                            } else {
                                BotonPrincipal(
                                    texto = "Enviar código",
                                    onClick = onEnviarCodigo,
                                    habilitado = !uiState.cargando,
                                    icono = R.drawable.ic_correo,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                TextButton(
                                    onClick = onYaTengoCodigo,
                                    enabled = !uiState.cargando,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Ya tengo un código de activación")
                                }
                            }
                        } else {
                            Explicacion("Elige tu pregunta de seguridad y escribe la respuesta.")
                            SelectorDesplegable(
                                etiqueta = "Pregunta de seguridad",
                                opciones = uiState.preguntas,
                                seleccion = uiState.pregunta,
                                textoDe = { it },
                                onSeleccion = onPreguntaCambia,
                                error = uiState.errorPregunta,
                                textoVacio = "Selecciona tu pregunta",
                                habilitado = !uiState.cargando
                            )
                            CampoTexto(
                                valor = uiState.respuesta,
                                onValorCambia = onRespuestaCambia,
                                etiqueta = stringResource(R.string.recuperar_respuesta),
                                icono = R.drawable.ic_candado,
                                error = uiState.errorRespuesta,
                                habilitado = !uiState.cargando
                            )
                            BotonPrincipal(
                                texto = stringResource(R.string.btn_verificar),
                                onClick = onVerificarPregunta,
                                habilitado = !uiState.cargando,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    PasoRecuperacion.NUEVA_CONTRASENA -> {
                        TituloSeccion(stringResource(R.string.recuperar_titulo_nueva))
                        Explicacion(stringResource(R.string.recuperar_paso_nueva))
                        uiState.errorGeneral?.let { MensajeAvisoProtocolo(it) }
                        CampoContrasena(
                            valor = uiState.nuevaContrasena,
                            onValorCambia = onNuevaCambia,
                            etiqueta = stringResource(R.string.recuperar_nueva),
                            visible = uiState.contrasenaVisible,
                            onAlternarVisible = onAlternarVisibilidad,
                            error = uiState.errorNueva
                        )
                        CampoContrasena(
                            valor = uiState.confirmacion,
                            onValorCambia = onConfirmacionCambia,
                            etiqueta = stringResource(R.string.recuperar_confirmar),
                            visible = uiState.contrasenaVisible,
                            onAlternarVisible = onAlternarVisibilidad,
                            error = uiState.errorConfirmacion
                        )
                        BotonPrincipal(
                            texto = stringResource(R.string.btn_guardar_contrasena),
                            onClick = onGuardarContrasena,
                            habilitado = !uiState.cargando,
                            icono = R.drawable.ic_check,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    PasoRecuperacion.LISTO -> {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_check),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        Text(
                            text = stringResource(R.string.recuperar_listo),
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Explicacion(stringResource(R.string.recuperar_listo_detalle))
                        BotonPrincipal(
                            texto = stringResource(R.string.btn_volver_login),
                            onClick = onVolver,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Explicacion(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RecuperarPreview() {
    AppProtocolos_EQ014Theme {
        RecuperarContent(
            uiState = RecuperarUiState(),
            onVolver = {},
            onMetodoCambia = {},
            onCorreoCambia = {},
            onCodigoCambia = {},
            onEnviarCodigo = {},
            onVerificarCodigo = {},
            onYaTengoCodigo = {},
            onPreguntaCambia = {},
            onRespuestaCambia = {},
            onVerificarPregunta = {},
            onNuevaCambia = {},
            onConfirmacionCambia = {},
            onAlternarVisibilidad = {},
            onGuardarContrasena = {}
        )
    }
}
