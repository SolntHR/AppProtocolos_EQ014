package com.example.appprotocolos_eq014.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.data.SesionPreferencias
import com.example.appprotocolos_eq014.model.EditarPerfilUiState
import com.example.appprotocolos_eq014.repository.UsuarioRepository
import com.example.appprotocolos_eq014.util.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditarPerfilViewModel(
    private val sesionPreferencias: SesionPreferencias,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditarPerfilUiState())
    val uiState: StateFlow<EditarPerfilUiState> = _uiState.asStateFlow()

    private val idUsuario = sesionPreferencias.leerIdUsuario()

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        viewModelScope.launch {
            val usuario = usuarioRepository.obtenerPorId(idUsuario)
            if (usuario == null) {
                _uiState.update { it.copy(isLoading = false, errorGeneral = "No se encontró el usuario de la sesión") }
                return@launch
            }
            val esCliente = usuario.rolCodigo == "CLIENTE"
            _uiState.update {
                it.copy(
                    isLoading = false,
                    esCliente = esCliente,
                    puedeEditarDatosProtegidos = usuario.rolCodigo == "JEFE_TECNICO",
                    nombre = if (esCliente) usuario.contactoNombre.orEmpty() else usuario.nombre,
                    rut = usuario.rut,
                    rol = usuario.rolNombre,
                    empresa = usuario.nombre,
                    cargo = usuario.contactoCargo.orEmpty(),
                    telefono = usuario.telefono,
                    correo = usuario.correo
                )
            }
        }
    }

    fun onNombreCambia(valor: String) {
        _uiState.update { it.copy(nombre = valor, errorNombre = null) }
    }

    fun onRutCambia(valor: String) {
        _uiState.update { it.copy(rut = valor.uppercase(), errorRut = null) }
    }

    fun onCargoCambia(valor: String) {
        _uiState.update { it.copy(cargo = valor, errorCargo = null) }
    }

    fun onTelefonoCambia(valor: String) {
        _uiState.update { it.copy(telefono = valor, errorTelefono = null) }
    }

    fun onGuardar() {
        val estado = _uiState.value
        val protegidos = estado.puedeEditarDatosProtegidos

        val errorNombre = Validaciones.errorNombre(estado.nombre)
        val errorRut = if (protegidos) Validaciones.errorRut(estado.rut) else null
        val errorCargo = if (protegidos && estado.esCliente) {
            Validaciones.errorObligatorio(estado.cargo, "Ingresa el cargo")
        } else {
            null
        }
        val errorTelefono = Validaciones.errorTelefono(estado.telefono)

        if (listOf(errorNombre, errorRut, errorCargo, errorTelefono).any { it != null }) {
            _uiState.update {
                it.copy(
                    errorNombre = errorNombre,
                    errorRut = errorRut,
                    errorCargo = errorCargo,
                    errorTelefono = errorTelefono
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, errorGeneral = null) }
            val actualizado = usuarioRepository.actualizarPerfil(
                idUsuario = idUsuario,
                nombre = if (estado.esCliente) estado.empresa else estado.nombre.trim(),
                rut = estado.rut.trim(),
                telefono = estado.telefono.trim(),
                contactoNombre = if (estado.esCliente) estado.nombre.trim() else null,
                contactoCargo = if (estado.esCliente) estado.cargo.trim() else null
            )
            _uiState.update {
                if (actualizado) {
                    it.copy(guardando = false, guardadoExitoso = true)
                } else {
                    it.copy(guardando = false, errorGeneral = "No se pudo guardar el perfil")
                }
            }
        }
    }
}
