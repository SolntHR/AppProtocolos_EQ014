package com.example.appprotocolos_eq014.ui.screens.usuarios

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.model.GestionUsuariosUiState
import com.example.appprotocolos_eq014.model.PestanaUsuarios
import com.example.appprotocolos_eq014.model.UsuarioFila
import com.example.appprotocolos_eq014.ui.components.AvatarIniciales
import com.example.appprotocolos_eq014.ui.theme.AppProtocolos_EQ014Theme
import com.example.appprotocolos_eq014.viewmodel.GestionUsuariosViewModel

private val TextoFab = Color(0xFF4C2900)

@Composable
fun GestionUsuariosScreen(
    viewModel: GestionUsuariosViewModel,
    onAgregarUsuario: () -> Unit,
    onAbrirUsuario: (Long) -> Unit,
    resultadoGuardado: Boolean?,
    onResultadoConsumido: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(resultadoGuardado) {
        if (resultadoGuardado != null) {
            viewModel.onUsuarioGuardado(esNuevo = resultadoGuardado)
            onResultadoConsumido()
        }
    }

    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMensajeMostrado()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GestionUsuariosContent(
            uiState = uiState,
            onPestanaCambia = viewModel::onPestanaCambia,
            onCambiarActivo = viewModel::onCambiarActivo,
            onAbrirUsuario = onAbrirUsuario
        )

        ExtendedFloatingActionButton(
            onClick = onAgregarUsuario,
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = TextoFab,
            shape = RoundedCornerShape(18.dp),
            icon = { Icon(painterResource(R.drawable.ic_mas), contentDescription = null) },
            text = { Text(stringResource(R.string.titulo_agregar_usuario), fontWeight = FontWeight.ExtraBold) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 88.dp, start = 16.dp, end = 16.dp)
        )
    }
}

@Composable
fun GestionUsuariosContent(
    uiState: GestionUsuariosUiState,
    onPestanaCambia: (PestanaUsuarios) -> Unit,
    onCambiarActivo: (UsuarioFila) -> Unit,
    onAbrirUsuario: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 10.dp)) {
                Text(
                    text = stringResource(R.string.titulo_gestion_usuarios),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.usuarios_resumen, uiState.activos, uiState.inactivos),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            PestanasUsuarios(
                pestana = uiState.pestana,
                onPestanaCambia = onPestanaCambia
            )
            Spacer(Modifier.height(4.dp))
        }

        if (uiState.isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if (uiState.usuariosVisibles.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.usuarios_sin_registros),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        } else {
            items(uiState.usuariosVisibles, key = { it.id }) { fila ->
                FilaUsuario(
                    fila = fila,
                    onAbrir = { onAbrirUsuario(fila.id) },
                    onCambiarActivo = { onCambiarActivo(fila) },
                    modifier = Modifier.animateItem()
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.usuarios_nota_inactivo),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp)
            )
        }
    }
}

@Composable
private fun PestanasUsuarios(
    pestana: PestanaUsuarios,
    onPestanaCambia: (PestanaUsuarios) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf(
            PestanaUsuarios.PERSONAL to stringResource(R.string.tab_personal_induztek),
            PestanaUsuarios.CLIENTES to stringResource(R.string.tab_usuarios_cliente)
        ).forEach { (opcion, texto) ->
            val activa = opcion == pestana
            Surface(
                onClick = { onPestanaCambia(opcion) },
                shape = RoundedCornerShape(11.dp),
                color = if (activa) MaterialTheme.colorScheme.surface else Color.Transparent,
                shadowElevation = if (activa) 1.dp else 0.dp,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = texto,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activa) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun FilaUsuario(
    fila: UsuarioFila,
    onAbrir: () -> Unit,
    onCambiarActivo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val opacidad by animateFloatAsState(if (fila.activo) 1f else 0.6f, label = "opacidad_usuario")
    val foto = remember(fila.foto) { fila.foto?.asImageBitmap() }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .alpha(opacidad)
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier.padding(start = 6.dp, end = 10.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onAbrir)
                    .heightIn(min = 64.dp)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarIniciales(
                    nombre = fila.nombre,
                    tamano = 44.dp,
                    colorFondo = MaterialTheme.colorScheme.primary,
                    colorTexto = MaterialTheme.colorScheme.onPrimary,
                    foto = foto
                )
                Column(
                    modifier = Modifier.padding(start = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = fila.nombre,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = fila.detalle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!fila.activo) {
                        Text(
                            text = stringResource(R.string.usuarios_nota_fila),
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Switch(
                    checked = fila.activo,
                    onCheckedChange = { onCambiarActivo() },
                    enabled = !fila.esUsuarioActual,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                Text(
                    text = stringResource(if (fila.activo) R.string.estado_activo else R.string.estado_inactivo),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun GestionUsuariosPreview() {
    AppProtocolos_EQ014Theme {
        GestionUsuariosContent(
            uiState = GestionUsuariosUiState(
                isLoading = false,
                personal = listOf(
                    UsuarioFila(7, "Ricardo Espinoza Lagos", "Jefe técnico", activo = true, esUsuarioActual = true),
                    UsuarioFila(1, "Matías Godoy Herrera", "Técnico de terreno", activo = true, esUsuarioActual = false),
                    UsuarioFila(3, "Carlos Rondón Pérez", "Técnico de terreno", activo = false, esUsuarioActual = false)
                ),
                activos = 10,
                inactivos = 1
            ),
            onPestanaCambia = {},
            onCambiarActivo = {},
            onAbrirUsuario = {}
        )
    }
}
