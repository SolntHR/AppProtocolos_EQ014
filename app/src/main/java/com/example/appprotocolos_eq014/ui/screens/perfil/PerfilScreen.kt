package com.example.appprotocolos_eq014.ui.screens.perfil

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.model.DatoPerfil
import com.example.appprotocolos_eq014.model.PerfilUiState
import com.example.appprotocolos_eq014.ui.components.AvatarIniciales
import com.example.appprotocolos_eq014.ui.components.BotonPrincipal
import com.example.appprotocolos_eq014.ui.components.BotonSecundario
import com.example.appprotocolos_eq014.ui.components.TarjetaSeccion
import com.example.appprotocolos_eq014.ui.theme.AppProtocolos_EQ014Theme
import com.example.appprotocolos_eq014.ui.theme.AzulOscuro
import com.example.appprotocolos_eq014.ui.theme.AzulPrimario
import com.example.appprotocolos_eq014.ui.theme.AzulTextoCabecera
import com.example.appprotocolos_eq014.ui.theme.Blanco
import com.example.appprotocolos_eq014.ui.theme.NaranjaAcento
import com.example.appprotocolos_eq014.viewmodel.PerfilViewModel

@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel,
    onSesionCerrada: () -> Unit,
    onEditarPerfil: () -> Unit,
    perfilActualizado: Boolean = false,
    onPerfilActualizadoConsumido: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var mostrarOpcionesFoto by remember { mutableStateOf(false) }

    val lanzadorCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { foto -> viewModel.onFotoCapturada(foto) }

    val lanzadorPermisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) lanzadorCamara.launch(null) else viewModel.onPermisoCamaraDenegado()
    }

    val lanzadorGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> viewModel.onFotoElegida(uri) }

    val tomarFoto = {
        val permisoConcedido = ContextCompat.checkSelfPermission(
            contexto, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (permisoConcedido) {
            lanzadorCamara.launch(null)
        } else {
            lanzadorPermisoCamara.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(uiState.sesionCerrada) {
        if (uiState.sesionCerrada) {
            onSesionCerrada()
        }
    }

    LaunchedEffect(perfilActualizado) {
        if (perfilActualizado) {
            viewModel.onPerfilActualizado()
            onPerfilActualizadoConsumido()
        }
    }

    val textoMensaje = uiState.mensaje?.let { stringResource(it) }
    LaunchedEffect(textoMensaje) {
        if (textoMensaje != null) {
            snackbarHostState.showSnackbar(textoMensaje)
            viewModel.onMensajeMostrado()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PerfilContent(
            uiState = uiState,
            onEditarPerfil = onEditarPerfil,
            onCerrarSesion = viewModel::onCerrarSesion,
            onCambiarFoto = { mostrarOpcionesFoto = true }
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }

    if (mostrarOpcionesFoto) {
        HojaOpcionesFoto(
            tieneFoto = uiState.foto != null,
            onTomarFoto = {
                mostrarOpcionesFoto = false
                tomarFoto()
            },
            onElegirGaleria = {
                mostrarOpcionesFoto = false
                lanzadorGaleria.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onEliminarFoto = {
                mostrarOpcionesFoto = false
                viewModel.onEliminarFoto()
            },
            onCerrar = { mostrarOpcionesFoto = false }
        )
    }

    uiState.fotoPendiente?.let { foto ->
        DialogoVistaPreviaFoto(
            foto = foto,
            guardando = uiState.procesandoFoto,
            onUsar = viewModel::onUsarFoto,
            onCancelar = viewModel::onCancelarFoto
        )
    }
}

@Composable
fun PerfilContent(
    uiState: PerfilUiState,
    onEditarPerfil: () -> Unit,
    onCerrarSesion: () -> Unit,
    onCambiarFoto: () -> Unit
) {
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val foto = remember(uiState.foto) { uiState.foto?.asImageBitmap() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        EncabezadoPerfil(
            nombre = uiState.nombre,
            rol = uiState.rol,
            correo = uiState.correo,
            foto = foto,
            onCambiarFoto = onCambiarFoto
        )

        val contenidoVisible = remember { MutableTransitionState(false).apply { targetState = true } }
        AnimatedVisibility(
            visibleState = contenidoVisible,
            enter = fadeIn() + slideInVertically { it / 10 }
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                uiState.error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                BotonPrincipal(
                    texto = stringResource(R.string.perfil_editar),
                    onClick = onEditarPerfil,
                    icono = R.drawable.ic_lapiz,
                    modifier = Modifier.fillMaxWidth()
                )

                BotonSecundario(
                    texto = stringResource(R.string.menu_cerrar_sesion),
                    onClick = onCerrarSesion,
                    icono = R.drawable.ic_salir,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(4.dp))

                TarjetaSeccion(
                    titulo = stringResource(
                        if (uiState.esCliente) R.string.perfil_usuario_cliente else R.string.tab_personal_induztek
                    )
                ) {
                    uiState.datos.forEachIndexed { indice, dato ->
                        FilaDato(dato)
                        if (indice < uiState.datos.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EncabezadoPerfil(
    nombre: String,
    rol: String,
    correo: String,
    foto: ImageBitmap?,
    onCambiarFoto: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(Brush.linearGradient(listOf(AzulPrimario, AzulOscuro)))
            .padding(start = 18.dp, end = 18.dp, top = 26.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.clickable(onClick = onCambiarFoto)
        ) {
            AvatarIniciales(
                nombre = nombre,
                tamano = 96.dp,
                foto = foto,
                modifier = Modifier.border(3.dp, Blanco.copy(alpha = 0.85f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 2.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Blanco)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(NaranjaAcento),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_camara),
                    contentDescription = stringResource(R.string.cd_cambiar_foto),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Text(
            text = nombre,
            color = Blanco,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp)
        )

        Text(
            text = rol,
            color = Blanco,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Blanco.copy(alpha = 0.18f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )

        Text(
            text = correo,
            color = AzulTextoCabecera,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun FilaDato(dato: DatoPerfil) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = dato.etiqueta,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
        Text(
            text = dato.valor,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PerfilScreenPreview() {
    AppProtocolos_EQ014Theme {
        PerfilContent(
            uiState = PerfilUiState(
                isLoading = false,
                nombre = "Matías Godoy Herrera",
                correo = "mgodoy@induztek.ejemplo",
                rol = "Técnico de terreno",
                datos = listOf(
                    DatoPerfil("Nombre", "Matías Godoy Herrera"),
                    DatoPerfil("RUT", "17.284.519-3"),
                    DatoPerfil("Perfil", "Técnico de terreno"),
                    DatoPerfil("Correo", "mgodoy@induztek.ejemplo"),
                    DatoPerfil("Teléfono", "+56900000101")
                )
            ),
            onEditarPerfil = {},
            onCerrarSesion = {},
            onCambiarFoto = {}
        )
    }
}
