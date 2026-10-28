package com.example.appprotocolos_eq014.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.model.InstrumentoCalibracion
import com.example.appprotocolos_eq014.model.InstrumentosUiState
import com.example.appprotocolos_eq014.repository.InstrumentoRepository
import com.example.appprotocolos_eq014.util.Formato
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InstrumentosViewModel(
    private val instrumentoRepository: InstrumentoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InstrumentosUiState())
    val uiState: StateFlow<InstrumentosUiState> = _uiState.asStateFlow()

    init {
        cargarInstrumentos()
    }

    private fun cargarInstrumentos() {
        viewModelScope.launch {
            val instrumentos = instrumentoRepository.listarConCalibracion()
            _uiState.update { it.copy(isLoading = false, instrumentos = instrumentos) }
        }
    }

    fun onRegistrarCalibracion(instrumento: InstrumentoCalibracion) {
        _uiState.update {
            it.copy(
                instrumentoEditando = instrumento,
                fechaCalibracion = Formato.hoyPantalla(),
                fechaVencimiento = Formato.enUnAnioPantalla(),
                errorFechaCalibracion = null,
                errorFechaVencimiento = null
            )
        }
    }

    fun onCerrarFormulario() {
        _uiState.update { it.copy(instrumentoEditando = null) }
    }

    fun onFechaCalibracionCambia(valor: String) {
        _uiState.update { it.copy(fechaCalibracion = valor, errorFechaCalibracion = null) }
    }

    fun onFechaVencimientoCambia(valor: String) {
        _uiState.update { it.copy(fechaVencimiento = valor, errorFechaVencimiento = null) }
    }

    fun onGuardarCalibracion() {
        val estado = _uiState.value
        val instrumento = estado.instrumentoEditando ?: return
        val calibracion = Formato.fechaParaGuardar(estado.fechaCalibracion)
        val vencimiento = Formato.fechaParaGuardar(estado.fechaVencimiento)

        val errorCalibracion = when {
            calibracion == null -> "Fecha inválida. Formato: dd-mm-aaaa"
            (Formato.diasHasta(calibracion) ?: 0) > 0 -> "La calibración no puede ser una fecha futura"
            else -> null
        }
        val errorVencimiento = when {
            vencimiento == null -> "Fecha inválida. Formato: dd-mm-aaaa"
            calibracion != null && vencimiento <= calibracion -> "Debe ser posterior a la fecha de calibración"
            else -> null
        }
        if (errorCalibracion != null || errorVencimiento != null) {
            _uiState.update {
                it.copy(errorFechaCalibracion = errorCalibracion, errorFechaVencimiento = errorVencimiento)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            val guardado = instrumentoRepository.registrarCalibracion(instrumento.id, calibracion!!, vencimiento!!)
            val instrumentos = instrumentoRepository.listarConCalibracion()
            _uiState.update {
                it.copy(
                    guardando = false,
                    instrumentos = instrumentos,
                    instrumentoEditando = if (guardado) null else it.instrumentoEditando,
                    mensaje = if (guardado) "Calibración registrada para ${instrumento.nombre}" else "No se pudo registrar la calibración"
                )
            }
        }
    }

    fun onMensajeMostrado() {
        _uiState.update { it.copy(mensaje = null) }
    }
}
