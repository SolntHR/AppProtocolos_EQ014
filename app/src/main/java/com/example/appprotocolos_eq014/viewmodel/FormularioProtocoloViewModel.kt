package com.example.appprotocolos_eq014.viewmodel

import android.content.Context
import android.database.sqlite.SQLiteException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.data.SesionPreferencias
import com.example.appprotocolos_eq014.model.EquipoProtocolo
import com.example.appprotocolos_eq014.model.EstadoProtocolo
import com.example.appprotocolos_eq014.notificaciones.NotificacionesLocales
import com.example.appprotocolos_eq014.model.EstadoFila
import com.example.appprotocolos_eq014.model.Fechas
import com.example.appprotocolos_eq014.model.FilaPruebaUi
import com.example.appprotocolos_eq014.model.FormularioProtocoloUiState
import com.example.appprotocolos_eq014.model.MedicionAnterior
import com.example.appprotocolos_eq014.model.Parametros
import com.example.appprotocolos_eq014.model.ReglasProtocolo
import com.example.appprotocolos_eq014.model.ResultadoPrueba
import com.example.appprotocolos_eq014.model.Rol
import com.example.appprotocolos_eq014.model.SeccionPruebaUi
import com.example.appprotocolos_eq014.model.TipoPrueba
import com.example.appprotocolos_eq014.model.VisitaAbierta
import com.example.appprotocolos_eq014.repository.ProtocoloAGuardar
import com.example.appprotocolos_eq014.repository.PruebaAGuardar
import com.example.appprotocolos_eq014.repository.RegistroProtocoloRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FormularioProtocoloViewModel(
    private val sesion: SesionPreferencias,
    private val repository: RegistroProtocoloRepository,
    private val contexto: Context // contexto de la aplicacion, para las notificaciones locales
) : ViewModel() {

    private val _uiState = MutableStateFlow(FormularioProtocoloUiState())
    val uiState: StateFlow<FormularioProtocoloUiState> = _uiState.asStateFlow()

    // ---------------------------------------------------------------
    // Carga inicial: nuevo protocolo (opcionalmente con visita) o edicion
    // ---------------------------------------------------------------
    // Evita reiniciar el formulario al girar la pantalla (el ViewModel sobrevive, la pantalla no)
    private var claveActual: String? = null

    fun descartar() {
        claveActual = null
    }

    fun iniciar(idVisita: Long?, idProtocolo: String?) {
        val clave = "$idVisita|$idProtocolo"
        if (clave == claveActual) return
        claveActual = clave
        val idUsuario = sesion.leerIdUsuario()
        val rol = sesion.leerRolUsuario().orEmpty()
        _uiState.value = FormularioProtocoloUiState(cargando = true, rolUsuario = rol, fechaHoraPrueba = Fechas.ahora())

        viewModelScope.launch {
            try {
                val nombre = repository.nombreUsuario(idUsuario)
                val instrumentos = repository.instrumentos()
                val visitas = repository.visitasAbiertas(idUsuario, rol)
                _uiState.update {
                    it.copy(tecnicoNombre = nombre, instrumentos = instrumentos, visitas = visitas)
                }

                if (idProtocolo != null) {
                    cargarEdicion(idProtocolo, idUsuario)
                } else {
                    _uiState.update { it.copy(cargando = false) }
                    val visita = visitas.firstOrNull { it.id == idVisita }
                    if (visita != null) seleccionarVisita(visita)
                }
            } catch (e: SQLiteException) {
                _uiState.update { it.copy(cargando = false, error = "No se pudo cargar el formulario: ${e.message}") }
            }
        }
    }

    private suspend fun cargarEdicion(idProtocolo: String, idUsuario: Long) {
        val p = repository.paraEditar(idProtocolo)
        if (p == null) {
            _uiState.update { it.copy(cargando = false, error = "El protocolo no existe.") }
            return
        }
        if (p.creadorId != idUsuario) {
            _uiState.update { it.copy(cargando = false, error = "Solo quien creó el protocolo puede modificarlo (RN-17).") }
            return
        }
        val visita = repository.visita(p.visitaId)
        val equipos = if (visita == null) emptyList()
        else repository.equiposDisponibles(visita.id, visita.instalacionId, idProtocolo)
        val equipo = equipos.firstOrNull { it.id == p.equipoId }
        val anteriores = repository.medicionesAnteriores(p.equipoId, idProtocolo, p.fechaHoraPrueba)

        var secciones = if (equipo == null) emptyList() else seccionesVacias(equipo)
        // Se vuelcan las pruebas guardadas en las filas del formulario
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
                            // Si el rele no opero, no se guardo el tiempo medido
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
                idProtocolo = p.idProtocolo,
                numero = p.numero,
                estadoActual = p.estado,
                fechaHoraPrueba = p.fechaHoraPrueba,
                visitas = if (visita != null && it.visitas.none { v -> v.id == visita.id }) it.visitas + visita else it.visitas,
                visitaSeleccionada = visita,
                equipos = equipos,
                equipoSeleccionado = equipo,
                temperatura = p.temperatura?.aTexto().orEmpty(),
                humedad = p.humedad?.aTexto().orEmpty(),
                descripcionMantenimiento = p.descripcionMantenimiento.orEmpty(),
                observaciones = p.observaciones.orEmpty(),
                anteriores = anteriores,
                secciones = secciones
            )
        }
        reevaluarTodo()
    }

    // ---------------------------------------------------------------
    // 1. Visita y 2. Equipo
    // ---------------------------------------------------------------
    fun seleccionarVisita(visita: VisitaAbierta) {
        if (_uiState.value.esEdicion) return
        _uiState.update {
            it.copy(visitaSeleccionada = visita, equipoSeleccionado = null, equipos = emptyList(), secciones = emptyList(), error = null)
        }
        viewModelScope.launch {
            val equipos = repository.equiposDisponibles(visita.id, visita.instalacionId, null)
            _uiState.update { it.copy(equipos = equipos) }
        }
    }

    // HU-01: se registro un equipo nuevo desde el formulario
    fun onEquipoCreado(idEquipo: Long) {
        val visita = _uiState.value.visitaSeleccionada ?: return
        viewModelScope.launch {
            val equipos = repository.equiposDisponibles(visita.id, visita.instalacionId, null)
            _uiState.update { it.copy(equipos = equipos) }
            equipos.firstOrNull { it.id == idEquipo }?.let { seleccionarEquipo(it) }
        }
    }

    fun seleccionarEquipo(equipo: EquipoProtocolo) {
        if (_uiState.value.esEdicion) return
        _uiState.update { it.copy(equipoSeleccionado = equipo, secciones = seccionesVacias(equipo), anteriores = emptyMap(), error = null) }
        viewModelScope.launch {
            // RN-09: valores de la prueba anterior del mismo tipo en el mismo equipo
            val anteriores = repository.medicionesAnteriores(equipo.id, null, Fechas.ahora())
            _uiState.update { it.copy(anteriores = anteriores) }
            reevaluarTodo()
        }
    }

    // Arma las secciones segun RN-05 con valores pre-cargados desde la placa del equipo
    private fun seccionesVacias(equipo: EquipoProtocolo): List<SeccionPruebaUi> {
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
            // Se sugiere el primer instrumento vigente del tipo correcto
            val sugeridos = instrumentos.filter {
                it.nombreTipo.lowercase().contains(ReglasProtocolo.palabraInstrumento(tipo))
            }
            val hoy = Fechas.hoy()
            val sugerido = sugeridos.firstOrNull { it.vigenteEn(hoy) } ?: sugeridos.firstOrNull()
            SeccionPruebaUi(tipo = tipo, filas = filas, comunes = comunes, instrumentoId = sugerido?.id)
        }
    }

    // ---------------------------------------------------------------
    // 3. Condiciones ambientales (RNF-05: rangos razonables)
    // ---------------------------------------------------------------
    fun onTemperaturaCambia(valor: String) {
        val n = ReglasProtocolo.numero(valor)
        val error = when {
            valor.isBlank() -> null
            n == null -> "Ingresa un número"
            n < -30 || n > 60 -> "Rango permitido: -30 a 60 °C"
            else -> null
        }
        _uiState.update { it.copy(temperatura = valor, errorTemperatura = error, error = null) }
    }

    fun onHumedadCambia(valor: String) {
        val n = ReglasProtocolo.numero(valor)
        val error = when {
            valor.isBlank() -> null
            n == null -> "Ingresa un número"
            n < 0 || n > 100 -> "Rango permitido: 0 a 100 %"
            else -> null
        }
        _uiState.update { it.copy(humedad = valor, errorHumedad = error, error = null) }
    }

    // ---------------------------------------------------------------
    // 4. Pruebas y mediciones
    // ---------------------------------------------------------------
    fun onValorFila(tipo: TipoPrueba, indice: Int, codigo: String, valor: String) =
        actualizarFila(tipo, indice) { it.copy(valores = it.valores + (codigo to valor)) }

    fun onOperaFila(tipo: TipoPrueba, indice: Int, opera: Boolean) =
        actualizarFila(tipo, indice) { it.copy(opera = opera) }

    // RN-08c: solo el ingeniero (o jefe tecnico) indica el resultado de devanados
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

    // Aclaracion 2: una prueba se marca como No realizada con motivo obligatorio
    fun onNoRealizada(tipo: TipoPrueba, noRealizada: Boolean) =
        actualizarSeccion(tipo) { it.copy(noRealizada = noRealizada) }

    fun onMotivoNoRealizada(tipo: TipoPrueba, motivo: String) =
        actualizarSeccion(tipo) { it.copy(motivoNoRealizada = motivo) }

    fun onDescripcionMantenimiento(texto: String) = _uiState.update { it.copy(descripcionMantenimiento = texto) }
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

    // Aplica las reglas a cada fila y valida el instrumento (RN-11)
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
            seccion.noRealizada || !hayCompletas -> null
            instrumento == null -> "Selecciona el instrumento utilizado (RF-05)"
            !instrumento.vigenteEn(estado.fechaHoraPrueba) ->
                "Calibración vencida el ${Fechas.fecha(instrumento.fechaVencimiento)}. No se puede usar (RN-11)"
            else -> null
        }
        return seccion.copy(filas = filas, errorInstrumento = errorInstrumento)
    }

    // ---------------------------------------------------------------
    // Guardar
    // ---------------------------------------------------------------
    fun guardarBorrador() = guardar(registrar = false)
    fun registrarEnTerreno() = guardar(registrar = true)

    private fun guardar(registrar: Boolean) {
        val s = _uiState.value
        val visita = s.visitaSeleccionada
        val equipo = s.equipoSeleccionado
        val temperatura = ReglasProtocolo.numero(s.temperatura)
        val humedad = ReglasProtocolo.numero(s.humedad)

        val error = when {
            visita == null -> "Selecciona la visita (OT)."
            equipo == null -> "Selecciona el equipo."
            s.errorTemperatura != null || s.errorHumedad != null -> "Revisa la temperatura y la humedad."
            registrar && (temperatura == null || humedad == null) ->
                "La temperatura ambiente y la humedad son obligatorias para registrar en terreno (HU-02)."
            s.secciones.any { it.filasConProblema } ->
                "Hay mediciones incompletas o fuera de rango. Revisa las filas marcadas en rojo."
            s.secciones.any { it.errorInstrumento != null } -> "Revisa el instrumento de cada prueba."
            s.secciones.any { it.noRealizada && it.motivoNoRealizada.isBlank() } ->
                "Indica el motivo de cada prueba no realizada."
            registrar && s.secciones.any { !it.todasLasFilasCompletas } ->
                "Cada prueba debe quedar completa o marcada como No realizada con su motivo."
            registrar && s.secciones.none { it.filasCompletas.isNotEmpty() } ->
                "Para registrar en terreno se requiere al menos una prueba completa (RN-04)."
            registrar && visita.incluyeMantenimiento && s.descripcionMantenimiento.isBlank() ->
                "Describe el mantenimiento realizado."
            else -> null
        }
        if (error != null || visita == null || equipo == null) {
            _uiState.update { it.copy(error = error) }
            return
        }

        val noRealizadas = s.secciones.filter { it.noRealizada }.map { sec ->
            PruebaAGuardar(
                tipo = sec.tipo,
                elemento = "Todos los elementos",
                instrumentoId = null,
                resultado = null,
                comentario = sec.comentario.ifBlank { null },
                ingresadoManualmente = false,
                valores = emptyMap(),
                realizada = false,
                motivoNoRealizada = sec.motivoNoRealizada.trim()
            )
        }
        val pruebas = noRealizadas + s.secciones.flatMap { sec ->
            sec.filasCompletas.map { f ->
                PruebaAGuardar(
                    tipo = sec.tipo,
                    elemento = f.elemento,
                    instrumentoId = sec.instrumentoId,
                    resultado = f.evaluacion.resultado,
                    comentario = sec.comentario.ifBlank { null },
                    ingresadoManualmente = sec.tipo == TipoPrueba.DEVANADOS,
                    valores = f.evaluacion.calculados
                )
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, error = null) }
            try {
                val id = repository.guardar(
                    datos = ProtocoloAGuardar(
                        idProtocolo = s.idProtocolo,
                        visitaId = visita.id,
                        equipoId = equipo.id,
                        temperatura = temperatura,
                        humedad = humedad,
                        descripcionMantenimiento = s.descripcionMantenimiento,
                        observaciones = s.observaciones,
                        pruebas = pruebas
                    ),
                    usuarioId = sesion.leerIdUsuario(),
                    rol = sesion.leerRolUsuario().orEmpty(),
                    registrarEnTerreno = registrar
                )
                // Notificacion local con el numero del protocolo
                NotificacionesLocales.notificarEstadoProtocolo(
                    contexto,
                    repository.numeroDe(id),
                    if (registrar) EstadoProtocolo.REGISTRADO_TERRENO else EstadoProtocolo.BORRADOR
                )
                _uiState.update {
                    it.copy(
                        guardando = false,
                        protocoloGuardadoId = id,
                        mensajeGuardado = if (registrar) "Protocolo registrado en terreno" else "Borrador guardado en el dispositivo"
                    )
                }
            } catch (e: IllegalArgumentException) {
                _uiState.update { it.copy(guardando = false, error = e.message) }
            } catch (e: IllegalStateException) {
                _uiState.update { it.copy(guardando = false, error = e.message) }
            } catch (e: SQLiteException) {
                _uiState.update { it.copy(guardando = false, error = "Error de base de datos: ${e.message}") }
            }
        }
    }

    fun onGuardadoConsumido() {
        claveActual = null
        _uiState.update { it.copy(protocoloGuardadoId = null, mensajeGuardado = null) }
    }
}

// 1500.0 -> "1500", 0.4 -> "0.4"
private fun Double.aTexto(): String =
    if (this % 1.0 == 0.0) this.toLong().toString() else this.toString()

private fun Double.redondear(decimales: Int): String {
    var factor = 1.0
    repeat(decimales) { factor *= 10 }
    return (Math.round(this * factor) / factor).aTexto()
}
