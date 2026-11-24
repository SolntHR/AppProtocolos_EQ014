package com.example.appprotocolos_eq014.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.data.remote.ErrorApi
import com.example.appprotocolos_eq014.model.uistate.FichaUsuarioUiState
import com.example.appprotocolos_eq014.model.uistate.RolPersonal
import com.example.appprotocolos_eq014.repository.UsuarioRepository
import com.example.appprotocolos_eq014.util.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FichaUsuarioViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val idUsuario: Long,
    private val idSesion: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(FichaUsuarioUiState(esNuevo = idUsuario == NUEVO))
    val uiState: StateFlow<FichaUsuarioUiState> = _uiState.asStateFlow()

    private var giro: String? = null

    init {
        if (idUsuario != NUEVO) cargarUsuario()
    }

    private fun cargarUsuario() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val usuario = usuarioRepository.obtenerPorId(idUsuario)
                val esCliente = usuario.rolCodigo == "CLIENTE"
                val rol = RolPersonal.entries.find { rol -> rol.codigo == usuario.rolCodigo }
                giro = usuario.giro
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        pendiente = usuario.rolCodigo == "SIN_ASIGNAR",
                        esCliente = esCliente,
                        rolPersonal = rol ?: RolPersonal.TECNICO,
                        rolOriginal = rol,
                        esMiCuenta = idUsuario == idSesion,
                        rolNombre = usuario.rolNombre,
                        nombre = if (esCliente) usuario.contactoNombre.orEmpty() else usuario.nombre,
                        rut = usuario.rut,
                        empresa = if (esCliente) usuario.nombre else "",
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

    private fun puedeCambiarPerfil(): Boolean = _uiState.value.esNuevo || _uiState.value.pendiente

    fun onTipoCambia(esCliente: Boolean) {
        if (!puedeCambiarPerfil()) return
        _uiState.update {
            it.copy(esCliente = esCliente, errorEmpresa = null, errorCargo = null)
        }
    }

    fun onRolCambia(rol: RolPersonal) {
        if (!_uiState.value.puedeEditarRol) return
        _uiState.update { it.copy(rolPersonal = rol, errorGeneral = null) }
    }

    fun onContrasenaJefeCambia(valor: String) {
        _uiState.update { it.copy(contrasenaJefe = valor, errorContrasenaJefe = null) }
    }

    fun onCancelarContrasena() {
        _uiState.update { it.copy(pedirContrasena = false, contrasenaJefe = "", errorContrasenaJefe = null) }
    }

    fun onConfirmarContrasena() {
        if (_uiState.value.contrasenaJefe.isBlank()) {
            _uiState.update { it.copy(errorContrasenaJefe = "Ingresa tu contraseña") }
            return
        }
        guardar()
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

    fun validar(): Boolean {
        val estado = _uiState.value
        val errorNombre = Validaciones.errorNombre(estado.nombre)
        val errorRut = Validaciones.errorRut(estado.rut)
        val errorEmpresa = if (estado.esCliente) Validaciones.errorObligatorio(estado.empresa, "Ingresa la empresa cliente") else null
        val errorCargo = if (estado.esCliente) Validaciones.errorObligatorio(estado.cargo, "Ingresa el cargo") else null
        val errorTelefono = Validaciones.errorTelefono(estado.telefono)
        val errorCorreo = if (estado.esNuevo) Validaciones.errorCorreo(estado.correo) else null
        _uiState.update {
            it.copy(
                errorNombre = errorNombre,
                errorRut = errorRut,
                errorEmpresa = errorEmpresa,
                errorCargo = errorCargo,
                errorTelefono = errorTelefono,
                errorCorreo = errorCorreo,
                errorGeneral = null
            )
        }
        return listOf(errorNombre, errorRut, errorEmpresa, errorCargo, errorTelefono, errorCorreo).all { it == null }
    }

    fun onGuardar() {
        if (_uiState.value.guardando || !validar()) return
        if (_uiState.value.cambiaRol) {
            _uiState.update { it.copy(pedirContrasena = true, contrasenaJefe = "", errorContrasenaJefe = null) }
            return
        }
        guardar()
    }

    private fun guardar() {
        if (_uiState.value.guardando) return
        val estado = _uiState.value
        val nombreRegistro = if (estado.esCliente) estado.empresa.trim() else estado.nombre.trim()
        val contactoNombre = if (estado.esCliente) estado.nombre.trim() else null
        val contactoCargo = if (estado.esCliente) estado.cargo.trim() else null
        val rolCodigo = if (estado.esCliente) "CLIENTE" else estado.rolPersonal.codigo
        val telefono = Validaciones.telefonoCompleto(estado.telefono)

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            try {
                if (estado.esNuevo) {
                    usuarioRepository.crearUsuario(
                        rolCodigo = rolCodigo,
                        nombre = nombreRegistro,
                        rut = estado.rut.trim(),
                        correoPersonal = estado.correo,
                        telefono = telefono,
                        contactoNombre = contactoNombre,
                        contactoCargo = contactoCargo
                    )
                } else {
                    if (estado.cambiaRol) {
                        val jefe = usuarioRepository.obtenerPorId(idSesion)
                        usuarioRepository.cambiarRol(idUsuario, rolCodigo, idSesion, jefe.correo, estado.contrasenaJefe)
                    }
                    usuarioRepository.actualizarPerfil(
                        idUsuario = idUsuario,
                        nombre = nombreRegistro,
                        rut = estado.rut.trim(),
                        telefono = telefono,
                        giro = giro,
                        contactoNombre = contactoNombre,
                        contactoCargo = contactoCargo
                    )
                }
                _uiState.update { it.copy(guardando = false, pedirContrasena = false, contrasenaJefe = "", guardadoExitoso = true) }
            } catch (e: ErrorApi) {
                if (e.codigo == 401) {
                    _uiState.update { it.copy(guardando = false, errorContrasenaJefe = e.message) }
                    return@launch
                }
                _uiState.update {
                    it.copy(
                        guardando = false,
                        pedirContrasena = false,
                        contrasenaJefe = "",
                        errorNombre = e.campos["nombre"] ?: it.errorNombre,
                        errorRut = e.campos["rut"] ?: it.errorRut,
                        errorTelefono = e.campos["telefono"] ?: it.errorTelefono,
                        errorCorreo = e.campos["correoPersonal"] ?: it.errorCorreo,
                        errorGeneral = e.message
                    )
                }
            }
        }
    }

    companion object {
        const val NUEVO = -1L
    }
}
