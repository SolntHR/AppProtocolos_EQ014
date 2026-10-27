package com.example.appprotocolos_eq014.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.data.SesionPreferencias
import com.example.appprotocolos_eq014.model.DatoPerfil
import com.example.appprotocolos_eq014.model.PerfilUiState
import com.example.appprotocolos_eq014.model.Usuario
import com.example.appprotocolos_eq014.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PerfilViewModel(
    private val sesionPreferencias: SesionPreferencias,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        cargarPerfil()
    }

    fun cargarPerfil() {
        val idUsuario = sesionPreferencias.leerIdUsuario()
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val usuario = usuarioRepository.obtenerPorId(idUsuario)
            if (usuario == null) {
                _uiState.update { it.copy(isLoading = false, error = "No se encontró el usuario de la sesión") }
            } else {
                _uiState.update { construirEstado(usuario) }
            }
        }
    }

    fun onCerrarSesion() {
        sesionPreferencias.cerrarSesion()
        _uiState.update { it.copy(sesionCerrada = true) }
    }

    private fun construirEstado(usuario: Usuario): PerfilUiState {
        val esCliente = usuario.rolCodigo == "CLIENTE"
        val nombrePersona = if (esCliente) usuario.contactoNombre ?: usuario.nombre else usuario.nombre

        val datos = if (esCliente) {
            listOf(
                DatoPerfil("Nombre", nombrePersona),
                DatoPerfil("RUT", usuario.rut),
                DatoPerfil("Empresa cliente", usuario.nombre),
                DatoPerfil("Cargo", usuario.contactoCargo.orEmpty()),
                DatoPerfil("Correo", usuario.correo),
                DatoPerfil("Teléfono", usuario.telefono)
            )
        } else {
            listOf(
                DatoPerfil("Nombre", usuario.nombre),
                DatoPerfil("RUT", usuario.rut),
                DatoPerfil("Perfil", usuario.rolNombre),
                DatoPerfil("Correo", usuario.correo),
                DatoPerfil("Teléfono", usuario.telefono)
            )
        }

        return PerfilUiState(
            isLoading = false,
            nombre = nombrePersona,
            correo = usuario.correo,
            rol = usuario.rolNombre,
            esCliente = esCliente,
            esJefeTecnico = usuario.rolCodigo == "JEFE_TECNICO",
            datos = datos
        )
    }
}
