package com.example.appprotocolos_eq014.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.data.SesionPreferencias
import com.example.appprotocolos_eq014.model.ActaUiState
import com.example.appprotocolos_eq014.model.EstadoProtocolo
import com.example.appprotocolos_eq014.repository.VisitaRepository
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
    private val idVisita: Long
) : ViewModel() {

    private val rolPuedeCerrar = sesionPreferencias.leerRolUsuario() in setOf("TECNICO", "INGENIERO")

    private val _uiState = MutableStateFlow(ActaUiState(fechaHora = Formato.ahoraParaMostrar()))
    val uiState: StateFlow<ActaUiState> = _uiState.asStateFlow()

    init {
        cargarActa()
    }

    private fun cargarActa() {
        viewModelScope.launch {
            val acta = visitaRepository.obtenerActa(idVisita)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    acta = acta,
                    puedeRegistrarRecepcion = rolPuedeCerrar && acta != null && acta.recepcion == null,
                    errorGeneral = if (acta == null) "No se encontró la visita" else null
                )
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

        val pendientes = acta.protocolos.filter { it.estado == EstadoProtocolo.BORRADOR }.map { it.numero }
        val errorGeneral = if (pendientes.isNotEmpty()) {
            "No se puede cerrar la visita. Protocolos en borrador: ${pendientes.joinToString(", ")}"
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
            val registrada = visitaRepository.registrarRecepcion(
                idVisita = idVisita,
                nombre = estado.receptorNombre.trim(),
                rut = estado.receptorRut.trim(),
                cargo = estado.receptorCargo.trim(),
                fechaHora = Formato.ahoraParaGuardar()
            )
            if (registrada) {
                val actaActualizada = visitaRepository.obtenerActa(idVisita)
                _uiState.update {
                    it.copy(
                        guardando = false,
                        acta = actaActualizada,
                        puedeRegistrarRecepcion = false,
                        recienRegistrada = true
                    )
                }
            } else {
                _uiState.update { it.copy(guardando = false, errorGeneral = "No se pudo registrar la recepción") }
            }
        }
    }
}
