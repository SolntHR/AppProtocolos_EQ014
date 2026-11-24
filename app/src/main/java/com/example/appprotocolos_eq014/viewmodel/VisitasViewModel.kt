package com.example.appprotocolos_eq014.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.data.SesionPreferencias
import com.example.appprotocolos_eq014.data.remote.ErrorApi
import com.example.appprotocolos_eq014.model.catalogos.Rol
import com.example.appprotocolos_eq014.model.entidades.Visita
import com.example.appprotocolos_eq014.model.uistate.DetalleVisitaUiState
import com.example.appprotocolos_eq014.model.uistate.FiltroVisita
import com.example.appprotocolos_eq014.model.uistate.VisitasUiState
import com.example.appprotocolos_eq014.repository.CatalogoRepository
import com.example.appprotocolos_eq014.repository.RegistroCatalogoRepository
import com.example.appprotocolos_eq014.repository.VisitaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal suspend fun visitasAccesibles(
    sesion: SesionPreferencias,
    visitas: VisitaRepository,
    catalogo: RegistroCatalogoRepository
): List<Visita> = when (sesion.leerRolUsuario()) {
    Rol.CLIENTE -> catalogo.visitasCerradasDeCliente(sesion.leerIdUsuario())
    Rol.TECNICO, Rol.INGENIERO, Rol.JEFE_TECNICO -> visitas.visitasDeUsuario(sesion.leerIdUsuario())
    else -> emptyList()
}

class VisitasViewModel(
    private val sesion: SesionPreferencias,
    private val visitas: VisitaRepository,
    private val catalogo: RegistroCatalogoRepository
) : ViewModel() {
    private val _estado = MutableStateFlow(VisitasUiState())
    val estado = _estado.asStateFlow()

    fun cargar() {
        _estado.update {
            it.copy(
                cargando = true,
                rol = sesion.leerRolUsuario().orEmpty(),
                error = null
            )
        }
        viewModelScope.launch {
            try {
                val lista = visitasAccesibles(sesion, visitas, catalogo)
                _estado.update { it.copy(cargando = false, visitas = lista) }
            } catch (e: ErrorApi) {
                _estado.update { it.copy(cargando = false, error = e.message) }
            }
        }
    }

    fun buscar(texto: String) = _estado.update { it.copy(busqueda = texto) }
    fun filtrar(filtro: FiltroVisita) = _estado.update { it.copy(filtro = filtro) }
}

class DetalleVisitaViewModel(
    private val sesion: SesionPreferencias,
    private val visitas: VisitaRepository,
    private val catalogo: RegistroCatalogoRepository,
    private val equipos: CatalogoRepository,
    private val idVisita: Long
) : ViewModel() {
    private val _estado = MutableStateFlow(DetalleVisitaUiState())
    val estado = _estado.asStateFlow()

    fun cargar() {
        val rol = sesion.leerRolUsuario().orEmpty()
        _estado.update {
            it.copy(
                cargando = true,
                rol = rol,
                usuarioId = sesion.leerIdUsuario(),
                error = null
            )
        }
        viewModelScope.launch {
            try {
                if (visitasAccesibles(sesion, visitas, catalogo).none { it.id == idVisita }) {
                    _estado.update {
                        it.copy(
                            cargando = false,
                            visita = null,
                            error = "No tienes acceso a esta visita."
                        )
                    }
                    return@launch
                }
                val visita = visitas.obtenerActa(idVisita, sesion.leerIdUsuario()).let { visita ->
                    if (rol == Rol.CLIENTE) visita.copy(protocolos = visita.protocolos.filter { it.estado.visibleParaCliente })
                    else visita
                }
                val listaEquipos = equipos.listarEquipos(
                    if (rol == Rol.CLIENTE) sesion.leerIdUsuario() else null
                ).filter { it.instalacionId == visita.instalacionId }
                _estado.update {
                    it.copy(
                        cargando = false,
                        visita = visita,
                        equipos = listaEquipos,
                        error = null
                    )
                }
            } catch (e: ErrorApi) {
                _estado.update { it.copy(cargando = false, error = e.message) }
            }
        }
    }

    fun eliminar() {
        if (_estado.value.eliminando) return
        _estado.update { it.copy(eliminando = true, errorAccion = null) }
        viewModelScope.launch {
            try {
                visitas.eliminarVisita(idVisita, sesion.leerIdUsuario())
                _estado.update { it.copy(eliminando = false, eliminada = true) }
            } catch (e: ErrorApi) {
                _estado.update { it.copy(eliminando = false, errorAccion = e.message) }
            }
        }
    }

}