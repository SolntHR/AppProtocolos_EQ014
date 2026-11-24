package com.example.appprotocolos_eq014.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.data.SesionPreferencias
import com.example.appprotocolos_eq014.data.remote.ErrorApi
import com.example.appprotocolos_eq014.model.catalogos.EstadoProtocolo
import com.example.appprotocolos_eq014.model.catalogos.Rol
import com.example.appprotocolos_eq014.model.uistate.DetalleProtocoloUiState
import com.example.appprotocolos_eq014.model.uistate.ListaProtocolosUiState
import com.example.appprotocolos_eq014.notificaciones.NotificacionesLocales
import com.example.appprotocolos_eq014.repository.ProtocoloRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProtocolosViewModel(
    private val sesion: SesionPreferencias,
    private val repository: ProtocoloRepository,
    private val contexto: Context
) : ViewModel() {
    private val _lista = MutableStateFlow(ListaProtocolosUiState())
    val lista: StateFlow<ListaProtocolosUiState> = _lista.asStateFlow()

    private val _detalle = MutableStateFlow(DetalleProtocoloUiState())
    val detalle: StateFlow<DetalleProtocoloUiState> = _detalle.asStateFlow()

    fun cargarLista() {
        val rol = sesion.leerRolUsuario().orEmpty()
        val id = sesion.leerIdUsuario()
        _lista.update { it.copy(cargando = true, rolUsuario = rol, idUsuario = id, error = null) }
        viewModelScope.launch {
            if (!Rol.esPersonalInduztek(rol)) {
                _lista.update { it.copy(cargando = false, protocolos = emptyList(), error = "Sin permiso.") }
                return@launch
            }
            try {
                val protocolos = repository.listar()
                val promedio = repository.promedioDiasFormalizacion()
                _lista.update { it.copy(cargando = false, protocolos = protocolos, promedioDiasFormalizacion = promedio) }
            } catch (e: ErrorApi) {
                _lista.update { it.copy(cargando = false, error = e.message) }
            }
        }
    }

    fun onFiltroEstado(estado: EstadoProtocolo?) = _lista.update { it.copy(filtroEstado = estado) }
    fun onBusqueda(texto: String) = _lista.update { it.copy(busqueda = texto) }
    fun onSoloMios(valor: Boolean) = _lista.update { it.copy(soloMios = valor) }
    fun onSoloAtrasados(valor: Boolean) = _lista.update { it.copy(soloAtrasados = valor) }

    fun cargarDetalle(idProtocolo: String) {
        val rol = sesion.leerRolUsuario().orEmpty()
        val id = sesion.leerIdUsuario()
        _detalle.value = DetalleProtocoloUiState(
            cargando = true, rolUsuario = rol, idUsuario = id, mensaje = _detalle.value.mensaje
        )
        viewModelScope.launch {
            if (!Rol.esPersonalInduztek(rol)) {
                _detalle.update { it.copy(cargando = false, error = "Sin permiso.") }
                return@launch
            }
            try {
                val d = repository.detalle(idProtocolo)
                val historialEquipo = repository.historialEquipo(d.equipoId)
                _detalle.update { it.copy(cargando = false, detalle = d, historialEquipo = historialEquipo, error = null) }
            } catch (e: ErrorApi) {
                _detalle.update { it.copy(cargando = false, error = e.message) }
            }
        }
    }

    fun cambiarEstado(nuevo: EstadoProtocolo, motivo: String?) {
        val d = _detalle.value.detalle ?: return

        if (nuevo == EstadoProtocolo.FORMALIZADO && d.tieneVariacionSignificativa && motivo.isNullOrBlank()) {
            _detalle.update { it.copy(error = "Hay variaciones significativas: escribe un comentario antes de formalizar (RN-09).") }
            return
        }
        viewModelScope.launch {
            _detalle.update { it.copy(procesando = true, error = null) }
            try {
                repository.cambiarEstado(d.id, nuevo, sesion.leerIdUsuario(), motivo)
                NotificacionesLocales.notificarEstadoProtocolo(contexto, d.numero, nuevo)
                _detalle.update { it.copy(procesando = false, mensaje = "Protocolo ${nuevo.etiqueta.lowercase()}") }
                cargarDetalle(d.id)
            } catch (e: ErrorApi) {
                _detalle.update { it.copy(procesando = false, error = e.campos.values.firstOrNull() ?: e.message) }
            }
        }
    }

    fun onMensajeMostrado() = _detalle.update { it.copy(mensaje = null) }
}