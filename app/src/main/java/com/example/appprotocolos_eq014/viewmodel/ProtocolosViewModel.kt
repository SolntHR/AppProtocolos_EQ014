package com.example.appprotocolos_eq014.viewmodel

import android.content.Context
import android.database.sqlite.SQLiteException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.data.SesionPreferencias
import com.example.appprotocolos_eq014.model.DetalleProtocoloUiState
import com.example.appprotocolos_eq014.model.EstadoProtocolo
import com.example.appprotocolos_eq014.model.ListaProtocolosUiState
import com.example.appprotocolos_eq014.model.Rol
import com.example.appprotocolos_eq014.notificaciones.NotificacionesLocales
import com.example.appprotocolos_eq014.repository.RegistroProtocoloRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Listado general de protocolos (RF-13) y detalle con formalizar / observar / anular (RF-12)
class ProtocolosViewModel(
    private val sesion: SesionPreferencias,
    private val repository: RegistroProtocoloRepository,
    private val contexto: Context // contexto de la aplicacion, para las notificaciones locales
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
                val protocolos = repository.todos()
                val promedio = repository.promedioDiasFormalizacion()
                _lista.update { it.copy(cargando = false, protocolos = protocolos, promedioDiasFormalizacion = promedio) }
            } catch (e: SQLiteException) {
                _lista.update { it.copy(cargando = false, error = "No se pudo cargar el listado: ${e.message}") }
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
                _detalle.update {
                    it.copy(cargando = false, detalle = d, error = if (d == null) "El protocolo no existe." else null)
                }
            } catch (e: SQLiteException) {
                _detalle.update { it.copy(cargando = false, error = "Error al cargar: ${e.message}") }
            }
        }
    }

    fun cambiarEstado(nuevo: EstadoProtocolo, motivo: String?) {
        val d = _detalle.value.detalle ?: return
        // RN-09: con variacion significativa el ingeniero debe comentar antes de formalizar
        if (nuevo == EstadoProtocolo.FORMALIZADO && d.tieneVariacionSignificativa && motivo.isNullOrBlank()) {
            _detalle.update { it.copy(error = "Hay variaciones significativas: escribe un comentario antes de formalizar (RN-09).") }
            return
        }
        viewModelScope.launch {
            _detalle.update { it.copy(procesando = true, error = null) }
            try {
                repository.cambiarEstado(
                    idProtocolo = d.resumen.id,
                    nuevo = nuevo,
                    usuarioId = sesion.leerIdUsuario(),
                    rol = sesion.leerRolUsuario().orEmpty(),
                    motivo = motivo
                )
                NotificacionesLocales.notificarEstadoProtocolo(contexto, d.resumen.numero, nuevo)
                _detalle.update { it.copy(procesando = false, mensaje = "Protocolo ${nuevo.etiqueta.lowercase()}") }
                cargarDetalle(d.resumen.id)
            } catch (e: IllegalArgumentException) {
                _detalle.update { it.copy(procesando = false, error = e.message) }
            } catch (e: IllegalStateException) {
                _detalle.update { it.copy(procesando = false, error = e.message) }
            } catch (e: SQLiteException) {
                _detalle.update { it.copy(procesando = false, error = "Error de base de datos: ${e.message}") }
            }
        }
    }

    fun onMensajeMostrado() = _detalle.update { it.copy(mensaje = null) }
}
