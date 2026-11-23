package com.example.appprotocolos_eq014.viewmodel

import android.database.sqlite.SQLiteException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.data.SesionPreferencias
import com.example.appprotocolos_eq014.model.uistate.EquipoCliente
import com.example.appprotocolos_eq014.model.uistate.InicioUiState
import com.example.appprotocolos_eq014.repository.CatalogoRepository
import com.example.appprotocolos_eq014.repository.UsuarioRepository
import com.example.appprotocolos_eq014.repository.VisitaRepository
import com.example.appprotocolos_eq014.repository.RegistroCatalogoRepository
import com.example.appprotocolos_eq014.util.Formato
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InicioViewModel(
    private val sesionPreferencias: SesionPreferencias,
    private val catalogoRepository: CatalogoRepository,
    private val visitaRepository: VisitaRepository,
    private val usuarioRepository: UsuarioRepository,
    private val catalogoClienteRepository: RegistroCatalogoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InicioUiState())
    val uiState: StateFlow<InicioUiState> = _uiState.asStateFlow()

    fun cargarDatosInicio() {
        val idUsuario = sesionPreferencias.leerIdUsuario()
        val rol = sesionPreferencias.leerRolUsuario().orEmpty()

        _uiState.update { it.copy(rolUsuario = rol, isLoading = it.primerNombre.isEmpty(), fechaHoy = Formato.encabezadoHoy(), error = null) }

        viewModelScope.launch {
          try {
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
                "TECNICO", "INGENIERO", "JEFE_TECNICO" -> {
                    val visitas = visitaRepository.visitasDeUsuario(idUsuario, verTodas = rol == "JEFE_TECNICO")
                    _uiState.update { it.copy(visitas = visitas, isLoading = false) }
                }
                else -> _uiState.update { it.copy(isLoading = false) }
            }
          } catch (e: SQLiteException) {
              _uiState.update { it.copy(isLoading = false, error = "No se pudieron cargar los datos. Inténtalo nuevamente.") }
          }
        }
    }

    private suspend fun cargarEquiposCliente(idCliente: Long) {
        val equipos = catalogoRepository.obtenerEquiposPorCliente(idCliente).map { equipo ->
            EquipoCliente(
                id = equipo.id,
                tag = equipo.tag,
                descripcion = listOfNotNull(
                    listOfNotNull(equipo.tipoEquipoNombre, equipo.marca).joinToString(" "),
                    equipo.instalacionNombre.ifEmpty { null }
                ).joinToString(" · ")
            )
        }
        val actas = catalogoClienteRepository.visitasCerradasDeCliente(idCliente)
        _uiState.update { it.copy(equiposCliente = equipos, actasCliente = actas, isLoading = false) }
    }

}
