package com.example.appprotocolos_eq014.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.data.SesionPreferencias
import com.example.appprotocolos_eq014.data.remote.ErrorApi
import com.example.appprotocolos_eq014.model.entidades.Usuario
import com.example.appprotocolos_eq014.model.uistate.GestionUsuariosUiState
import com.example.appprotocolos_eq014.model.uistate.PestanaUsuarios
import com.example.appprotocolos_eq014.model.uistate.UsuarioFila
import com.example.appprotocolos_eq014.repository.FotoPerfilRepository
import com.example.appprotocolos_eq014.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GestionUsuariosViewModel(
    sesionPreferencias: SesionPreferencias,
    private val usuarioRepository: UsuarioRepository,
    private val fotoPerfilRepository: FotoPerfilRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GestionUsuariosUiState())
    val uiState: StateFlow<GestionUsuariosUiState> = _uiState.asStateFlow()

    private val idUsuarioActual = sesionPreferencias.leerIdUsuario()

    init {
        cargarUsuarios()
    }

    fun cargarUsuarios() {
        viewModelScope.launch {
            try {
                val usuarios = usuarioRepository.listarUsuarios()
                val filas = usuarios.associateWith { aFila(it) }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        personal = usuarios.filter { u -> u.rolCodigo != "CLIENTE" }.map { u -> filas.getValue(u) },
                        clientes = usuarios.filter { u -> u.rolCodigo == "CLIENTE" }.map { u -> filas.getValue(u) },
                        activos = usuarios.count { u -> u.activo },
                        inactivos = usuarios.count { u -> !u.activo }
                    )
                }
            } catch (e: ErrorApi) {
                _uiState.update { it.copy(isLoading = false, mensaje = e.message) }
            }
        }
    }

    fun onPestanaCambia(pestana: PestanaUsuarios) {
        _uiState.update { it.copy(pestana = pestana) }
    }

    fun onCambiarActivo(fila: UsuarioFila) {
        if (fila.esUsuarioActual) return
        val nuevoEstado = !fila.activo
        viewModelScope.launch {
            try {
                usuarioRepository.cambiarActivo(fila.id, nuevoEstado)
                val primerNombre = fila.nombre.substringBefore(" ")
                cargarUsuarios()
                _uiState.update {
                    it.copy(mensaje = "$primerNombre quedó ${if (nuevoEstado) "activo" else "inactivo"}")
                }
            } catch (e: ErrorApi) {
                _uiState.update { it.copy(mensaje = e.message) }
            }
        }
    }

    fun onUsuarioGuardado(esNuevo: Boolean) {
        cargarUsuarios()
        _uiState.update { it.copy(mensaje = if (esNuevo) "Usuario agregado" else "Usuario actualizado") }
    }

    fun onMensajeMostrado() {
        _uiState.update { it.copy(mensaje = null) }
    }

    private suspend fun aFila(usuario: Usuario): UsuarioFila {
        val esCliente = usuario.rolCodigo == "CLIENTE"
        return UsuarioFila(
            id = usuario.id.toLong(),
            nombre = if (esCliente) usuario.contactoNombre ?: usuario.nombre else usuario.nombre,
            detalle = when {
                esCliente -> listOfNotNull(usuario.nombre, usuario.contactoCargo).joinToString(" · ")
                usuario.rolCodigo == "SIN_ASIGNAR" -> "Pendiente: asignar perfil"
                else -> usuario.rolNombre
            },
            activo = usuario.activo,
            esUsuarioActual = usuario.id.toLong() == idUsuarioActual,
            foto = fotoPerfilRepository.cargar(fotoPerfilRepository.rutaDe(usuario.id.toLong()))
        )
    }
}