package com.example.appprotocolos_eq014.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.data.SesionPreferencias
import com.example.appprotocolos_eq014.data.remote.ErrorApi
import com.example.appprotocolos_eq014.model.entidades.Equipo
import com.example.appprotocolos_eq014.model.catalogos.EstadoProtocolo
import com.example.appprotocolos_eq014.notificaciones.NotificacionesLocales
import com.example.appprotocolos_eq014.model.reglas.EstadoFila
import com.example.appprotocolos_eq014.util.Fechas
import com.example.appprotocolos_eq014.model.uistate.FilaPruebaUi
import com.example.appprotocolos_eq014.model.uistate.FormularioProtocoloUiState
import com.example.appprotocolos_eq014.model.reglas.MedicionAnterior
import com.example.appprotocolos_eq014.model.reglas.Parametros
import com.example.appprotocolos_eq014.model.reglas.ReglasProtocolo
import com.example.appprotocolos_eq014.model.catalogos.ResultadoPrueba
import com.example.appprotocolos_eq014.model.catalogos.Rol
import com.example.appprotocolos_eq014.model.uistate.SeccionPruebaUi
import com.example.appprotocolos_eq014.model.reglas.TipoPrueba
import com.example.appprotocolos_eq014.model.entidades.Visita
import com.example.appprotocolos_eq014.model.entidades.Prueba
import com.example.appprotocolos_eq014.model.entidades.Protocolo
import com.example.appprotocolos_eq014.repository.ProtocoloRepository
import com.example.appprotocolos_eq014.util.Imagenes
import com.example.appprotocolos_eq014.util.ValidacionesProtocolo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

class FormularioProtocoloViewModel(
    private val sesion: SesionPreferencias,
    private val repository: ProtocoloRepository,
    private val contexto: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(FormularioProtocoloUiState())
    val uiState: StateFlow<FormularioProtocoloUiState> = _uiState.asStateFlow()

    private var claveActual: String? = null

    fun descartar() {
        claveActual = null
    }

    fun iniciar(idVisita: Long?, idProtocolo: String?, idEquipoInicial: Long? = null) {
        val clave = "$idVisita|$idProtocolo|$idEquipoInicial"
        if (clave == claveActual) return
        claveActual = clave
        val idUsuario = sesion.leerIdUsuario()
        val rol = sesion.leerRolUsuario().orEmpty()
        _uiState.value = FormularioProtocoloUiState(cargando = true, rolUsuario = rol, fechaHoraPrueba = Fechas.ahora())

        viewModelScope.launch {
            try {
                val nombre = repository.nombreUsuario(idUsuario)
                val instrumentos = repository.instrumentos()
                val visitas = repository.visitasAbiertas(idUsuario)
                _uiState.update {
                    it.copy(tecnicoNombre = nombre, instrumentos = instrumentos, visitas = visitas)
                }

                if (idProtocolo != null) {
                    cargarEdicion(idProtocolo, idUsuario)
                } else {
                    _uiState.update { it.copy(cargando = false) }
                    val visita = visitas.firstOrNull { it.id == idVisita }
                    if (visita != null) cargarEquiposVisita(visita, idEquipoInicial)
                }
            } catch (e: ErrorApi) {
                _uiState.update { it.copy(cargando = false, error = e.message) }
            }
        }
    }

    private suspend fun cargarEdicion(idProtocolo: String, idUsuario: Long) {
        val p = repository.paraEditar(idProtocolo)
        if (p.usuarioRegistraId != idUsuario) {
            _uiState.update { it.copy(cargando = false, error = "Solo quien creó el protocolo puede modificarlo (RN-17).") }
            return
        }
        val visita = repository.visita(p.visitaId)
        val equipos = repository.equiposDisponibles(visita.id, visita.instalacionId, idProtocolo)
        val equipo = equipos.firstOrNull { it.id == p.equipoId }
        val anteriores = repository.medicionesAnteriores(p.equipoId, idProtocolo, p.fechaHoraPrueba)

        var secciones = if (equipo == null) emptyList() else seccionesVacias(equipo)

        for (prueba in p.pruebas) {
            secciones = secciones.map { s ->
                if (s.tipo != prueba.tipo) return@map s
                if (!prueba.realizada) {
                    return@map s.copy(noRealizada = true, motivoNoRealizada = prueba.motivoNoRealizada.orEmpty())
                }
                val comunes = s.comunes.toMutableMap()
                prueba.valores[Parametros.KV.codigo]?.let { comunes[Parametros.KV.codigo] = it.aTexto() }
                prueba.valores[Parametros.TAP.codigo]?.let { comunes[Parametros.TAP.codigo] = it.aTexto() }
                val campos = ReglasProtocolo.camposFila(s.tipo, equipo?.tipoEquipoId ?: 0)
                s.copy(
                    instrumentoId = prueba.instrumentoId ?: s.instrumentoId,
                    comunes = comunes,
                    unidadAislacion = "MΩ",
                    comentario = if (s.comentario.isBlank()) prueba.comentario.orEmpty() else s.comentario,
                    filas = s.filas.map { f ->
                        if (f.elemento != prueba.elemento) f
                        else f.copy(
                            valores = campos.mapNotNull { c -> prueba.valores[c.codigo]?.let { c.codigo to it.aTexto() } }.toMap(),

                            opera = !(prueba.tipo == TipoPrueba.RELE && prueba.valores[Parametros.T_MEDIDO.codigo] == null),
                            resultadoManual = if (prueba.tipo == TipoPrueba.DEVANADOS) prueba.resultado else null
                        )
                    }
                )
            }
        }

        _uiState.update {
            it.copy(
                cargando = false,
                idProtocolo = p.id,
                numero = p.numero,
                estadoActual = p.estado,
                fechaHoraPrueba = p.fechaHoraPrueba,
                visitas = if (it.visitas.none { v -> v.id == visita.id }) it.visitas + visita else it.visitas,
                visitaSeleccionada = visita,
                equipos = equipos,
                equipoSeleccionado = equipo,
                temperatura = p.temperatura?.aTexto().orEmpty(),
                humedad = p.humedad?.aTexto().orEmpty(),
                descripcionMantenimiento = p.descripcionMantenimiento.orEmpty(),
                observaciones = p.observaciones.orEmpty(),
                motivoObservacion = p.motivoObservacion,
                anteriores = anteriores,
                secciones = secciones,
                fotosGuardadas = p.evidencias
            )
        }
        reevaluarTodo()
    }

    fun seleccionarVisita(visita: Visita) {
        cargarEquiposVisita(visita)
    }

    private fun cargarEquiposVisita(visita: Visita, idEquipoInicial: Long? = null) {
        if (_uiState.value.esEdicion) return
        _uiState.update {
            it.copy(
                visitaSeleccionada = visita, equipoSeleccionado = null, equipos = emptyList(), secciones = emptyList(),
                errorVisita = null, error = null
            )
        }
        cargarEquipos(visita, idEquipoInicial)
    }

    fun onEquipoCreado(idEquipo: Long) {
        val visita = _uiState.value.visitaSeleccionada ?: return
        cargarEquipos(visita, idEquipo)
    }

    private fun cargarEquipos(visita: Visita, idEquipoASeleccionar: Long?) {
        viewModelScope.launch {
            try {
                val equipos = repository.equiposDisponibles(visita.id, visita.instalacionId, null)
                _uiState.update { it.copy(equipos = equipos) }
                equipos.firstOrNull { it.id == idEquipoASeleccionar }?.let { seleccionarEquipo(it) }
            } catch (e: ErrorApi) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun seleccionarEquipo(equipo: Equipo) {
        if (_uiState.value.esEdicion) return
        _uiState.update {
            it.copy(
                equipoSeleccionado = equipo, secciones = seccionesVacias(equipo), anteriores = emptyMap(),
                errorEquipo = null, error = null
            )
        }
        viewModelScope.launch {
            try {
                val anteriores = repository.medicionesAnteriores(equipo.id, null, Fechas.ahora())
                _uiState.update { it.copy(anteriores = anteriores) }
                reevaluarTodo()
            } catch (e: ErrorApi) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private fun seccionesVacias(equipo: Equipo): List<SeccionPruebaUi> {
        val instrumentos = _uiState.value.instrumentos
        return ReglasProtocolo.pruebasPara(equipo.tipoEquipoId).map { tipo ->
            val filas = ReglasProtocolo.elementosPara(tipo, equipo.tipoEquipoId).map { elemento ->
                val valores = when (tipo) {
                    TipoPrueba.TTR -> ReglasProtocolo.relacionTeorica(equipo.tensiones)
                        ?.let { mapOf(Parametros.REL_TEO.codigo to it.redondear(3)) }.orEmpty()
                    TipoPrueba.RELE -> equipo.corriente
                        ?.let { mapOf(Parametros.I_AJUSTE.codigo to it.aTexto()) }.orEmpty()
                    else -> emptyMap()
                }
                FilaPruebaUi(elemento = elemento, valores = valores)
            }
            val comunes = when (tipo) {
                TipoPrueba.AISLACION -> mapOf(Parametros.KV.codigo to "5")
                TipoPrueba.CONTACTOS -> equipo.maxContactosUohm
                    ?.let { mapOf(Parametros.MAX_PLACA.codigo to it.aTexto()) }.orEmpty()
                else -> emptyMap()
            }

            val sugeridos = instrumentos.filter {
                it.nombreTipo.lowercase().contains(ReglasProtocolo.palabraInstrumento(tipo))
            }
            val hoy = Fechas.hoy()
            val sugerido = sugeridos.firstOrNull { it.vigenteEn(hoy) }
            SeccionPruebaUi(tipo = tipo, filas = filas, comunes = comunes, instrumentoId = sugerido?.id)
        }
    }

    fun onTemperaturaCambia(valor: String) {
        val n = ReglasProtocolo.numero(valor)
        val error = when {
            valor.isBlank() -> null
            n == null -> "Ingresa un número"
            n !in -30.0..60.0 -> "Rango permitido: -30 a 60 °C"
            else -> null
        }
        _uiState.update { it.copy(temperatura = valor, errorTemperatura = error, error = null) }
    }

    fun onHumedadCambia(valor: String) {
        val n = ReglasProtocolo.numero(valor)
        val error = when {
            valor.isBlank() -> null
            n == null -> "Ingresa un número"
            n !in 0.0..100.0 -> "Rango permitido: 0 a 100 %"
            else -> null
        }
        _uiState.update { it.copy(humedad = valor, errorHumedad = error, error = null) }
    }

    fun onValorFila(tipo: TipoPrueba, indice: Int, codigo: String, valor: String) =
        actualizarFila(tipo, indice) { it.copy(valores = it.valores + (codigo to valor)) }

    fun onOperaFila(tipo: TipoPrueba, indice: Int, opera: Boolean) =
        actualizarFila(tipo, indice) { it.copy(opera = opera) }

    fun onResultadoManual(tipo: TipoPrueba, indice: Int, resultado: ResultadoPrueba?) {
        if (!Rol.puedeFormalizar(_uiState.value.rolUsuario)) return
        actualizarFila(tipo, indice) { it.copy(resultadoManual = resultado) }
    }

    fun onComun(tipo: TipoPrueba, codigo: String, valor: String) =
        actualizarSeccion(tipo) { it.copy(comunes = it.comunes + (codigo to valor)) }

    fun onUnidadAislacion(tipo: TipoPrueba, unidad: String) =
        actualizarSeccion(tipo) { it.copy(unidadAislacion = unidad) }

    fun onInstrumento(tipo: TipoPrueba, idInstrumento: Long) =
        actualizarSeccion(tipo) { it.copy(instrumentoId = idInstrumento) }

    fun onComentarioSeccion(tipo: TipoPrueba, texto: String) =
        actualizarSeccion(tipo) { it.copy(comentario = texto) }

    fun onNoRealizada(tipo: TipoPrueba, noRealizada: Boolean) =
        actualizarSeccion(tipo) { it.copy(noRealizada = noRealizada) }

    fun onMotivoNoRealizada(tipo: TipoPrueba, motivo: String) =
        actualizarSeccion(tipo) { it.copy(motivoNoRealizada = motivo) }

    fun onDescripcionMantenimiento(texto: String) =
        _uiState.update { it.copy(descripcionMantenimiento = texto, errorMantenimiento = null) }
    fun onObservaciones(texto: String) = _uiState.update { it.copy(observaciones = texto) }

    private fun actualizarFila(tipo: TipoPrueba, indice: Int, cambio: (FilaPruebaUi) -> FilaPruebaUi) =
        actualizarSeccion(tipo) { s ->
            s.copy(filas = s.filas.mapIndexed { i, f -> if (i == indice) cambio(f) else f })
        }

    private fun actualizarSeccion(tipo: TipoPrueba, cambio: (SeccionPruebaUi) -> SeccionPruebaUi) {
        _uiState.update { estado ->
            estado.copy(
                error = null,
                secciones = estado.secciones.map { s -> if (s.tipo == tipo) reevaluar(cambio(s), estado) else s }
            )
        }
    }

    private fun reevaluarTodo() {
        _uiState.update { estado -> estado.copy(secciones = estado.secciones.map { reevaluar(it, estado) }) }
    }

    private fun reevaluar(seccion: SeccionPruebaUi, estado: FormularioProtocoloUiState): SeccionPruebaUi {
        val tipoEquipo = estado.equipoSeleccionado?.tipoEquipoId ?: 0
        val filas = seccion.filas.map { f ->
            val anterior: MedicionAnterior? = estado.anteriores["${seccion.tipo.idDb}|${f.elemento}"]
            f.copy(
                evaluacion = ReglasProtocolo.evaluar(
                    tipo = seccion.tipo,
                    tipoEquipoId = tipoEquipo,
                    valores = f.valores,
                    comunes = seccion.comunes,
                    unidadAislacion = seccion.unidadAislacion,
                    opera = f.opera,
                    resultadoManual = f.resultadoManual,
                    anterior = anterior
                )
            )
        }
        val hayCompletas = filas.any { it.evaluacion.estado == EstadoFila.COMPLETA }
        val instrumento = estado.instrumentos.firstOrNull { it.id == seccion.instrumentoId }
        val errorInstrumento = when {
            seccion.noRealizada -> null
            instrumento != null && !instrumento.vigenteEn(estado.fechaHoraPrueba) ->
                "Calibración vencida el ${Fechas.fecha(instrumento.fechaVencimiento)}. No se puede usar."
            !hayCompletas -> null
            instrumento == null -> "Selecciona el instrumento utilizado"
            else -> null
        }
        return seccion.copy(filas = filas, errorInstrumento = errorInstrumento)
    }

    fun siguiente() {
        val s = _uiState.value
        val error = ValidacionesProtocolo.errorDelPaso(s, s.paso, registrar = false)
        _uiState.update {
            val marcado = marcarErrores(it, registrar = false)
            if (error != null) marcado.copy(error = error)
            else marcado.copy(paso = (it.paso + 1).coerceAtMost(FormularioProtocoloUiState.PASOS.lastIndex), error = null)
        }
    }

    fun anterior() = _uiState.update { it.copy(paso = (it.paso - 1).coerceAtLeast(0), error = null) }

    fun onFotoElegida(uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            val foto = Imagenes.jpegDesdeGaleria(contexto, uri)
            if (foto == null) _uiState.update { it.copy(errorFotos = "No se pudo leer la imagen.") } else agregarFoto(foto)
        }
    }

    fun onFotoCapturada(foto: Bitmap?) {
        if (foto != null) agregarFoto(Imagenes.aJpeg(foto))
    }

    fun onPermisoCamaraDenegado() =
        _uiState.update { it.copy(errorFotos = "Sin permiso de cámara. Puedes elegir la foto desde la galería.") }

    private fun agregarFoto(foto: ByteArray) {
        _uiState.update {
            if (it.totalFotos >= FormularioProtocoloUiState.MAXIMO_FOTOS) {
                it.copy(errorFotos = "Máximo ${FormularioProtocoloUiState.MAXIMO_FOTOS} fotos por protocolo.")
            } else {
                it.copy(fotosNuevas = it.fotosNuevas + foto, errorFotos = null)
            }
        }
    }

    fun onQuitarFotoNueva(indice: Int) =
        _uiState.update { it.copy(fotosNuevas = it.fotosNuevas.filterIndexed { i, _ -> i != indice }, errorFotos = null) }

    fun onEliminarFotoGuardada(idEvidencia: Long) {
        viewModelScope.launch {
            try {
                repository.eliminarEvidencia(idEvidencia, sesion.leerIdUsuario())
                _uiState.update { it.copy(fotosGuardadas = it.fotosGuardadas.filter { f -> f.id != idEvidencia }, errorFotos = null) }
            } catch (e: ErrorApi) {
                _uiState.update { it.copy(errorFotos = e.message) }
            }
        }
    }

    fun guardarBorrador() = guardar(registrar = false)
    fun registrarEnTerreno() = guardar(registrar = true)

    private fun marcarErrores(s: FormularioProtocoloUiState, registrar: Boolean): FormularioProtocoloUiState {
        val obligatorio = "Obligatorio para registrar"
        return s.copy(
            errorVisita = if (s.visitaSeleccionada == null) "Selecciona la visita" else null,
            errorEquipo = if (s.visitaSeleccionada != null && s.equipoSeleccionado == null) "Selecciona el equipo" else null,
            errorTemperatura = if (s.temperatura.isBlank()) obligatorio.takeIf { registrar } else s.errorTemperatura,
            errorHumedad = if (s.humedad.isBlank()) obligatorio.takeIf { registrar } else s.errorHumedad,
            errorMantenimiento = if (registrar && s.visitaSeleccionada?.incluyeMantenimiento == true &&
                s.descripcionMantenimiento.isBlank()) "Describe el mantenimiento realizado" else null
        )
    }

    private fun guardar(registrar: Boolean) {
        val s = _uiState.value
        if (s.guardando) return
        val problema = ValidacionesProtocolo.primerError(s, registrar)
        val visita = s.visitaSeleccionada
        val equipo = s.equipoSeleccionado
        if (problema != null || visita == null || equipo == null) {
            _uiState.update {
                marcarErrores(it, registrar).copy(paso = problema?.first ?: 0, error = problema?.second)
            }
            return
        }

        val noRealizadas = s.secciones.filter { it.noRealizada }.map { sec ->
            Prueba(
                tipo = sec.tipo,
                elemento = "Todos los elementos",
                instrumentoId = null,
                resultado = null,
                comentario = sec.comentario.ifBlank { null },
                ingresadoManualmente = false,
                realizada = false,
                motivoNoRealizada = sec.motivoNoRealizada.trim()
            )
        }
        val pruebas = noRealizadas + s.secciones.flatMap { sec ->
            sec.filasCompletas.map { f ->
                Prueba(
                    tipo = sec.tipo,
                    elemento = f.elemento,
                    instrumentoId = sec.instrumentoId,
                    resultado = f.evaluacion.resultado,
                    comentario = sec.comentario.ifBlank { null },
                    ingresadoManualmente = sec.tipo == TipoPrueba.DEVANADOS,
                    mediciones = Prueba.medicionesDesde(f.evaluacion.calculados)
                )
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, error = null) }
            val idUsuario = sesion.leerIdUsuario()
            val guardado = try {
                repository.guardar(
                    datos = Protocolo(
                        id = s.idProtocolo.orEmpty(),
                        visitaId = visita.id,
                        equipoId = equipo.id,
                        temperatura = ReglasProtocolo.numero(s.temperatura),
                        humedad = ReglasProtocolo.numero(s.humedad),
                        descripcionMantenimiento = s.descripcionMantenimiento,
                        observaciones = s.observaciones,
                        pruebas = pruebas
                    ),
                    usuarioId = idUsuario,
                    registrar = registrar
                )
            } catch (e: ErrorApi) {
                _uiState.update { it.copy(guardando = false, error = e.campos.values.firstOrNull() ?: e.message) }
                return@launch
            }

            var fotosFallidas = 0
            s.fotosNuevas.forEach { foto ->
                try {
                    repository.subirEvidencia(guardado.id, idUsuario, foto)
                } catch (_: ErrorApi) {
                    fotosFallidas++
                }
            }

            NotificacionesLocales.notificarEstadoProtocolo(
                contexto,
                guardado.numero,
                if (registrar) EstadoProtocolo.REGISTRADO_TERRENO else EstadoProtocolo.BORRADOR
            )
            val mensaje = if (registrar) "Protocolo registrado en terreno" else "Borrador guardado"
            _uiState.update {
                it.copy(
                    guardando = false,
                    fotosNuevas = emptyList(),
                    protocoloGuardadoId = guardado.id,
                    mensajeGuardado = if (fotosFallidas == 0) mensaje else "$mensaje, pero $fotosFallidas foto(s) no se subieron"
                )
            }
        }
    }

    fun onGuardadoConsumido() {
        claveActual = null
        _uiState.update { it.copy(protocoloGuardadoId = null, mensajeGuardado = null) }
    }
}

private fun Double.aTexto(): String =
    if (this % 1.0 == 0.0) this.toLong().toString() else this.toString()

private fun Double.redondear(decimales: Int): String {
    var factor = 1.0
    repeat(decimales) { factor *= 10 }
    return ((this * factor).roundToLong() / factor).aTexto()
}