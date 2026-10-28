package com.example.appprotocolos_eq014.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.data.SesionPreferencias
import com.example.appprotocolos_eq014.model.GestionUsuariosUiState
import com.example.appprotocolos_eq014.model.PestanaUsuarios
import com.example.appprotocolos_eq014.model.Usuario
import com.example.appprotocolos_eq014.model.UsuarioFila
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
        }
    }

    fun onPestanaCambia(pestana: PestanaUsuarios) {
        _uiState.update { it.copy(pestana = pestana) }
    }

    fun onCambiarActivo(fila: UsuarioFila) {
        if (fila.esUsuarioActual) return
        val nuevoEstado = !fila.activo
        viewModelScope.launch {
            if (usuarioRepository.cambiarActivo(fila.id, nuevoEstado)) {
                val primerNombre = fila.nombre.substringBefore(" ")
                cargarUsuarios()
                _uiState.update {
                    it.copy(mensaje = "$primerNombre quedó ${if (nuevoEstado) "activo" else "inactivo"}")
                }
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
            detalle = if (esCliente) {
                listOfNotNull(usuario.nombre, usuario.contactoCargo).joinToString(" · ")
            } else {
                usuario.rolNombre
            },
            activo = usuario.activo,
            esUsuarioActual = usuario.id.toLong() == idUsuarioActual,
            foto = fotoPerfilRepository.cargar(usuario.fotoPerfil)
        )
    }
}
