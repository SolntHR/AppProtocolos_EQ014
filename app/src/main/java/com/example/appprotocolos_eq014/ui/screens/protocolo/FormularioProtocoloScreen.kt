package com.example.appprotocolos_eq014.ui.screens.protocolo

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.model.CampoMedicion
import com.example.appprotocolos_eq014.model.Comparacion
import com.example.appprotocolos_eq014.model.EstadoFila
import com.example.appprotocolos_eq014.model.Fechas
import com.example.appprotocolos_eq014.model.FilaPruebaUi
import com.example.appprotocolos_eq014.model.FormularioProtocoloUiState
import com.example.appprotocolos_eq014.model.InstrumentoOpcion
import com.example.appprotocolos_eq014.model.Parametros
import com.example.appprotocolos_eq014.model.ReglasProtocolo
import com.example.appprotocolos_eq014.model.ResultadoPrueba
import com.example.appprotocolos_eq014.model.Rol
import com.example.appprotocolos_eq014.model.SeccionPruebaUi
import com.example.appprotocolos_eq014.model.TipoPrueba
import com.example.appprotocolos_eq014.model.formatoCl
import com.example.appprotocolos_eq014.ui.components.BarraSuperior
import com.example.appprotocolos_eq014.ui.components.BotonPrincipal
import com.example.appprotocolos_eq014.ui.components.BotonSecundario
import com.example.appprotocolos_eq014.ui.components.EtiquetaEstadoProtocolo
import com.example.appprotocolos_eq014.ui.components.EtiquetaResultadoProtocolo
import com.example.appprotocolos_eq014.ui.components.MensajeAvisoProtocolo
import com.example.appprotocolos_eq014.ui.components.SelectorDesplegable
import com.example.appprotocolos_eq014.ui.components.TarjetaSeccion
import com.example.appprotocolos_eq014.ui.styles.EstilosCampo
import com.example.appprotocolos_eq014.ui.theme.AmarilloAviso
import com.example.appprotocolos_eq014.ui.theme.AmarilloSuave
import com.example.appprotocolos_eq014.ui.theme.RojoError
import com.example.appprotocolos_eq014.viewmodel.FormularioProtocoloViewModel
import com.example.appprotocolos_eq014.permisos.SolicitarPermisoNotificaciones

/**
 * Formulario de protocolo (RF-03 a RF-06, RF-11, RF-17).
 * - idVisita: visita preseleccionada (opcional)
 * - idProtocolo: si no es null se edita un Borrador u Observado propio (RN-17)
 */
@Composable
fun FormularioProtocoloScreen(
    viewModel: FormularioProtocoloViewModel,
    idVisita: Long?,
    idProtocolo: String?,
    onVolver: () -> Unit,
    onGuardado: (String) -> Unit,
    onRegistrarEquipo: (idInstalacion: Long) -> Unit = {},
    equipoCreado: Long? = null,
    onEquipoCreadoConsumido: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Notificaciones locales: se pide el permiso al entrar al formulario (Android 13+)
    SolicitarPermisoNotificaciones()

    LaunchedEffect(idVisita, idProtocolo) { viewModel.iniciar(idVisita, idProtocolo) }

    // Al volver del formulario de equipo, se recarga la lista y se selecciona el equipo nuevo
    LaunchedEffect(equipoCreado) {
        val id = equipoCreado ?: return@LaunchedEffect
        viewModel.onEquipoCreado(id)
        onEquipoCreadoConsumido()
    }

    LaunchedEffect(state.protocoloGuardadoId) {
        val id = state.protocoloGuardadoId ?: return@LaunchedEffect
        Toast.makeText(context, state.mensajeGuardado ?: "Guardado", Toast.LENGTH_SHORT).show()
        viewModel.onGuardadoConsumido()
        onGuardado(id)
    }

    val salir = {
        viewModel.descartar()
        onVolver()
    }
    BackHandler(onBack = salir)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BarraSuperior(
            titulo = if (state.esEdicion) "Editar ${state.numero.orEmpty()}" else "Nuevo protocolo",
            onVolver = salir
        )

        if (state.cargando) {
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
            if (!Rol.puedeCrearProtocolos(state.rolUsuario)) {
                MensajeAvisoProtocolo("Tu perfil es de solo consulta. No puedes registrar protocolos.")
            }
            state.error?.let { MensajeAvisoProtocolo(it) }
            state.estadoActual?.let { estado ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Estado actual: ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    EtiquetaEstadoProtocolo(estado)
                }
            }

            SeccionVisita(state, viewModel)
            SeccionEquipo(state, viewModel, onRegistrarEquipo)

            if (state.equipoSeleccionado != null) {
                SeccionAmbiente(state, viewModel)

                state.secciones.forEach { seccion ->
                    TarjetaPrueba(
                        seccion = seccion,
                        tipoEquipoId = state.equipoSeleccionado?.tipoEquipoId ?: 0,
                        instrumentos = state.instrumentos,
                        puedeResultadoManual = Rol.puedeFormalizar(state.rolUsuario),
                        viewModel = viewModel
                    )
                }

                TarjetaSeccion(titulo = "Observaciones") {
                    if (state.visitaSeleccionada?.incluyeMantenimiento == true) {
                        OutlinedTextField(
                            value = state.descripcionMantenimiento,
                            onValueChange = viewModel::onDescripcionMantenimiento,
                            label = { Text("Descripción del mantenimiento *") },
                            minLines = 2,
                            shape = EstilosCampo.forma,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    OutlinedTextField(
                        value = state.observaciones,
                        onValueChange = viewModel::onObservaciones,
                        label = { Text("Observaciones del equipo (opcional)") },
                        placeholder = { Text("Ej.: fuga de aceite leve en válvula inferior") },
                        minLines = 2,
                        shape = EstilosCampo.forma,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                ResumenResultado(state)
            }
        }

        // Botones fijos abajo
        Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BotonSecundario(
                    texto = "Guardar borrador",
                    onClick = { if (!state.guardando) viewModel.guardarBorrador() },
                    modifier = Modifier.weight(1f)
                )
                BotonPrincipal(
                    texto = if (state.guardando) "Guardando..." else "Registrar",
                    onClick = viewModel::registrarEnTerreno,
                    habilitado = !state.guardando,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ---------------------------------------------------------------
// 1. Visita
// ---------------------------------------------------------------
@Composable
private fun SeccionVisita(state: FormularioProtocoloUiState, viewModel: FormularioProtocoloViewModel) {
    TarjetaSeccion(titulo = "1. Datos de la visita") {
        SelectorDesplegable(
            etiqueta = "Orden de trabajo (OT)",
            opciones = state.visitas,
            seleccion = state.visitaSeleccionada,
            textoDe = { "${it.numeroOt} · ${it.clienteNombre} · ${Fechas.fecha(it.fecha)}" },
            onSeleccion = viewModel::seleccionarVisita,
            habilitado = !state.esEdicion,
            textoVacio = "Selecciona la visita"
        )
        if (state.visitas.isEmpty() && !state.esEdicion) {
            Text(
                "No tienes visitas abiertas asignadas.",
                color = RojoError,
                style = MaterialTheme.typography.bodySmall
            )
        }
        state.visitaSeleccionada?.let { v ->
            DatoLectura("Cliente", v.clienteNombre)
            DatoLectura("Instalación", v.instalacionNombre)
            DatoLectura("Ingeniero a cargo", v.ingenieroNombre)
        }
        DatoLectura("Fecha y hora de la prueba", Fechas.fechaHora(state.fechaHoraPrueba))
        DatoLectura("Técnico responsable", state.tecnicoNombre)
    }
}

// ---------------------------------------------------------------
// 2. Equipo
// ---------------------------------------------------------------
@Composable
private fun SeccionEquipo(
    state: FormularioProtocoloUiState,
    viewModel: FormularioProtocoloViewModel,
    onRegistrarEquipo: (Long) -> Unit
) {
    if (state.visitaSeleccionada == null) return
    TarjetaSeccion(titulo = "2. Equipo") {
        SelectorDesplegable(
            etiqueta = "Equipo (TAG)",
            opciones = state.equipos,
            seleccion = state.equipoSeleccionado,
            textoDe = { "${it.tag} · ${it.tipoEquipoNombre}" },
            onSeleccion = viewModel::seleccionarEquipo,
            habilitado = !state.esEdicion,
            textoVacio = "Selecciona el equipo"
        )
        if (state.equipos.isEmpty()) {
            Text(
                "Todos los equipos de esta instalación ya tienen protocolo en la visita.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        // HU-01: equipo que se prueba por primera vez
        if (!state.esEdicion) {
            BotonSecundario(
                texto = "Registrar equipo nuevo",
                onClick = { state.visitaSeleccionada?.let { onRegistrarEquipo(it.instalacionId) } },
                icono = R.drawable.ic_mas,
                modifier = Modifier.fillMaxWidth()
            )
        }
        state.equipoSeleccionado?.let { e ->
            DatoLectura("Marca / Serie", "${e.marca ?: "-"} · ${e.numeroSerie ?: "-"}")
            val placa = listOfNotNull(
                e.potenciaKva?.let { "${it.formatoCl(0)} kVA" },
                e.tensiones,
                e.corriente?.let { "${it.formatoCl(0)} A" },
                e.maxContactosUohm?.let { "contactos máx. ${it.formatoCl(0)} µΩ" }
            ).joinToString(" · ")
            if (placa.isNotBlank()) DatoLectura("Datos de placa", placa)
        }
    }
}

// ---------------------------------------------------------------
// 3. Condiciones ambientales (HU-02)
// ---------------------------------------------------------------
@Composable
private fun SeccionAmbiente(state: FormularioProtocoloUiState, viewModel: FormularioProtocoloViewModel) {
    TarjetaSeccion(titulo = "3. Condiciones ambientales") {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CampoNumero(
                valor = state.temperatura,
                onCambio = viewModel::onTemperaturaCambia,
                etiqueta = "Temperatura *",
                unidad = "°C",
                error = state.errorTemperatura,
                permitirNegativo = true,
                modifier = Modifier.weight(1f)
            )
            CampoNumero(
                valor = state.humedad,
                onCambio = viewModel::onHumedadCambia,
                etiqueta = "Humedad *",
                unidad = "%",
                error = state.errorHumedad,
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            "* Obligatorios para pasar a \"Registrado en terreno\".",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ---------------------------------------------------------------
// 4. Pruebas (una tarjeta por tipo de prueba segun RN-05)
// ---------------------------------------------------------------
@Composable
private fun TarjetaPrueba(
    seccion: SeccionPruebaUi,
    tipoEquipoId: Long,
    instrumentos: List<InstrumentoOpcion>,
    puedeResultadoManual: Boolean,
    viewModel: FormularioProtocoloViewModel
) {
    val tipo = seccion.tipo
    TarjetaSeccion(titulo = tipo.nombre) {
        if (seccion.filasCompletas.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Resultado de la prueba: ", style = MaterialTheme.typography.bodyMedium)
                EtiquetaResultadoProtocolo(seccion.resultado)
            }
        }

        // Aclaracion 2: cada prueba queda realizada o No realizada con motivo
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Prueba realizada", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Switch(checked = !seccion.noRealizada, onCheckedChange = { viewModel.onNoRealizada(tipo, !it) })
        }
        if (seccion.noRealizada) {
            OutlinedTextField(
                value = seccion.motivoNoRealizada,
                onValueChange = { viewModel.onMotivoNoRealizada(tipo, it) },
                label = { Text("Motivo por el que no se realizó *") },
                placeholder = { Text("Ej.: cliente no autorizó desconexión") },
                isError = seccion.motivoNoRealizada.isBlank(),
                supportingText = if (seccion.motivoNoRealizada.isBlank()) ({ Text("El motivo es obligatorio") }) else null,
                minLines = 2,
                shape = EstilosCampo.forma,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (!seccion.noRealizada) {
        // RF-05 / RN-11: instrumento con su vencimiento
        val sugeridos = instrumentos.filter {
            it.nombreTipo.lowercase().contains(ReglasProtocolo.palabraInstrumento(tipo))
        }.ifEmpty { instrumentos }
        val hoy = Fechas.hoy()
        SelectorDesplegable(
            etiqueta = "Instrumento",
            opciones = sugeridos,
            seleccion = instrumentos.firstOrNull { it.id == seccion.instrumentoId },
            textoDe = {
                "${it.descripcion} · vence ${Fechas.fecha(it.fechaVencimiento)}" +
                        if (!it.vigenteEn(hoy)) " (VENCIDO)" else ""
            },
            onSeleccion = { viewModel.onInstrumento(tipo, it.id) },
            error = seccion.errorInstrumento
        )

        // Datos comunes de la prueba
        when (tipo) {
            TipoPrueba.AISLACION -> {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CampoNumero(
                        valor = seccion.comunes[Parametros.KV.codigo].orEmpty(),
                        onCambio = { viewModel.onComun(tipo, Parametros.KV.codigo, it) },
                        etiqueta = "Tensión de prueba",
                        unidad = "kV",
                        modifier = Modifier.weight(1f)
                    )
                    listOf("MΩ", "GΩ").forEach { u ->
                        FilterChip(
                            selected = seccion.unidadAislacion == u,
                            onClick = { viewModel.onUnidadAislacion(tipo, u) },
                            label = { Text(u, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
            TipoPrueba.TTR -> CampoNumero(
                valor = seccion.comunes[Parametros.TAP.codigo].orEmpty(),
                onCambio = { viewModel.onComun(tipo, Parametros.TAP.codigo, it) },
                etiqueta = "Posición del cambiador (tap)",
                unidad = "",
                modifier = Modifier.fillMaxWidth()
            )
            TipoPrueba.CONTACTOS -> CampoNumero(
                valor = seccion.comunes[Parametros.MAX_PLACA.codigo].orEmpty(),
                onCambio = { viewModel.onComun(tipo, Parametros.MAX_PLACA.codigo, it) },
                etiqueta = "Máximo de placa",
                unidad = "µΩ",
                modifier = Modifier.fillMaxWidth()
            )
            else -> Unit
        }

        seccion.filas.forEachIndexed { indice, fila ->
            HorizontalDivider()
            FilaPrueba(
                tipo = tipo,
                tipoEquipoId = tipoEquipoId,
                fila = fila,
                unidadAislacion = seccion.unidadAislacion,
                puedeResultadoManual = puedeResultadoManual,
                onValor = { codigo, valor -> viewModel.onValorFila(tipo, indice, codigo, valor) },
                onOpera = { viewModel.onOperaFila(tipo, indice, it) },
                onResultadoManual = { viewModel.onResultadoManual(tipo, indice, it) }
            )
        }
        }

        OutlinedTextField(
            value = seccion.comentario,
            onValueChange = { viewModel.onComentarioSeccion(tipo, it) },
            label = { Text("Comentario de la prueba (opcional)") },
            shape = EstilosCampo.forma,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FilaPrueba(
    tipo: TipoPrueba,
    tipoEquipoId: Long,
    fila: FilaPruebaUi,
    unidadAislacion: String,
    puedeResultadoManual: Boolean,
    onValor: (String, String) -> Unit,
    onOpera: (Boolean) -> Unit,
    onResultadoManual: (ResultadoPrueba?) -> Unit
) {
    val ev = fila.evaluacion
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                fila.elemento,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            if (ev.estado == EstadoFila.COMPLETA) EtiquetaResultadoProtocolo(ev.resultado)
        }

        if (tipo == TipoPrueba.RELE) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("¿El relé operó?", modifier = Modifier.weight(1f))
                Switch(checked = fila.opera, onCheckedChange = onOpera)
            }
        }

        val campos: List<CampoMedicion> = ReglasProtocolo.camposFila(tipo, tipoEquipoId)
        campos.chunked(2).forEach { par ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                par.forEach { campo ->
                    val unidad = if (tipo == TipoPrueba.AISLACION) unidadAislacion else campo.unidad
                    CampoNumero(
                        valor = fila.valores[campo.codigo].orEmpty(),
                        onCambio = { onValor(campo.codigo, it) },
                        etiqueta = campo.etiqueta,
                        unidad = if (unidad == "-") "" else unidad,
                        // RNF-05: se marca en rojo el campo exacto que tiene el problema
                        marcarError = campo.codigo in ev.camposConError,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (par.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        // RN-08c: resultado manual (solo ingeniero / jefe tecnico)
        if (tipo == TipoPrueba.DEVANADOS) {
            Text(
                if (puedeResultadoManual) "Resultado (lo indica el ingeniero):" else "El resultado lo indica el ingeniero.",
                style = MaterialTheme.typography.bodySmall
            )
            if (puedeResultadoManual) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ResultadoPrueba.entries.forEach { r ->
                        FilterChip(
                            selected = fila.resultadoManual == r,
                            onClick = { onResultadoManual(if (fila.resultadoManual == r) null else r) },
                            label = { Text(r.etiqueta) }
                        )
                    }
                }
            }
        }

        ev.error?.let {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(R.drawable.ic_alerta), contentDescription = null,
                    tint = RojoError, modifier = Modifier.size(16.dp)
                )
                Text(it, color = RojoError, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 6.dp))
            }
        }
        if (ev.estado == EstadoFila.COMPLETA && ev.error == null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(R.drawable.ic_check), contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)
                )
                Text("Medición completa", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 6.dp))
            }
        }
        ev.detalle?.let { Text(it, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold) }
        ev.comparaciones.forEach { LineaComparacion(it) }
    }
}

// RN-09 / HU-06: valor anterior y variacion
@Composable
fun LineaComparacion(c: Comparacion) {
    val signo = if (c.variacionPct > 0) "+" else ""
    val texto = "${c.nombre}: anterior ${c.valorAnterior.formatoCl(2)} ${c.unidad} (${c.numeroAnterior ?: "-"}) → " +
            "$signo${c.variacionPct.formatoCl(1)} %"
    if (c.significativa) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(AmarilloSuave)
                .padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(R.drawable.ic_alerta), contentDescription = null,
                    tint = AmarilloAviso, modifier = Modifier.size(18.dp)
                )
                Text(
                    "$texto · Variación significativa",
                    color = AmarilloAviso,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    } else {
        Text(texto, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun ResumenResultado(state: FormularioProtocoloUiState) {
    TarjetaSeccion(titulo = "Resultado global (RN-10)") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Peor resultado de las pruebas: ", modifier = Modifier.weight(1f))
            EtiquetaResultadoProtocolo(state.resultadoGlobal)
        }
        if (state.hayVariacionSignificativa) {
            MensajeAvisoProtocolo(
                "Hay variaciones significativas respecto a la prueba anterior. El ingeniero deberá comentar antes de formalizar.",
                esError = false
            )
        }
        val pruebasCompletas = state.secciones.sumOf { it.filasCompletas.size }
        Text(
            "$pruebasCompletas mediciones completas. Se guardan en el dispositivo aunque no haya señal.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ---------------------------------------------------------------
// Componentes pequeños
// ---------------------------------------------------------------
@Composable
private fun DatoLectura(etiqueta: String, valor: String) {
    Column {
        Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
        Text(valor.ifBlank { "-" }, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
    }
}

// Campo numerico grande con su unidad visible (RNF-04 / RNF-05)
@Composable
fun CampoNumero(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    unidad: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    marcarError: Boolean = false,
    // Se deja escribir el signo para que la regla muestre el motivo del rechazo (HU-03)
    permitirNegativo: Boolean = true
) {
    OutlinedTextField(
        value = valor,
        onValueChange = { nuevo ->
            // Solo digitos, coma/punto y (si corresponde) signo menos
            val filtrado = nuevo.filter { it.isDigit() || it == ',' || it == '.' || (permitirNegativo && it == '-') }
            onCambio(filtrado)
        },
        label = { Text(etiqueta) },
        suffix = if (unidad.isNotBlank()) ({ Text(unidad, fontWeight = FontWeight.Bold) }) else null,
        isError = error != null || marcarError,
        supportingText = if (error != null) ({ Text(error) }) else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = EstilosCampo.forma,
        colors = EstilosCampo.colores(),
        modifier = modifier
    )
}
