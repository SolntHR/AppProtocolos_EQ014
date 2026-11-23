package com.example.appprotocolos_eq014.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.data.remote.ErrorApi
import com.example.appprotocolos_eq014.model.uistate.LoginUiState
import com.example.appprotocolos_eq014.repository.UsuarioRepository
import com.example.appprotocolos_eq014.util.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val guardarSesion: (idUsuario: Long, codigoRol: String) -> Unit
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null, loginExitoso = false) }
    }

    fun onContrasenaCambia(valor: String) {
        _uiState.update { it.copy(contrasena = valor, errorContrasena = null, loginExitoso = false) }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    fun validar(): Boolean {
        val estado = _uiState.value
        val errorCorreo = Validaciones.errorCorreo(estado.correo)
        val errorContrasena = Validaciones.errorObligatorio(estado.contrasena, "Ingresa tu contraseña")
        _uiState.update { it.copy(errorCorreo = errorCorreo, errorContrasena = errorContrasena) }
        return errorCorreo == null && errorContrasena == null
    }

    fun onIngresar() {
        if (_uiState.value.isLoading || !validar()) return
        val estado = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val usuario = usuarioRepository.login(estado.correo, estado.contrasena)
                guardarSesion(usuario.id.toLong(), usuario.rolCodigo)
                _uiState.update { it.copy(isLoading = false, loginExitoso = true) }
            } catch (e: ErrorApi) {
                _uiState.update { it.copy(isLoading = false, errorContrasena = e.message) }
            }
        }
    }
}