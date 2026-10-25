package com.example.appprotocolos_eq014.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appprotocolos_eq014.model.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel : ViewModel() {
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

    fun onIngresar(): Boolean {
        val estado = _uiState.value

        val errorCorreo = if (!estado.correo.contains("@") || !estado.correo.contains("."))
            "Ingresa un correo válido" else null
        val errorContrasena = if (estado.contrasena.isEmpty())
            "La contraseña es obligatoria" else null

        val hayErrores = listOf(errorCorreo, errorContrasena).any { it != null }

        _uiState.update {
            it.copy(
                errorCorreo = errorCorreo,
                errorContrasena = errorContrasena,
                loginExitoso = !hayErrores
            )
        }

        return !hayErrores
    }

    fun onLimpiar() {
        _uiState.value = LoginUiState()
    }
}