package com.example.appprotocolos_eq014.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.data.SesionPreferencias
import com.example.appprotocolos_eq014.data.remote.ErrorApi
import com.example.appprotocolos_eq014.model.uistate.ActaUiState
import com.example.appprotocolos_eq014.model.catalogos.EstadoProtocolo
import com.example.appprotocolos_eq014.repository.VisitaRepository
import com.example.appprotocolos_eq014.repository.RegistroCatalogoRepository
import com.example.appprotocolos_eq014.notificaciones.NotificacionesLocales
import com.example.appprotocolos_eq014.util.Formato
import com.example.appprotocolos_eq014.util.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ActaViewModel(
    sesionPreferencias: SesionPreferencias,
    private val visitaRepository: VisitaRepository,
    private val catalogoClienteRepository: RegistroCatalogoRepository,
    private val contexto: Context,
    private val idVisita: Long
) : ViewModel() {
    private val rolPuedeCerrar = sesionPreferencias.leerRolUsuario() in setOf("TECNICO", "INGENIERO")
    private val esCliente = sesionPreferencias.leerRolUsuario() == "CLIENTE"
    private val idUsuario = sesionPreferencias.leerIdUsuario()

    private val _uiState = MutableStateFlow(ActaUiState(fechaHora = Formato.ahoraParaMostrar()))
    val uiState: StateFlow<ActaUiState> = _uiState.asStateFlow()

    init {
        cargarActa()
    }

    private fun cargarActa() {
        viewModelScope.launch {
            try {
                if (esCliente && !catalogoClienteRepository.visitaEsDeCliente(idVisita, idUsuario)) {
                    _uiState.update { it.copy(isLoading = false, errorGeneral = "No tienes acceso a esta acta.") }
                    return@launch
                }
                val acta = visitaRepository.obtenerActa(idVisita, idUsuario).let { a ->
                    if (esCliente) a.copy(protocolos = a.protocolos.filter { it.estado.visibleParaCliente }) else a
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        acta = acta,
                        puedeRegistrarRecepcion = rolPuedeCerrar && acta.recepcion == null,
                        errorGeneral = null
                    )
                }
            } catch (e: ErrorApi) {
                _uiState.update { it.copy(isLoading = false, errorGeneral = e.message) }
            }
        }
    }

    fun onNombreCambia(valor: String) {
        _uiState.update { it.copy(receptorNombre = valor, errorNombre = null) }
    }

    fun onRutCambia(valor: String) {
        _uiState.update { it.copy(receptorRut = valor.uppercase(), errorRut = null) }
    }

    fun onCargoCambia(valor: String) {
        _uiState.update { it.copy(receptorCargo = valor, errorCargo = null) }
    }

    fun onRegistrarRecepcion() {
        val estado = _uiState.value
        val acta = estado.acta ?: return

        val errorNombre = Validaciones.errorObligatorio(estado.receptorNombre, "Ingresa el nombre de quien recibe")
        val errorRut = Validaciones.errorRut(estado.receptorRut)
        val errorCargo = Validaciones.errorObligatorio(estado.receptorCargo, "Ingresa el cargo")

        val estadosPermitidos = setOf(
            EstadoProtocolo.REGISTRADO_TERRENO, EstadoProtocolo.FORMALIZADO, EstadoProtocolo.ANULADO
        )
        val pendientes = acta.protocolos.filter { it.estado !in estadosPermitidos }
            .map { "${it.numero} (${it.estado.etiqueta})" }
        val errorGeneral = if (acta.protocolos.isEmpty()) {
            "No se puede cerrar la visita: aún no tiene protocolos registrados."
        } else if (pendientes.isNotEmpty()) {
            "No se puede cerrar la visita. Faltan por registrar: ${pendientes.joinToString(", ")}"
        } else {
            null
        }

        if (listOf(errorNombre, errorRut, errorCargo, errorGeneral).any { it != null }) {
            _uiState.update {
                it.copy(
                    errorNombre = errorNombre,
                    errorRut = errorRut,
                    errorCargo = errorCargo,
                    errorGeneral = errorGeneral
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, errorGeneral = null) }
            try {
                visitaRepository.registrarRecepcion(
                    idVisita = idVisita,
                    nombre = estado.receptorNombre.trim(),
                    rut = Validaciones.formatearRut(estado.receptorRut),
                    cargo = estado.receptorCargo.trim()
                )
                NotificacionesLocales.notificarVisitaCerrada(contexto, acta.numeroOt, estado.receptorNombre.trim())
                val actaActualizada = visitaRepository.obtenerActa(idVisita, idUsuario)
                _uiState.update {
                    it.copy(
                        guardando = false,
                        acta = actaActualizada,
                        puedeRegistrarRecepcion = false,
                        recienRegistrada = true
                    )
                }
            } catch (e: ErrorApi) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        errorNombre = e.campos["receptorNombre"] ?: it.errorNombre,
                        errorRut = e.campos["receptorRut"] ?: it.errorRut,
                        errorCargo = e.campos["receptorCargo"] ?: it.errorCargo,
                        errorGeneral = e.message
                    )
                }
            }
        }
    }
}