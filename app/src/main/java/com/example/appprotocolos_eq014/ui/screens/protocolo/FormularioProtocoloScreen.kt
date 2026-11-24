package com.example.appprotocolos_eq014.ui.screens.protocolo

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.model.catalogos.ResultadoPrueba
import com.example.appprotocolos_eq014.model.catalogos.Rol
import com.example.appprotocolos_eq014.model.entidades.Instrumento
import com.example.appprotocolos_eq014.model.reglas.CampoMedicion
import com.example.appprotocolos_eq014.model.reglas.Comparacion
import com.example.appprotocolos_eq014.model.reglas.EstadoFila
import com.example.appprotocolos_eq014.model.reglas.Parametros
import com.example.appprotocolos_eq014.model.reglas.ReglasProtocolo
import com.example.appprotocolos_eq014.model.reglas.TipoPrueba
import com.example.appprotocolos_eq014.model.uistate.FilaPruebaUi
import com.example.appprotocolos_eq014.model.uistate.FormularioProtocoloUiState
import com.example.appprotocolos_eq014.model.uistate.SeccionPruebaUi
import com.example.appprotocolos_eq014.permisos.Permisos
import com.example.appprotocolos_eq014.permisos.SolicitarPermisoNotificaciones
import com.example.appprotocolos_eq014.ui.components.BarraSuperior
import com.example.appprotocolos_eq014.ui.components.BotonPrincipal
import com.example.appprotocolos_eq014.ui.components.BotonSecundario
import com.example.appprotocolos_eq014.ui.components.EtiquetaEstado
import com.example.appprotocolos_eq014.ui.components.EtiquetaEstadoProtocolo
import com.example.appprotocolos_eq014.ui.components.EtiquetaResultadoProtocolo
import com.example.appprotocolos_eq014.ui.components.MensajeAvisoProtocolo
import com.example.appprotocolos_eq014.ui.components.SelectorDesplegable
import com.example.appprotocolos_eq014.ui.components.TarjetaSeccion
import com.example.appprotocolos_eq014.ui.components.TonoEstado
import com.example.appprotocolos_eq014.ui.styles.EstilosCampo
import com.example.appprotocolos_eq014.ui.theme.AmarilloAviso
import com.example.appprotocolos_eq014.ui.theme.AmarilloSuave
import com.example.appprotocolos_eq014.ui.theme.AzulPrimario
import com.example.appprotocolos_eq014.ui.theme.AzulSuave
import com.example.appprotocolos_eq014.ui.theme.RojoError
import com.example.appprotocolos_eq014.ui.theme.RojoSuave
import com.example.appprotocolos_eq014.util.Fechas
import com.example.appprotocolos_eq014.util.ValidacionesProtocolo
import com.example.appprotocolos_eq014.util.formatoCl
import com.example.appprotocolos_eq014.viewmodel.FormularioProtocoloViewModel

@Composable
fun FormularioProtocoloScreen(
    viewModel: FormularioProtocoloViewModel,
    idVisita: Long?,
    idProtocolo: String?,
    onVolver: () -> Unit,
    onGuardado: (String) -> Unit,
    onRegistrarEquipo: (idInstalacion: Long) -> Unit = {},
    equipoCreado: Long? = null,
    onEquipoCreadoConsumido: () -> Unit = {},
    idEquipoInicial: Long? = null
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    SolicitarPermisoNotificaciones()

    LaunchedEffect(idVisita, idProtocolo, idEquipoInicial) { viewModel.iniciar(idVisita, idProtocolo, idEquipoInicial) }

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

    val lanzadorGaleria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        viewModel.onFotoElegida(uri)
    }
    val lanzadorCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { foto ->
        viewModel.onFotoCapturada(foto)
    }
    val lanzadorPermisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) lanzadorCamara.launch(null) else viewModel.onPermisoCamaraDenegado()
    }
    val tomarFoto = {
        if (Permisos.tiene(context, Manifest.permission.CAMERA)) lanzadorCamara.launch(null)
        else lanzadorPermisoCamara.launch(Manifest.permission.CAMERA)
    }
    val elegirFoto = {
        lanzadorGaleria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    val salir = {
        viewModel.descartar()
        onVolver()
    }
    BackHandler { if (state.paso > 0) viewModel.anterior() else salir() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BarraSuperior(
            titulo = if (state.esEdicion) "Editar ${state.numero.orEmpty()}" else "Nuevo protocolo",
            onVolver = salir,
            accion = if (state.cargando || state.esUltimoPaso) null else ({
                TextButton(onClick = viewModel::guardarBorrador, enabled = !state.guardando) { Text("Guardar borrador") }
            })
        )

        if (state.cargando) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }

        val faltantes = ValidacionesProtocolo.faltantes(state, registrar = true)
        IndicadorPasos(state, faltantes, onIrAPaso = viewModel::irAPaso)

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
            val faltanAqui = faltantes[state.paso].orEmpty()
            if (state.intentado && faltanAqui.isNotEmpty() && !state.esUltimoPaso) {
                MensajeAvisoProtocolo("Faltan ${textoDatos(faltanAqui.size)} en este paso. Están marcados en rojo.")
            }
            state.motivoObservacion?.let {
                MensajeAvisoProtocolo("Observado: $it. Corrige lo indicado y vuelve a registrar.", esError = false)
            }

            val seccion = state.seccionDelPaso(state.paso)
            when {
                state.paso == ValidacionesProtocolo.PASO_VISITA -> {
                    state.estadoActual?.let { estado ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Estado actual: ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            EtiquetaEstadoProtocolo(estado)
                        }
                    }
                    SeccionVisita(state, viewModel)
                    SeccionEquipo(state, viewModel, onRegistrarEquipo)
                }
                state.paso == ValidacionesProtocolo.PASO_CONDICIONES -> SeccionAmbiente(state, viewModel)
                seccion != null -> TarjetaPrueba(
                    seccion = seccion,
                    tipoEquipoId = state.equipoSeleccionado?.tipoEquipoId ?: 0,
                    instrumentos = state.instrumentos,
                    puedeResultadoManual = Rol.puedeFormalizar(state.rolUsuario),
                    intentado = state.intentado,
                    viewModel = viewModel
                )
                state.paso == state.pasoObservaciones -> {
                    TarjetaSeccion(titulo = "Observaciones") {
                        if (state.visitaSeleccionada?.incluyeMantenimiento == true) {
                            OutlinedTextField(
                                value = state.descripcionMantenimiento,
                                onValueChange = viewModel::onDescripcionMantenimiento,
                                label = { Text("Descripción del mantenimiento *") },
                                isError = state.errorMantenimiento != null,
                                supportingText = if (state.errorMantenimiento != null) ({ Text(state.errorMantenimiento.orEmpty()) }) else null,
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
                    SeccionFotos(state, viewModel, onTomarFoto = tomarFoto, onElegirFoto = elegirFoto)
                }
                else -> PasoResumen(state, faltantes, onIrAPaso = viewModel::irAPaso)
            }
        }

        Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (state.esUltimoPaso) {
                    BotonSecundario(
                        texto = "Guardar borrador",
                        onClick = viewModel::guardarBorrador,
                        modifier = Modifier.weight(1f)
                    )
                    BotonPrincipal(
                        texto = if (state.guardando) "Guardando..." else "Registrar",
                        onClick = viewModel::registrarEnTerreno,
                        habilitado = !state.guardando,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    BotonSecundario(
                        texto = if (state.paso == 0) "Cancelar" else "Atrás",
                        onClick = { if (state.paso == 0) salir() else viewModel.anterior() },
                        modifier = Modifier.weight(1f)
                    )
                    BotonPrincipal(
                        texto = if (state.paso == state.pasoObservaciones) "Ver resumen" else "Siguiente",
                        onClick = viewModel::siguiente,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private fun textoDatos(cantidad: Int) = if (cantidad == 1) "1 dato" else "$cantidad datos"

@Composable
private fun IndicadorPasos(
    state: FormularioProtocoloUiState,
    faltantes: Map<Int, List<String>>,
    onIrAPaso: (Int) -> Unit
) {
    val pasos = state.pasos
    val faltanAqui = faltantes[state.paso].orEmpty().size
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            pasos.indices.forEach { i ->
                val actual = i == state.paso
                val conError = state.intentado && faltantes[i].orEmpty().isNotEmpty()
                val listo = !conError && !actual && i < state.pasoResumen && faltantes[i].isNullOrEmpty()
                val (fondo, texto, borde) = when {
                    actual && conError -> Triple(RojoError, Color.White, RojoError)
                    actual -> Triple(AzulPrimario, Color.White, AzulPrimario)
                    conError -> Triple(RojoSuave, RojoError, RojoError)
                    listo -> Triple(AzulSuave, AzulPrimario, AzulSuave)
                    else -> Triple(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.outlineVariant)
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(fondo)
                        .border(1.5.dp, borde, CircleShape)
                        .clickable { onIrAPaso(i) }
                ) {
                    Text(
                        when {
                            conError -> "!"
                            listo -> "✓"
                            else -> "${i + 1}"
                        },
                        color = texto,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (i < pasos.lastIndex) {
                    Box(
                        Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(if (i < state.paso) AzulPrimario else MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Paso ${state.paso + 1} de ${pasos.size} · ${pasos[state.paso]}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            if (state.intentado && faltanAqui > 0 && !state.esUltimoPaso) {
                EtiquetaEstado("Faltan ${textoDatos(faltanAqui)}", TonoEstado.ERROR)
            }
        }
    }
}

@Composable
private fun PasoResumen(
    state: FormularioProtocoloUiState,
    faltantes: Map<Int, List<String>>,
    onIrAPaso: (Int) -> Unit
) {
    val total = faltantes.values.sumOf { it.size }
    if (state.intentado && total > 0) {
        val pasosConFaltas = faltantes.size
        MensajeAvisoProtocolo(
            "No se puede registrar todavía. Faltan ${textoDatos(total)} en $pasosConFaltas " +
                    (if (pasosConFaltas == 1) "paso" else "pasos") + ". Toca un paso para ir a completarlo."
        )
    } else if (state.intentado) {
        ValidacionesProtocolo.primerError(state, registrar = true)?.let { MensajeAvisoProtocolo(it.second) }
    }
    ResumenResultado(state)
    Text("Estado por paso", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    (0 until state.pasoResumen).forEach { i ->
        val faltan = faltantes[i].orEmpty()
        val conError = state.intentado && faltan.isNotEmpty()
        val seccion = state.seccionDelPaso(i)
        val (texto, tono) = when {
            faltan.isNotEmpty() -> (if (state.intentado) "Faltan ${faltan.size}" else "Pendiente") to
                    (if (state.intentado) TonoEstado.ERROR else TonoEstado.NEUTRO)
            seccion?.noRealizada == true -> "No realizada" to TonoEstado.NEUTRO
            i == state.pasoObservaciones -> "${state.totalFotos} ${if (state.totalFotos == 1) "foto" else "fotos"}" to TonoEstado.NEUTRO
            else -> "Completo" to TonoEstado.EXITO
        }
        Surface(
            onClick = { onIrAPaso(i) },
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(if (conError) 2.dp else 1.dp, if (conError) RojoError else MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(if (conError) RojoSuave else AzulSuave)
                ) {
                    Text("${i + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (conError) RojoError else AzulPrimario)
                }
                Column(Modifier.weight(1f)) {
                    Text(state.pasos[i], style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    if (conError) {
                        Text(faltan.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = RojoError)
                    }
                }
                EtiquetaEstado(texto, tono)
            }
        }
    }
}

@Composable
private fun SeccionFotos(
    state: FormularioProtocoloUiState,
    viewModel: FormularioProtocoloViewModel,
    onTomarFoto: () -> Unit,
    onElegirFoto: () -> Unit
) {
    TarjetaSeccion(titulo = "Fotos de evidencia (${state.totalFotos}/${FormularioProtocoloUiState.MAXIMO_FOTOS})") {
        if (state.totalFotos > 0) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.fotosGuardadas, key = { "g${it.id}" }) { foto ->
                    MiniaturaFoto(foto.url) { viewModel.onEliminarFotoGuardada(foto.id) }
                }
                itemsIndexed(state.fotosNuevas) { indice, foto ->
                    MiniaturaFoto(foto) { viewModel.onQuitarFotoNueva(indice) }
                }
            }
        }
        state.errorFotos?.let { Text(it, color = RojoError, style = MaterialTheme.typography.bodySmall) }
        if (state.totalFotos < FormularioProtocoloUiState.MAXIMO_FOTOS) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BotonSecundario("Cámara", onTomarFoto, Modifier.weight(1f), R.drawable.ic_camara)
                BotonSecundario("Galería", onElegirFoto, Modifier.weight(1f), R.drawable.ic_galeria)
            }
        }
        if (state.fotosNuevas.isNotEmpty()) {
            Text(
                "Las fotos nuevas se suben al guardar el protocolo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MiniaturaFoto(modelo: Any, onQuitar: () -> Unit) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        AsyncImage(
            model = modelo,
            contentDescription = "Foto de evidencia",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Surface(
            onClick = onQuitar,
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        ) {
            Text("✕", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = RojoError)
        }
    }
}

@Composable
private fun SeccionVisita(state: FormularioProtocoloUiState, viewModel: FormularioProtocoloViewModel) {
    TarjetaSeccion(titulo = "Datos de la visita") {
        SelectorDesplegable(
            etiqueta = "Orden de trabajo (OT)",
            opciones = state.visitas,
            seleccion = state.visitaSeleccionada,
            textoDe = { "${it.numeroOt} · ${it.clienteNombre} · ${Fechas.fecha(it.fecha)}" },
            onSeleccion = viewModel::seleccionarVisita,
            habilitado = !state.esEdicion,
            error = state.errorVisita,
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

@Composable
private fun SeccionEquipo(
    state: FormularioProtocoloUiState,
    viewModel: FormularioProtocoloViewModel,
    onRegistrarEquipo: (Long) -> Unit
) {
    if (state.visitaSeleccionada == null) return
    TarjetaSeccion(titulo = "Equipos disponibles") {
        Text(
            "Equipos de ${state.visitaSeleccionada.instalacionNombre} que aún no tienen protocolo en esta visita.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val opciones = if (state.esEdicion) listOfNotNull(state.equipoSeleccionado) else state.equipos
        opciones.forEach { equipo ->
            val elegido = equipo.id == state.equipoSeleccionado?.id
            val conError = state.errorEquipo != null
            Surface(
                onClick = { viewModel.seleccionarEquipo(equipo) },
                enabled = !state.esEdicion,
                shape = RoundedCornerShape(16.dp),
                color = if (elegido) AzulSuave else if (conError) RojoSuave else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    if (elegido || conError) 2.dp else 1.dp,
                    if (elegido) AzulPrimario else if (conError) RojoError else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(14.dp)
                ) {
                    RadioButton(selected = elegido, onClick = null)
                    Column(Modifier.weight(1f)) {
                        Text(equipo.tag, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(equipo.descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        state.errorEquipo?.let { Text(it, color = RojoError, style = MaterialTheme.typography.bodySmall) }
        if (state.equipos.isEmpty() && !state.esEdicion) {
            Text(
                "No hay equipos disponibles: todos ya tienen protocolo en esta visita o la instalación no tiene equipos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

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

@Composable
private fun SeccionAmbiente(state: FormularioProtocoloUiState, viewModel: FormularioProtocoloViewModel) {
    TarjetaSeccion(titulo = "Condiciones ambientales") {
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

@Composable
private fun TarjetaPrueba(
    seccion: SeccionPruebaUi,
    tipoEquipoId: Long,
    instrumentos: List<Instrumento>,
    puedeResultadoManual: Boolean,
    intentado: Boolean,
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
                    intentado = intentado,
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
    intentado: Boolean,
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

                        marcarError = campo.codigo in ev.camposConError ||
                                (intentado && fila.valores[campo.codigo].isNullOrBlank()),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (par.size == 1) Spacer(Modifier.weight(1f))
            }
        }

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
            "$pruebasCompletas mediciones completas.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DatoLectura(etiqueta: String, valor: String) {
    Column {
        Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
        Text(valor.ifBlank { "-" }, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun CampoNumero(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    unidad: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    marcarError: Boolean = false,

    permitirNegativo: Boolean = true
) {
    OutlinedTextField(
        value = valor,
        onValueChange = { nuevo ->

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