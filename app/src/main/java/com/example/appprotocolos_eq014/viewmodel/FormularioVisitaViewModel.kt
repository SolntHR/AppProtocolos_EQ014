package com.example.appprotocolos_eq014.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.data.remote.ErrorApi
import com.example.appprotocolos_eq014.model.catalogos.Rol
import com.example.appprotocolos_eq014.model.uistate.FormularioVisitaUiState
import com.example.appprotocolos_eq014.model.uistate.OpcionCatalogo
import com.example.appprotocolos_eq014.repository.RegistroCatalogoRepository
import com.example.appprotocolos_eq014.repository.UsuarioRepository
import com.example.appprotocolos_eq014.repository.VisitaRepository
import com.example.appprotocolos_eq014.util.Formato
import com.example.appprotocolos_eq014.util.ValidacionesVisita
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FormularioVisitaViewModel(
    private val idUsuario: Long,
    private val catalogo: RegistroCatalogoRepository,
    private val usuarios: UsuarioRepository,
    private val visitas: VisitaRepository,
    private val idVisita: Long? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(FormularioVisitaUiState(idVisita = idVisita, fecha = Formato.hoyPantalla()))
    val uiState: StateFlow<FormularioVisitaUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    private fun cargar() {
        viewModelScope.launch {
            try {
                val instalaciones = catalogo.instalaciones()
                val activos = usuarios.listarUsuarios().filter { it.activo }
                val ingenieros = activos
                    .filter { it.rolCodigo == Rol.INGENIERO || it.rolCodigo == Rol.JEFE_TECNICO }
                    .map { OpcionCatalogo(it.id.toLong(), "${it.nombre} · ${it.rolNombre}") }
                val personal = activos
                    .filter { Rol.esPersonalInduztek(it.rolCodigo) && it.rolCodigo != "SIN_ASIGNAR" }
                    .map { OpcionCatalogo(it.id.toLong(), "${it.nombre} · ${it.rolNombre}") }
                val visita = idVisita?.let { visitas.obtenerVisita(it) }
                _uiState.update {
                    val base = it.copy(
                        cargando = false,
                        instalaciones = instalaciones,
                        ingenieros = ingenieros,
                        personal = personal,
                        ingeniero = ingenieros.firstOrNull { opcion -> opcion.id == idUsuario }
                    )
                    if (visita == null) base else base.copy(
                        numeroOt = visita.numeroOt,
                        instalacion = instalaciones.firstOrNull { opcion -> opcion.id == visita.instalacionId },
                        instalacionBloqueada = visita.totalProtocolos > 0,
                        fecha = Formato.fecha(visita.fecha),
                        fechaOriginal = visita.fecha.take(10),
                        tipoVisita = visita.tipoVisita,
                        ingeniero = ingenieros.firstOrNull { opcion -> opcion.id == visita.ingenieroCargoId },
                        participantes = visita.participantes.toSet()
                    )
                }
            } catch (e: ErrorApi) {
                _uiState.update { it.copy(cargando = false, error = e.message) }
            }
        }
    }

    fun onNumeroOt(valor: String) = actualizar(ValidacionesVisita.CAMPO_OT) { it.copy(numeroOt = valor.uppercase()) }
    fun onInstalacion(valor: OpcionCatalogo) = actualizar(ValidacionesVisita.CAMPO_INSTALACION) { it.copy(instalacion = valor) }
    fun onFecha(valor: String) = actualizar(ValidacionesVisita.CAMPO_FECHA) { it.copy(fecha = valor) }
    fun onTipoVisita(valor: String) = _uiState.update { it.copy(tipoVisita = valor) }
    fun onIngeniero(valor: OpcionCatalogo) = actualizar(ValidacionesVisita.CAMPO_INGENIERO) { it.copy(ingeniero = valor) }

    fun onParticipante(id: Long) = actualizar(ValidacionesVisita.CAMPO_PARTICIPANTES) {
        it.copy(participantes = if (id in it.participantes) it.participantes - id else it.participantes + id)
    }

    private fun actualizar(campo: String, cambio: (FormularioVisitaUiState) -> FormularioVisitaUiState) {
        _uiState.update { cambio(it).copy(errores = it.errores - campo, error = null) }
    }

    fun guardar() {
        val s = _uiState.value
        if (s.guardando) return
        val errores = ValidacionesVisita.validar(s)
        if (errores.isNotEmpty()) {
            _uiState.update { it.copy(errores = errores, error = "Revisa los campos marcados en rojo.") }
            return
        }
        val instalacion = s.instalacion ?: return
        val ingeniero = s.ingeniero ?: return
        val fecha = Formato.fechaParaGuardar(s.fecha) ?: return
        val numeroOt = ValidacionesVisita.normalizarOt(s.numeroOt)
        val participantes = s.participantes.toList()

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, error = null) }
            try {
                val id = if (idVisita == null) {
                    visitas.crearVisita(idUsuario, numeroOt, instalacion.id, fecha, s.tipoVisita, ingeniero.id, participantes)
                } else {
                    visitas.actualizarVisita(idVisita, idUsuario, numeroOt, instalacion.id, fecha, s.tipoVisita, ingeniero.id, participantes)
                }
                _uiState.update { it.copy(guardando = false, visitaCreadaId = id) }
            } catch (e: ErrorApi) {
                _uiState.update { it.copy(guardando = false, errores = it.errores + e.campos, error = e.message) }
            }
        }
    }
}