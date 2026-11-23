package com.example.appprotocolos_eq014.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.data.remote.ErrorApi
import com.example.appprotocolos_eq014.model.uistate.PasoRecuperacion
import com.example.appprotocolos_eq014.model.uistate.RecuperarUiState
import com.example.appprotocolos_eq014.repository.UsuarioRepository
import com.example.appprotocolos_eq014.util.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecuperarViewModel(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecuperarUiState())
    val uiState: StateFlow<RecuperarUiState> = _uiState.asStateFlow()

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null, errorGeneral = null) }
    }

    fun onPreguntaCambia(valor: String) {
        _uiState.update { it.copy(pregunta = valor, errorPregunta = null, errorGeneral = null) }
    }

    fun onRespuestaCambia(valor: String) {
        _uiState.update { it.copy(respuesta = valor, errorRespuesta = null, errorGeneral = null) }
    }

    fun validarVerificacion(): Boolean {
        val estado = _uiState.value
        val errorCorreo = Validaciones.errorCorreo(estado.correo)
        val errorPregunta = if (estado.pregunta == null) "Selecciona tu pregunta de seguridad" else null
        val errorRespuesta = Validaciones.errorObligatorio(estado.respuesta, "Ingresa tu respuesta")
        _uiState.update {
            it.copy(errorCorreo = errorCorreo, errorPregunta = errorPregunta, errorRespuesta = errorRespuesta)
        }
        return errorCorreo == null && errorPregunta == null && errorRespuesta == null
    }

    fun onVerificar() {
        if (_uiState.value.cargando || !validarVerificacion()) return
        val estado = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, errorGeneral = null) }
            try {
                usuarioRepository.verificarPregunta(estado.correo, checkNotNull(estado.pregunta), estado.respuesta)
                _uiState.update { it.copy(cargando = false, paso = PasoRecuperacion.NUEVA_CONTRASENA) }
            } catch (e: ErrorApi) {
                _uiState.update { it.copy(cargando = false, errorGeneral = e.message) }
            }
        }
    }

    fun onNuevaCambia(valor: String) {
        _uiState.update { it.copy(nuevaContrasena = valor, errorNueva = null) }
    }

    fun onConfirmacionCambia(valor: String) {
        _uiState.update { it.copy(confirmacion = valor, errorConfirmacion = null) }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    fun validarNuevaContrasena(): Boolean {
        val estado = _uiState.value
        val errorNueva = Validaciones.errorContrasena(estado.nuevaContrasena)
        val errorConfirmacion = when {
            estado.confirmacion.isEmpty() -> "Repite la nueva contraseña"
            estado.confirmacion != estado.nuevaContrasena -> "Las contraseñas no coinciden"
            else -> null
        }
        _uiState.update { it.copy(errorNueva = errorNueva, errorConfirmacion = errorConfirmacion) }
        return errorNueva == null && errorConfirmacion == null
    }

    fun onGuardarContrasena() {
        if (_uiState.value.cargando || !validarNuevaContrasena()) return
        val estado = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, errorGeneral = null) }
            try {
                usuarioRepository.restablecerConPregunta(
                    estado.correo, checkNotNull(estado.pregunta), estado.respuesta, estado.nuevaContrasena
                )
                _uiState.update { it.copy(cargando = false, paso = PasoRecuperacion.LISTO) }
            } catch (e: ErrorApi) {
                _uiState.update { it.copy(cargando = false, errorNueva = e.message) }
            }
        }
    }
}
