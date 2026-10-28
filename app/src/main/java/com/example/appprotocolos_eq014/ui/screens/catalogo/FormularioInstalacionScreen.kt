package com.example.appprotocolos_eq014.ui.screens.catalogo

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.ui.components.BarraSuperior
import com.example.appprotocolos_eq014.ui.components.BotonPrincipal
import com.example.appprotocolos_eq014.ui.components.BotonSecundario
import com.example.appprotocolos_eq014.ui.components.CampoTexto
import com.example.appprotocolos_eq014.ui.components.MensajeAvisoProtocolo
import com.example.appprotocolos_eq014.ui.components.SelectorDesplegable
import com.example.appprotocolos_eq014.ui.components.TarjetaSeccion
import com.example.appprotocolos_eq014.util.ValidacionesCatalogo as V
import com.example.appprotocolos_eq014.viewmodel.FormularioInstalacionViewModel

@Composable
fun FormularioInstalacionScreen(
    viewModel: FormularioInstalacionViewModel,
    onVolver: () -> Unit,
    onGuardado: (idInstalacion: Long) -> Unit
) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(s.instalacionGuardadaId) {
        val id = s.instalacionGuardadaId ?: return@LaunchedEffect
        Toast.makeText(context, "Instalación registrada", Toast.LENGTH_SHORT).show()
        onGuardado(id)
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        BarraSuperior(titulo = "Registrar instalación", onVolver = onVolver)
        if (s.cargando) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            s.error?.let { MensajeAvisoProtocolo(it) }
            TarjetaSeccion(titulo = "Datos de la instalación") {
                SelectorDesplegable(
                    etiqueta = "Cliente *",
                    opciones = s.clientes,
                    seleccion = s.cliente,
                    textoDe = { it.nombre },
                    onSeleccion = viewModel::onCliente,
                    error = s.errores[V.CAMPO_CLIENTE],
                    textoVacio = "Selecciona el cliente"
                )
                Text(
                    "Las empresas cliente se registran en Gestión de usuarios (Jefe técnico).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CampoTexto(
                    valor = s.nombre, onValorCambia = viewModel::onNombre,
                    etiqueta = "Nombre * (ej.: Planta Lampa)", icono = R.drawable.ic_equipos,
                    error = s.errores[V.CAMPO_NOMBRE]
                )
                CampoTexto(
                    valor = s.ubicacion, onValorCambia = viewModel::onUbicacion,
                    etiqueta = "Ubicación (ej.: Sector industrial)", icono = R.drawable.ic_ubicacion
                )
                SelectorDesplegable(
                    etiqueta = "Comuna *",
                    opciones = s.comunas,
                    seleccion = s.comuna,
                    textoDe = { it.nombre },
                    onSeleccion = viewModel::onComuna,
                    error = s.errores[V.CAMPO_COMUNA],
                    textoVacio = "Selecciona la comuna"
                )
                CampoTexto(
                    valor = s.observacionesAcceso, onValorCambia = viewModel::onObservacionesAcceso,
                    etiqueta = "Observaciones de acceso (ej.: sin señal)", icono = R.drawable.ic_lapiz
                )
            }
            Text("* Campos obligatorios", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BotonSecundario(texto = "Cancelar", onClick = onVolver, modifier = Modifier.weight(1f))
            BotonPrincipal(
                texto = if (s.guardando) "Guardando..." else "Guardar instalación",
                onClick = viewModel::guardar,
                habilitado = !s.guardando,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
