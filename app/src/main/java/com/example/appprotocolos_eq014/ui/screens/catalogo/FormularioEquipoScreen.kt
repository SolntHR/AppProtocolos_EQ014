package com.example.appprotocolos_eq014.ui.screens.catalogo

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.example.appprotocolos_eq014.viewmodel.FormularioEquipoViewModel

@Composable
fun FormularioEquipoScreen(
    viewModel: FormularioEquipoViewModel,
    onVolver: () -> Unit,
    onGuardado: (idEquipo: Long) -> Unit,
    onNuevaInstalacion: () -> Unit,
    instalacionCreada: Long? = null,
    onInstalacionCreadaConsumida: () -> Unit = {}
) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(s.equipoGuardadoId) {
        val id = s.equipoGuardadoId ?: return@LaunchedEffect
        Toast.makeText(context, "Equipo listo para usar", Toast.LENGTH_SHORT).show()
        onGuardado(id)
    }
    LaunchedEffect(instalacionCreada) {
        val id = instalacionCreada ?: return@LaunchedEffect
        viewModel.onInstalacionCreada(id)
        onInstalacionCreadaConsumida()
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        BarraSuperior(titulo = "Registrar equipo", onVolver = onVolver)
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

            AnimatedVisibility(visible = s.equipoExistente != null) {
                s.equipoExistente?.let { existente ->
                    TarjetaSeccion(titulo = "El equipo ${existente.tag} ya existe") {
                        Text(existente.descripcion, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "El TAG es único dentro de la instalación (RN-01).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        BotonPrincipal(
                            texto = "Usar el equipo registrado",
                            onClick = viewModel::usarEquipoExistente,
                            icono = R.drawable.ic_check,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            TarjetaSeccion(titulo = "1. Ubicación y tipo") {
                SelectorDesplegable(
                    etiqueta = "Instalación *",
                    opciones = s.instalaciones,
                    seleccion = s.instalacion,
                    textoDe = { it.nombre },
                    onSeleccion = viewModel::onInstalacion,
                    habilitado = !s.instalacionFija,
                    error = s.errores[V.CAMPO_INSTALACION],
                    textoVacio = "Selecciona la instalación"
                )
                if (!s.instalacionFija) {
                    TextButton(onClick = onNuevaInstalacion) {
                        Text("+ Registrar instalación nueva", fontWeight = FontWeight.Bold)
                    }
                }
                SelectorDesplegable(
                    etiqueta = "Tipo de equipo *",
                    opciones = s.tipos,
                    seleccion = s.tipo,
                    textoDe = { it.nombre },
                    onSeleccion = viewModel::onTipo,
                    error = s.errores[V.CAMPO_TIPO],
                    textoVacio = "Selecciona el tipo"
                )
                CampoTexto(
                    valor = s.tag, onValorCambia = viewModel::onTag,
                    etiqueta = "TAG del cliente * (ej.: TR-01)", icono = R.drawable.ic_equipos,
                    error = s.errores[V.CAMPO_TAG]
                )
            }

            TarjetaSeccion(titulo = "2. Datos de placa") {
                CampoTexto(valor = s.marca, onValorCambia = viewModel::onMarca, etiqueta = "Marca", icono = R.drawable.ic_lapiz)
                CampoTexto(valor = s.modelo, onValorCambia = viewModel::onModelo, etiqueta = "Modelo", icono = R.drawable.ic_lapiz)
                CampoTexto(valor = s.numeroSerie, onValorCambia = viewModel::onNumeroSerie, etiqueta = "Número de serie", icono = R.drawable.ic_buscar)
                CampoTexto(
                    valor = s.anio, onValorCambia = viewModel::onAnio, etiqueta = "Año de fabricación",
                    icono = R.drawable.ic_check, error = s.errores[V.CAMPO_ANIO], tipoTeclado = KeyboardType.Number
                )
                if (s.pidePotencia) {
                    CampoTexto(
                        valor = s.potenciaKva, onValorCambia = viewModel::onPotencia, etiqueta = "Potencia (kVA) *",
                        icono = R.drawable.ic_rayo, error = s.errores[V.CAMPO_POTENCIA], tipoTeclado = KeyboardType.Decimal
                    )
                }
                CampoTexto(
                    valor = s.tensiones, onValorCambia = viewModel::onTensiones,
                    etiqueta = if (s.pidePotencia) "Tensiones * (ej.: 12 / 0,4 kV)" else "Tensión nominal (ej.: 12 kV)",
                    icono = R.drawable.ic_rayo, error = s.errores[V.CAMPO_TENSIONES]
                )
                if (s.pideCorriente) {
                    CampoTexto(
                        valor = s.corriente, onValorCambia = viewModel::onCorriente,
                        etiqueta = "Corriente nominal / ajuste (A)",
                        icono = R.drawable.ic_rayo, error = s.errores[V.CAMPO_CORRIENTE], tipoTeclado = KeyboardType.Decimal
                    )
                }
                if (s.pideContactos) {
                    CampoTexto(
                        valor = s.maxContactos, onValorCambia = viewModel::onMaxContactos,
                        etiqueta = "Máximo resistencia de contactos (µΩ) *",
                        icono = R.drawable.ic_rayo, error = s.errores[V.CAMPO_CONTACTOS], tipoTeclado = KeyboardType.Decimal
                    )
                }
                CampoTexto(
                    valor = s.observaciones, onValorCambia = viewModel::onObservaciones,
                    etiqueta = "Observaciones", icono = R.drawable.ic_lapiz
                )
            }
            Text("* Campos obligatorios", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BotonSecundario(texto = "Cancelar", onClick = onVolver, modifier = Modifier.weight(1f))
            BotonPrincipal(
                texto = if (s.guardando) "Guardando..." else "Guardar equipo",
                onClick = viewModel::guardar,
                habilitado = !s.guardando,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
