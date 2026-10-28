package com.example.appprotocolos_eq014.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.data.SesionPreferencias
import com.example.appprotocolos_eq014.model.EquipoDetalle
import com.example.appprotocolos_eq014.model.FilaComparacion
import com.example.appprotocolos_eq014.model.FilaHistorial
import com.example.appprotocolos_eq014.model.HistorialUiState
import com.example.appprotocolos_eq014.model.ProtocoloResumen
import com.example.appprotocolos_eq014.model.EstadoProtocolo
import com.example.appprotocolos_eq014.repository.ProtocoloRepository
import com.example.appprotocolos_eq014.repository.RegistroCatalogoRepository
import com.example.appprotocolos_eq014.util.Formato
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

class HistorialViewModel(
    private val sesionPreferencias: SesionPreferencias,
    private val protocoloRepository: ProtocoloRepository,
    private val catalogoClienteRepository: RegistroCatalogoRepository,
    private val idEquipo: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HistorialUiState(esCliente = sesionPreferencias.leerRolUsuario() == "CLIENTE")
    )
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    private var idProtocoloReciente: String? = null

    init {
        cargarHistorial()
    }

    private fun cargarHistorial() {
        viewModelScope.launch {
            // RN-18: un cliente solo puede ver equipos de su propia empresa
            if (_uiState.value.esCliente &&
                !catalogoClienteRepository.equipoEsDeCliente(idEquipo, sesionPreferencias.leerIdUsuario())
            ) {
                _uiState.update { it.copy(isLoading = false, error = "No tienes acceso a este equipo") }
                return@launch
            }
            val equipo = protocoloRepository.obtenerEquipo(idEquipo)
            if (equipo == null) {
                _uiState.update { it.copy(isLoading = false, error = "No se encontró el equipo") }
                return@launch
            }
            val esCliente = _uiState.value.esCliente
            val protocolos = protocoloRepository.historialEquipo(idEquipo)
                .filter { !esCliente || it.estado.visibleParaCliente }
                .filter { it.estado != EstadoProtocolo.ANULADO }
            idProtocoloReciente = protocolos.firstOrNull()?.id

            val comparacion = construirComparacion(protocolos)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    tag = equipo.tag,
                    descripcion = describir(equipo),
                    protocolos = protocolos.map { p -> aFila(p, esCliente) },
                    comparacion = comparacion,
                    haySignificativa = comparacion.firstOrNull()?.significativa == true,
                    comentario = if (comparacion.firstOrNull()?.significativa == true) {
                        protocolos.firstOrNull()?.observaciones.orEmpty()
                    } else {
                        ""
                    }
                )
            }
        }
    }

    fun onComentarioCambia(valor: String) {
        _uiState.update { it.copy(comentario = valor, errorComentario = null, comentarioGuardado = false) }
    }

    fun onGuardarComentario() {
        val comentario = _uiState.value.comentario.trim()
        if (comentario.isEmpty()) {
            _uiState.update { it.copy(errorComentario = "Debe dejar un comentario antes de formalizar") }
            return
        }
        val idProtocolo = idProtocoloReciente ?: return
        viewModelScope.launch {
            val guardado = protocoloRepository.guardarObservacion(idProtocolo, comentario)
            _uiState.update {
                if (guardado) it.copy(comentarioGuardado = true) else it.copy(errorComentario = "No se pudo guardar el comentario")
            }
        }
    }

    private fun construirComparacion(protocolos: List<ProtocoloResumen>): List<FilaComparacion> {
        val conLectura = protocolos.filter { it.lectura1Min != null }
        return conLectura.mapIndexed { indice, protocolo ->
            val actual = protocolo.lectura1Min!!
            val anterior = conLectura.getOrNull(indice + 1)?.lectura1Min
            val variacion = anterior?.let { (actual - it) / it * 100 }
            FilaComparacion(
                numero = protocolo.numero,
                fecha = Formato.fecha(protocolo.fecha),
                valor = "${Formato.numero(actual)} MΩ",
                anterior = anterior?.let { "Ant. ${Formato.numero(it)} MΩ" },
                variacion = variacion?.let { "${Formato.numero(it, 1)} %" },
                significativa = variacion != null && abs(variacion) > LIMITE_VARIACION
            )
        }
    }

    private fun aFila(protocolo: ProtocoloResumen, esCliente: Boolean): FilaHistorial {
        val detalle = if (protocolo.lectura1Min != null && protocolo.lectura10Min != null) {
            val ip = protocolo.lectura10Min / protocolo.lectura1Min
            "${Formato.numero(protocolo.lectura1Min)} MΩ / ${Formato.numero(protocolo.lectura10Min)} MΩ · IP ${Formato.numero(ip, 2)}"
        } else {
            null
        }
        return FilaHistorial(
            numero = protocolo.numero,
            fecha = Formato.fecha(protocolo.fecha),
            estado = if (esCliente) protocolo.estado.etiquetaCliente else protocolo.estado.etiqueta,
            formalizado = protocolo.estado == EstadoProtocolo.FORMALIZADO,
            resultado = protocolo.resultado,
            detalle = detalle
        )
    }

    private fun describir(equipo: EquipoDetalle): String =
        listOfNotNull(
            listOfNotNull(equipo.tipo, equipo.marca).joinToString(" "),
            equipo.numeroSerie,
            equipo.potenciaKva?.let { "${Formato.numero(it)} kVA" },
            equipo.instalacion
        ).joinToString(" · ")

    private companion object {
        const val LIMITE_VARIACION = 25.0
    }
}
