package com.example.appprotocolos_eq014.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.model.FichaUsuarioUiState
import com.example.appprotocolos_eq014.model.RolPersonal
import com.example.appprotocolos_eq014.repository.UsuarioRepository
import com.example.appprotocolos_eq014.util.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FichaUsuarioViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val idUsuario: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(FichaUsuarioUiState(esNuevo = idUsuario == NUEVO))
    val uiState: StateFlow<FichaUsuarioUiState> = _uiState.asStateFlow()

    init {
        if (idUsuario != NUEVO) cargarUsuario()
    }

    private fun cargarUsuario() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val usuario = usuarioRepository.obtenerPorId(idUsuario)
            if (usuario == null) {
                _uiState.update { it.copy(isLoading = false, errorGeneral = "No se encontró el usuario") }
                return@launch
            }
            val esCliente = usuario.rolCodigo == "CLIENTE"
            _uiState.update {
                it.copy(
                    isLoading = false,
                    esCliente = esCliente,
                    rolPersonal = RolPersonal.entries.find { rol -> rol.codigo == usuario.rolCodigo } ?: RolPersonal.TECNICO,
                    rolNombre = usuario.rolNombre,
                    nombre = if (esCliente) usuario.contactoNombre.orEmpty() else usuario.nombre,
                    rut = usuario.rut,
                    empresa = if (esCliente) usuario.nombre else "",
                    cargo = usuario.contactoCargo.orEmpty(),
                    telefono = usuario.telefono,
                    correo = usuario.correo
                )
            }
        }
    }

    fun onTipoCambia(esCliente: Boolean) {
        if (!_uiState.value.esNuevo) return
        _uiState.update {
            it.copy(esCliente = esCliente, errorEmpresa = null, errorCargo = null)
        }
    }

    fun onRolCambia(rol: RolPersonal) {
        if (!_uiState.value.esNuevo) return
        _uiState.update { it.copy(rolPersonal = rol) }
    }

    fun onNombreCambia(valor: String) {
        _uiState.update { it.copy(nombre = valor, errorNombre = null) }
    }

    fun onRutCambia(valor: String) {
        _uiState.update { it.copy(rut = valor.uppercase(), errorRut = null) }
    }

    fun onEmpresaCambia(valor: String) {
        _uiState.update { it.copy(empresa = valor, errorEmpresa = null) }
    }

    fun onCargoCambia(valor: String) {
        _uiState.update { it.copy(cargo = valor, errorCargo = null) }
    }

    fun onTelefonoCambia(valor: String) {
        _uiState.update { it.copy(telefono = valor, errorTelefono = null) }
    }

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null) }
    }

    fun onGuardar() {
        val estado = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, errorGeneral = null) }

            val errorNombre = Validaciones.errorNombre(estado.nombre)
            val errorRut = Validaciones.errorRut(estado.rut)
                ?: if (usuarioRepository.existeRut(estado.rut, idUsuario)) "Ya existe un usuario con ese RUT" else null
            val errorEmpresa = if (estado.esCliente) {
                Validaciones.errorObligatorio(estado.empresa, "Ingresa la empresa cliente")
            } else {
                null
            }
            val errorCargo = if (estado.esCliente) {
                Validaciones.errorObligatorio(estado.cargo, "Ingresa el cargo")
            } else {
                null
            }
            val errorTelefono = Validaciones.errorTelefono(estado.telefono)
            val errorCorreo = if (estado.esNuevo) errorCorreoNuevo(estado.correo) else null

            val errores = listOf(errorNombre, errorRut, errorEmpresa, errorCargo, errorTelefono, errorCorreo)
            if (errores.any { it != null }) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        errorNombre = errorNombre,
                        errorRut = errorRut,
                        errorEmpresa = errorEmpresa,
                        errorCargo = errorCargo,
                        errorTelefono = errorTelefono,
                        errorCorreo = errorCorreo
                    )
                }
                return@launch
            }

            val nombreRegistro = if (estado.esCliente) estado.empresa.trim() else estado.nombre.trim()
            val contactoNombre = if (estado.esCliente) estado.nombre.trim() else null
            val contactoCargo = if (estado.esCliente) estado.cargo.trim() else null

            val exito = if (estado.esNuevo) {
                usuarioRepository.crearUsuario(
                    rolCodigo = if (estado.esCliente) "CLIENTE" else estado.rolPersonal.codigo,
                    nombre = nombreRegistro,
                    rut = estado.rut.trim(),
                    correo = estado.correo.trim(),
                    telefono = estado.telefono.trim(),
                    contactoNombre = contactoNombre,
                    contactoCargo = contactoCargo,
                    contrasenaInicial = CONTRASENA_INICIAL
                ) != -1L
            } else {
                usuarioRepository.actualizarPerfil(
                    idUsuario = idUsuario,
                    nombre = nombreRegistro,
                    rut = estado.rut.trim(),
                    telefono = estado.telefono.trim(),
                    contactoNombre = contactoNombre,
                    contactoCargo = contactoCargo
                )
            }

            _uiState.update {
                if (exito) {
                    it.copy(guardando = false, guardadoExitoso = true)
                } else {
                    it.copy(guardando = false, errorGeneral = "No se pudo guardar el usuario")
                }
            }
        }
    }

    private suspend fun errorCorreoNuevo(correo: String): String? = when {
        correo.isBlank() -> "Ingresa el correo electrónico"
        !Validaciones.correoValido(correo) -> "Correo inválido. Ejemplo: nombre@empresa.cl"
        usuarioRepository.existeCorreo(correo) -> "Ya existe una cuenta con ese correo"
        else -> null
    }

    companion object {
        const val NUEVO = -1L
        const val CONTRASENA_INICIAL = "123456"
    }
}
