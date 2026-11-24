package com.example.appprotocolos_eq014.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.data.SesionPreferencias
import com.example.appprotocolos_eq014.data.remote.ErrorApi
import com.example.appprotocolos_eq014.model.catalogos.EstadoProtocolo
import com.example.appprotocolos_eq014.model.entidades.Equipo
import com.example.appprotocolos_eq014.model.entidades.Protocolo
import com.example.appprotocolos_eq014.model.uistate.FilaComparacion
import com.example.appprotocolos_eq014.model.uistate.FilaHistorial
import com.example.appprotocolos_eq014.model.uistate.HistorialUiState
import com.example.appprotocolos_eq014.repository.CatalogoRepository
import com.example.appprotocolos_eq014.repository.ProtocoloRepository
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
    private val catalogoRepository: CatalogoRepository,
    private val idEquipo: Long
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        HistorialUiState(esCliente = sesionPreferencias.leerRolUsuario() == "CLIENTE")
    )
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    init {
        cargarHistorial()
    }

    private fun cargarHistorial() {
        viewModelScope.launch {
            try {
                val esCliente = _uiState.value.esCliente
                val equipo = catalogoRepository
                    .listarEquipos(if (esCliente) sesionPreferencias.leerIdUsuario() else null)
                    .firstOrNull { it.id == idEquipo }
                if (equipo == null) {
                    _uiState.update { it.copy(isLoading = false, error = "No tienes acceso a este equipo") }
                    return@launch
                }
                val protocolos = protocoloRepository.historialEquipo(idEquipo)
                    .filter { !esCliente || it.estado.visibleParaCliente }
                    .filter { it.estado != EstadoProtocolo.ANULADO }
                val comparacion = construirComparacion(protocolos)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        tag = equipo.tag,
                        descripcion = describir(equipo),
                        protocolos = protocolos.map { p -> aFila(p, esCliente) },
                        comparacion = comparacion,
                        haySignificativa = comparacion.firstOrNull()?.significativa == true
                    )
                }
            } catch (e: ErrorApi) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun construirComparacion(protocolos: List<Protocolo>): List<FilaComparacion> {
        val conLectura = protocolos.filter { it.lectura1Min != null }
        return conLectura.mapIndexed { indice, protocolo ->
            val actual = protocolo.lectura1Min!!
            val anterior = conLectura.getOrNull(indice + 1)?.lectura1Min
            val variacion = anterior?.let { (actual - it) / it * 100 }
            FilaComparacion(
                numero = protocolo.numero,
                fecha = Formato.fecha(protocolo.fechaHoraPrueba),
                valor = "${Formato.numero(actual)} MΩ",
                anterior = anterior?.let { "Ant. ${Formato.numero(it)} MΩ" },
                variacion = variacion?.let { "${Formato.numero(it, 1)} %" },
                significativa = variacion != null && abs(variacion) > LIMITE_VARIACION
            )
        }
    }

    private fun aFila(protocolo: Protocolo, esCliente: Boolean): FilaHistorial {
        val detalle = if (protocolo.lectura1Min != null && protocolo.lectura10Min != null) {
            val ip = protocolo.lectura10Min / protocolo.lectura1Min
            "${Formato.numero(protocolo.lectura1Min)} MΩ / ${Formato.numero(protocolo.lectura10Min)} MΩ · IP ${Formato.numero(ip, 2)}"
        } else {
            null
        }
        return FilaHistorial(
            numero = protocolo.numero,
            fecha = Formato.fecha(protocolo.fechaHoraPrueba),
            estado = if (esCliente) protocolo.estado.etiquetaCliente else protocolo.estado.etiqueta,
            formalizado = protocolo.estado == EstadoProtocolo.FORMALIZADO,
            resultado = protocolo.resultadoGlobal,
            detalle = detalle
        )
    }

    private fun describir(equipo: Equipo): String =
        listOfNotNull(
            listOfNotNull(equipo.tipoEquipoNombre, equipo.marca).joinToString(" "),
            equipo.numeroSerie,
            equipo.potenciaKva?.let { "${Formato.numero(it)} kVA" },
            equipo.instalacionNombre
        ).joinToString(" · ")

    private companion object {
        const val LIMITE_VARIACION = 25.0
    }
}