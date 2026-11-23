package com.example.appprotocolos_eq014.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.data.SesionPreferencias
import com.example.appprotocolos_eq014.data.remote.ErrorApi
import com.example.appprotocolos_eq014.model.uistate.EditarPerfilUiState
import com.example.appprotocolos_eq014.repository.UsuarioRepository
import com.example.appprotocolos_eq014.util.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class EditarPerfilViewModel(
    sesionPreferencias: SesionPreferencias,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditarPerfilUiState())
    val uiState: StateFlow<EditarPerfilUiState> = _uiState.asStateFlow()

    private val idUsuario = sesionPreferencias.leerIdUsuario()
    private var giro: String? = null

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        viewModelScope.launch {
            try {
                val usuario = usuarioRepository.obtenerPorId(idUsuario)
                val esCliente = usuario.rolCodigo == "CLIENTE"
                giro = usuario.giro
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
            } catch (e: ErrorApi) {
                _uiState.update { it.copy(isLoading = false, errorGeneral = e.message) }
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

    fun validar(): Boolean {
        val estado = _uiState.value
        val protegidos = estado.puedeEditarDatosProtegidos
        val errorNombre = Validaciones.errorNombre(estado.nombre)
        val errorRut = if (protegidos) Validaciones.errorRut(estado.rut) else null
        val errorCargo = if (protegidos && estado.esCliente) Validaciones.errorObligatorio(estado.cargo, "Ingresa el cargo") else null
        val errorTelefono = Validaciones.errorTelefono(estado.telefono)
        _uiState.update {
            it.copy(errorNombre = errorNombre, errorRut = errorRut, errorCargo = errorCargo, errorTelefono = errorTelefono)
        }
        return listOf(errorNombre, errorRut, errorCargo, errorTelefono).all { it == null }
    }

    fun onGuardar() {
        if (_uiState.value.guardando || !validar()) return
        val estado = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, errorGeneral = null) }
            try {
                usuarioRepository.actualizarPerfil(
                    idUsuario = idUsuario,
                    nombre = if (estado.esCliente) estado.empresa else estado.nombre.trim(),
                    rut = estado.rut.trim(),
                    telefono = Validaciones.telefonoCompleto(estado.telefono),
                    giro = giro,
                    contactoNombre = if (estado.esCliente) estado.nombre.trim() else null,
                    contactoCargo = if (estado.esCliente) estado.cargo.trim() else null
                )
                _uiState.update { it.copy(guardando = false, guardadoExitoso = true) }
            } catch (e: ErrorApi) {
                _uiState.update { it.copy(guardando = false, errorGeneral = e.message) }
            }
        }
    }
}