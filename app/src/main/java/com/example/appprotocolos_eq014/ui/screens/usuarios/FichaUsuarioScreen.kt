package com.example.appprotocolos_eq014.ui.screens.usuarios

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.model.uistate.FichaUsuarioUiState
import com.example.appprotocolos_eq014.model.uistate.RolPersonal
import com.example.appprotocolos_eq014.ui.components.BarraSuperior
import com.example.appprotocolos_eq014.ui.components.BotonPrincipal
import com.example.appprotocolos_eq014.ui.components.BotonSecundario
import com.example.appprotocolos_eq014.ui.components.CampoBloqueado
import com.example.appprotocolos_eq014.ui.components.CampoTexto
import com.example.appprotocolos_eq014.ui.components.SelectorSegmentado
import com.example.appprotocolos_eq014.ui.theme.AppProtocolos_EQ014Theme
import com.example.appprotocolos_eq014.viewmodel.FichaUsuarioViewModel

@Composable
fun FichaUsuarioScreen(
    viewModel: FichaUsuarioViewModel,
    onVolver: () -> Unit,
    onGuardado: (esNuevo: Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.guardadoExitoso) {
        if (uiState.guardadoExitoso) {
            onGuardado(uiState.esNuevo)
        }
    }

    FichaUsuarioContent(
        uiState = uiState,
        onTipoCambia = viewModel::onTipoCambia,
        onRolCambia = viewModel::onRolCambia,
        onNombreCambia = viewModel::onNombreCambia,
        onRutCambia = viewModel::onRutCambia,
        onEmpresaCambia = viewModel::onEmpresaCambia,
        onCargoCambia = viewModel::onCargoCambia,
        onTelefonoCambia = viewModel::onTelefonoCambia,
        onCorreoCambia = viewModel::onCorreoCambia,
        onGuardar = viewModel::onGuardar,
        onVolver = onVolver
    )

    if (uiState.pedirContrasena) {
        DialogoContrasenaJefe(
            contrasena = uiState.contrasenaJefe,
            error = uiState.errorContrasenaJefe,
            guardando = uiState.guardando,
            onContrasenaCambia = viewModel::onContrasenaJefeCambia,
            onConfirmar = viewModel::onConfirmarContrasena,
            onCancelar = viewModel::onCancelarContrasena
        )
    }
}

@Composable
private fun DialogoContrasenaJefe(
    contrasena: String,
    error: String?,
    guardando: Boolean,
    onContrasenaCambia: (String) -> Unit,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!guardando) onCancelar() },
        title = { Text("Confirma con tu contraseña") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Cambiar el perfil de un usuario requiere la contraseña del jefe técnico.")
                OutlinedTextField(
                    value = contrasena,
                    onValueChange = onContrasenaCambia,
                    label = { Text("Tu contraseña") },
                    singleLine = true,
                    isError = error != null,
                    supportingText = if (error != null) ({ Text(error) }) else null,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirmar, enabled = !guardando) {
                Text(if (guardando) "Verificando..." else "Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar, enabled = !guardando) { Text("Cancelar") }
        }
    )
}

@Composable
fun FichaUsuarioContent(
    uiState: FichaUsuarioUiState,
    onTipoCambia: (Boolean) -> Unit,
    onRolCambia: (RolPersonal) -> Unit,
    onNombreCambia: (String) -> Unit,
    onRutCambia: (String) -> Unit,
    onEmpresaCambia: (String) -> Unit,
    onCargoCambia: (String) -> Unit,
    onTelefonoCambia: (String) -> Unit,
    onCorreoCambia: (String) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit
) {
    val textoPersonal = stringResource(R.string.tab_personal_induztek)
    val textoCliente = stringResource(R.string.perfil_usuario_cliente)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BarraSuperior(
            titulo = stringResource(
                if (uiState.esNuevo) R.string.titulo_agregar_usuario else R.string.titulo_ficha_usuario
            ),
            onVolver = onVolver
        )

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
            if (uiState.esNuevo || uiState.pendiente) {
                SelectorSegmentado(
                    etiqueta = stringResource(R.string.etiqueta_tipo_usuario),
                    opciones = listOf(false, true),
                    seleccionada = uiState.esCliente,
                    textoDe = { esCliente -> if (esCliente) textoCliente else textoPersonal },
                    onSeleccionar = onTipoCambia
                )
            }

            CampoTexto(
                valor = uiState.nombre,
                onValorCambia = onNombreCambia,
                etiqueta = stringResource(
                    if (uiState.esCliente) R.string.campo_nombre_contacto else R.string.campo_nombre_completo
                ),
                icono = R.drawable.ic_persona,
                error = uiState.errorNombre
            )

            CampoTexto(
                valor = uiState.rut,
                onValorCambia = onRutCambia,
                etiqueta = stringResource(R.string.campo_rut),
                icono = R.drawable.ic_persona,
                error = uiState.errorRut
            )

            AnimatedVisibility(
                visible = !uiState.esCliente,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (uiState.puedeEditarRol) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SelectorSegmentado(
                            etiqueta = stringResource(R.string.etiqueta_perfil_acceso),
                            opciones = RolPersonal.entries.toList(),
                            seleccionada = uiState.rolPersonal,
                            textoDe = { it.etiqueta },
                            onSeleccionar = onRolCambia
                        )
                        if (uiState.cambiaRol) {
                            Text(
                                text = "Al guardar se pedirá tu contraseña para confirmar el cambio de perfil.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
                } else {
                    CampoBloqueado(
                        etiqueta = stringResource(R.string.campo_perfil),
                        valor = uiState.rolNombre,
                        ayuda = stringResource(R.string.ayuda_perfil_fijo)
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.esCliente,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    CampoTexto(
                        valor = uiState.empresa,
                        onValorCambia = onEmpresaCambia,
                        etiqueta = stringResource(R.string.campo_empresa_obligatoria),
                        icono = R.drawable.ic_equipos,
                        error = uiState.errorEmpresa
                    )
                    CampoTexto(
                        valor = uiState.cargo,
                        onValorCambia = onCargoCambia,
                        etiqueta = stringResource(R.string.campo_cargo),
                        icono = R.drawable.ic_persona,
                        error = uiState.errorCargo
                    )
                }
            }

            CampoTexto(
                valor = uiState.telefono,
                onValorCambia = onTelefonoCambia,
                etiqueta = stringResource(R.string.campo_telefono),
                icono = R.drawable.ic_telefono,
                error = uiState.errorTelefono,
                tipoTeclado = KeyboardType.Phone
            )

            if (uiState.esNuevo) {
                CampoTexto(
                    valor = uiState.correo,
                    onValorCambia = onCorreoCambia,
                    etiqueta = stringResource(R.string.registro_correo_personal),
                    icono = R.drawable.ic_correo,
                    error = uiState.errorCorreo,
                    tipoTeclado = KeyboardType.Email
                )
                Text(
                    text = stringResource(R.string.ayuda_contrasena_inicial),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 12.dp)
                )
            } else {
                CampoBloqueado(
                    etiqueta = stringResource(R.string.campo_correo),
                    valor = uiState.correo,
                    ayuda = stringResource(R.string.ayuda_correo_cuenta)
                )
            }

            AnimatedVisibility(visible = uiState.errorGeneral != null) {
                Text(
                    text = uiState.errorGeneral.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            BotonPrincipal(
                texto = stringResource(
                    if (uiState.esNuevo) R.string.titulo_agregar_usuario else R.string.btn_guardar_cambios
                ),
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
private fun FichaUsuarioPreview() {
    AppProtocolos_EQ014Theme {
        FichaUsuarioContent(
            uiState = FichaUsuarioUiState(errorRut = "RUT inválido: el dígito verificador no coincide"),
            onTipoCambia = {},
            onRolCambia = {},
            onNombreCambia = {},
            onRutCambia = {},
            onEmpresaCambia = {},
            onCargoCambia = {},
            onTelefonoCambia = {},
            onCorreoCambia = {},
            onGuardar = {},
            onVolver = {}
        )
    }
}