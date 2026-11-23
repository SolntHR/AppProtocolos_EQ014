package com.example.appprotocolos_eq014.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.model.uistate.PreguntasSeguridad
import com.example.appprotocolos_eq014.model.uistate.RegistroUiState
import com.example.appprotocolos_eq014.ui.components.BarraSuperior
import com.example.appprotocolos_eq014.ui.components.BotonPrincipal
import com.example.appprotocolos_eq014.ui.components.CampoBloqueado
import com.example.appprotocolos_eq014.ui.components.CampoContrasena
import com.example.appprotocolos_eq014.ui.components.CampoTexto
import com.example.appprotocolos_eq014.ui.components.MensajeAvisoProtocolo
import com.example.appprotocolos_eq014.ui.components.SelectorDesplegable
import com.example.appprotocolos_eq014.ui.components.TarjetaSeccion
import com.example.appprotocolos_eq014.ui.theme.AppProtocolos_EQ014Theme
import com.example.appprotocolos_eq014.util.ValidacionesRegistro
import com.example.appprotocolos_eq014.viewmodel.RegistroViewModel

@Composable
fun RegistroScreen(viewModel: RegistroViewModel, onVolver: () -> Unit) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    RegistroContent(
        estado = estado,
        onVolver = onVolver,
        onNombreCambia = viewModel::onNombreCambia,
        onRutCambia = viewModel::onRutCambia,
        onTelefonoCambia = viewModel::onTelefonoCambia,
        onCorreoCambia = viewModel::onCorreoCambia,
        onPreguntaCambia = viewModel::onPreguntaCambia,
        onRespuestaCambia = viewModel::onRespuestaCambia,
        onAlternarRespuesta = viewModel::onAlternarRespuesta,
        onRegistrar = viewModel::onRegistrar
    )
}

@Composable
private fun RegistroContent(
    estado: RegistroUiState,
    onVolver: () -> Unit,
    onNombreCambia: (String) -> Unit,
    onRutCambia: (String) -> Unit,
    onTelefonoCambia: (String) -> Unit,
    onCorreoCambia: (String) -> Unit,
    onPreguntaCambia: (String) -> Unit,
    onRespuestaCambia: (String) -> Unit,
    onAlternarRespuesta: () -> Unit,
    onRegistrar: () -> Unit
) {
    val foco = LocalFocusManager.current
    val datos = estado.datos
    val errores = estado.errores
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 480.dp).fillMaxSize().imePadding()) {
            BarraSuperior(titulo = stringResource(R.string.registro_titulo), onVolver = onVolver)
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (estado.correoCreado != null) {
                    Text(stringResource(R.string.registro_exito), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.registro_exito_detalle), style = MaterialTheme.typography.bodyMedium)
                    CampoBloqueado(stringResource(R.string.registro_correo_confirmado), estado.correoCreado,
                        stringResource(R.string.registro_rol_pendiente))
                    BotonPrincipal(stringResource(R.string.registro_volver_login), onVolver, Modifier.fillMaxWidth())
                } else {
                    Text(
                        stringResource(R.string.registro_introduccion),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    CampoTexto(datos.nombre, onNombreCambia, stringResource(R.string.registro_nombre),
                        R.drawable.ic_persona, error = errores.nombre, habilitado = !estado.cargando)
                    CampoTexto(datos.rut, onRutCambia, "RUT", R.drawable.ic_perfil,
                        error = errores.rut, habilitado = !estado.cargando)
                    CampoTexto(datos.telefono, onTelefonoCambia, stringResource(R.string.registro_telefono),
                        R.drawable.ic_telefono, error = errores.telefono, tipoTeclado = KeyboardType.Phone,
                        habilitado = !estado.cargando)
                    AyudaRegistro(stringResource(R.string.registro_telefono_ayuda))
                    CampoTexto(datos.correoPersonal, onCorreoCambia, stringResource(R.string.registro_correo_personal),
                        R.drawable.ic_correo, error = errores.correoPersonal, tipoTeclado = KeyboardType.Email,
                        habilitado = !estado.cargando)
                    AyudaRegistro(stringResource(R.string.registro_correo_ayuda))
                    CampoBloqueado(
                        etiqueta = stringResource(R.string.registro_correo_interno),
                        valor = ValidacionesRegistro.correoInternoSugerido(datos.nombre)
                            .ifBlank { stringResource(R.string.registro_correo_sin_nombre) },
                        ayuda = stringResource(R.string.registro_correo_sugerido)
                    )
                    TarjetaSeccion(stringResource(R.string.registro_seguridad)) {
                        AyudaRegistro(stringResource(R.string.registro_seguridad_ayuda))
                        SelectorDesplegable(
                            etiqueta = stringResource(R.string.registro_pregunta),
                            opciones = PreguntasSeguridad.lista,
                            seleccion = datos.preguntaSeguridad,
                            textoDe = { it },
                            onSeleccion = onPreguntaCambia,
                            error = errores.preguntaSeguridad,
                            habilitado = !estado.cargando
                        )
                        CampoContrasena(
                            valor = datos.respuestaSeguridad,
                            onValorCambia = onRespuestaCambia,
                            etiqueta = stringResource(R.string.registro_respuesta),
                            visible = estado.respuestaVisible,
                            onAlternarVisible = onAlternarRespuesta,
                            error = errores.respuestaSeguridad,
                            habilitado = !estado.cargando,
                            descripcionVisibilidad = stringResource(if (estado.respuestaVisible)
                                R.string.registro_ocultar_respuesta else R.string.registro_mostrar_respuesta)
                        )
                    }
                    if (errores.hayErrores) {
                        MensajeAvisoProtocolo(stringResource(R.string.registro_revisar_campos))
                    }
                    estado.errorGeneral?.let { MensajeAvisoProtocolo(it) }
                    BotonPrincipal(
                        texto = stringResource(if (estado.cargando) R.string.registro_creando else R.string.registro_titulo),
                        onClick = { foco.clearFocus(); onRegistrar() },
                        modifier = Modifier.fillMaxWidth(),
                        habilitado = !estado.cargando
                    )
                    TextButton(onClick = onVolver, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text(stringResource(R.string.registro_ya_tengo_cuenta))
                    }
                }
            }
        }
    }
}

@Composable
private fun AyudaRegistro(texto: String) {
    Text(texto, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
private fun RegistroPreview() {
    AppProtocolos_EQ014Theme {
        RegistroContent(RegistroUiState(), {}, {}, {}, {}, {}, {}, {}, {}, {})
    }
}
