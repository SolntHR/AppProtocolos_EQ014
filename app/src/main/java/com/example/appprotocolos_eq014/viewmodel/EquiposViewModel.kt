package com.example.appprotocolos_eq014.viewmodel

import android.database.sqlite.SQLiteException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.data.SesionPreferencias
import com.example.appprotocolos_eq014.data.remote.ErrorApi
import com.example.appprotocolos_eq014.model.catalogos.Rol
import com.example.appprotocolos_eq014.model.uistate.DetalleEquipoUiState
import com.example.appprotocolos_eq014.model.uistate.EquiposUiState
import com.example.appprotocolos_eq014.repository.CatalogoRepository
import com.example.appprotocolos_eq014.repository.ProtocoloRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private fun rolConAcceso(rol: String) = rol in setOf(Rol.CLIENTE, Rol.TECNICO, Rol.INGENIERO, Rol.JEFE_TECNICO)

class EquiposViewModel(
    private val sesion: SesionPreferencias,
    private val catalogo: CatalogoRepository
) : ViewModel() {
    private val _estado = MutableStateFlow(EquiposUiState())
    val estado = _estado.asStateFlow()

    fun cargar() {
        val rol = sesion.leerRolUsuario().orEmpty()
        _estado.update { it.copy(cargando = true, rol = rol, error = null) }
        viewModelScope.launch {
            if (!rolConAcceso(rol)) {
                _estado.update { it.copy(cargando = false, equipos = emptyList(), error = "No tienes acceso a los equipos.") }
                return@launch
            }
            try {
                val equipos = catalogo.listarEquipos(if (rol == Rol.CLIENTE) sesion.leerIdUsuario() else null)
                _estado.update { it.copy(cargando = false, equipos = equipos) }
            } catch (e: ErrorApi) {
                _estado.update { it.copy(cargando = false, error = e.message) }
            } catch (e: SQLiteException) {
                _estado.update { it.copy(cargando = false, error = "No se pudieron cargar los equipos. Inténtalo nuevamente.") }
            }
        }
    }

    fun buscar(texto: String) = _estado.update { it.copy(busqueda = texto) }
    fun filtrarTipo(id: Long?) = _estado.update { it.copy(tipoId = id) }
    fun filtrarInstalacion(id: Long?) = _estado.update { it.copy(instalacionId = id) }
    fun limpiarFiltros() = _estado.update { it.copy(busqueda = "", tipoId = null, instalacionId = null) }
}

class DetalleEquipoViewModel(
    private val sesion: SesionPreferencias,
    private val catalogo: CatalogoRepository,
    private val protocolos: ProtocoloRepository,
    private val idEquipo: Long
) : ViewModel() {
    private val _estado = MutableStateFlow(DetalleEquipoUiState())
    val estado = _estado.asStateFlow()

    init { cargar() }

    fun cargar() {
        _estado.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            val rol = sesion.leerRolUsuario().orEmpty()
            if (!rolConAcceso(rol)) {
                _estado.value = DetalleEquipoUiState(cargando = false, error = "No tienes acceso a este equipo.")
                return@launch
            }
            try {
                val equipo = catalogo.listarEquipos(if (rol == Rol.CLIENTE) sesion.leerIdUsuario() else null)
                    .firstOrNull { it.id == idEquipo }
                if (equipo == null) {
                    _estado.value = DetalleEquipoUiState(cargando = false, error = "El equipo no está disponible para tu cuenta.")
                    return@launch
                }
                val historial = protocolos.historialEquipo(idEquipo)
                    .filter { rol != Rol.CLIENTE || it.estado.visibleParaCliente }
                _estado.value = DetalleEquipoUiState(cargando = false, equipo = equipo, protocolos = historial)
            } catch (e: ErrorApi) {
                _estado.update { it.copy(cargando = false, error = e.message) }
            } catch (e: SQLiteException) {
                _estado.update { it.copy(cargando = false, error = "No se pudo cargar la ficha del equipo.") }
            }
        }
    }
}
