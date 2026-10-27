package com.example.appprotocolos_eq014.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.data.SesionPreferencias
import com.example.appprotocolos_eq014.model.LoginUiState
import com.example.appprotocolos_eq014.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val sesionPreferencias: SesionPreferencias
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

    // Evento de inicio de sesión
    fun onIngresar() {
        val estado = _uiState.value

        val errorCorreo = if (!estado.correo.contains("@") || !estado.correo.contains("."))
            "Ingresa un correo válido" else null
        val errorContrasena = if (estado.contrasena.isEmpty())
            "La contraseña es obligatoria" else null

        if (errorCorreo != null || errorContrasena != null) {
            _uiState.update { it.copy(errorCorreo = errorCorreo, errorContrasena = errorContrasena) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorCorreo = null, errorContrasena = null) }
            val usuario = usuarioRepository.login(estado.correo, estado.contrasena)
            if (usuario != null) {
                if (usuario.activo) {
                    // Login exitoso: Guardamos en preferencias
                    sesionPreferencias.guardarSesion(
                        idUsuario = usuario.id.toLong(),
                        codigoRol = usuario.rolCodigo
                    )
                    _uiState.update { it.copy(isLoading = false, loginExitoso = true) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorCorreo = "Usuario desactivado") }
                }
            } else {
                // Credenciales incorrectas
                _uiState.update { it.copy(isLoading = false, errorContrasena = "Correo o contraseña incorrectos") }
            }
            }
        }

    fun onLimpiar() {
        _uiState.value = LoginUiState()
    }
}