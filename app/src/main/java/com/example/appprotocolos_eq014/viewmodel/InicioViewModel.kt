package com.example.appprotocolos_eq014.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.data.SesionPreferencias
import com.example.appprotocolos_eq014.model.EquipoCliente
import com.example.appprotocolos_eq014.model.EstadoProtocolo
import com.example.appprotocolos_eq014.model.InicioUiState
import com.example.appprotocolos_eq014.model.ProtocoloCliente
import com.example.appprotocolos_eq014.repository.CatalogoRepository
import com.example.appprotocolos_eq014.repository.ProtocoloRepository
import com.example.appprotocolos_eq014.repository.UsuarioRepository
import com.example.appprotocolos_eq014.repository.VisitaRepository
import com.example.appprotocolos_eq014.util.Formato
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InicioViewModel(
    private val sesionPreferencias: SesionPreferencias,
    private val catalogoRepository: CatalogoRepository,
    private val protocoloRepository: ProtocoloRepository,
    private val visitaRepository: VisitaRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InicioUiState())
    val uiState: StateFlow<InicioUiState> = _uiState.asStateFlow()

    fun cargarDatosInicio() {
        val idUsuario = sesionPreferencias.leerIdUsuario()
        val rol = sesionPreferencias.leerRolUsuario().orEmpty()

        _uiState.update { it.copy(rolUsuario = rol, isLoading = it.primerNombre.isEmpty(), fechaHoy = Formato.encabezadoHoy()) }

        viewModelScope.launch {
            val usuario = usuarioRepository.obtenerPorId(idUsuario)
            val nombrePersona = if (rol == "CLIENTE") usuario?.contactoNombre ?: usuario?.nombre else usuario?.nombre
            _uiState.update {
                it.copy(
                    primerNombre = nombrePersona.orEmpty().substringBefore(" "),
                    nombreCompleto = nombrePersona.orEmpty(),
                    rolNombre = usuario?.rolNombre.orEmpty(),
                    empresa = if (rol == "CLIENTE") usuario?.nombre.orEmpty() else ""
                )
            }

            when (rol) {
                "CLIENTE" -> cargarEquiposCliente(idUsuario)
                "TECNICO", "INGENIERO" -> {
                    val visitas = visitaRepository.visitasDeUsuario(idUsuario)
                    _uiState.update { it.copy(visitas = visitas, isLoading = false) }
                }
                else -> _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun cargarEquiposCliente(idCliente: Long) {
        val equipos = catalogoRepository.obtenerEquiposPorCliente(idCliente).map { equipo ->
            val protocolos = protocoloRepository.historialEquipo(equipo.id)
                .filter { it.estado.visibleParaCliente }
                .map {
                    ProtocoloCliente(
                        numero = it.numero,
                        fecha = Formato.fecha(it.fecha),
                        estado = it.estado.etiquetaCliente,
                        formalizado = it.estado == EstadoProtocolo.FORMALIZADO
                    )
                }
            EquipoCliente(
                id = equipo.id,
                tag = equipo.tag,
                descripcion = listOfNotNull(
                    listOfNotNull(equipo.tipoEquipoNombre, equipo.marca).joinToString(" "),
                    equipo.instalacion.ifEmpty { null }
                ).joinToString(" · "),
                protocolos = protocolos
            )
        }
        _uiState.update { it.copy(equiposCliente = equipos, isLoading = false) }
    }

    fun onBusquedaCambia(query: String) {
        _uiState.update { it.copy(busqueda = query) }
    }
}
