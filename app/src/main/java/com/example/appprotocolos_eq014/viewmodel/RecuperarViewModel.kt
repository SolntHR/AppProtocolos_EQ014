package com.example.appprotocolos_eq014.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.model.PasoRecuperacion
import com.example.appprotocolos_eq014.model.RecuperarUiState
import com.example.appprotocolos_eq014.model.Usuario
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

    private var usuario: Usuario? = null

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null, errorGeneral = null) }
    }

    fun onPreguntaCambia(valor: String) {
        _uiState.update { it.copy(pregunta = valor, errorPregunta = null, errorGeneral = null) }
    }

    fun onRespuestaCambia(valor: String) {
        _uiState.update { it.copy(respuesta = valor, errorRespuesta = null, errorGeneral = null) }
    }

    fun onVerificar() {
        val estado = _uiState.value
        val errorCorreo = Validaciones.errorCorreo(estado.correo)
        val errorPregunta = if (estado.pregunta == null) "Selecciona tu pregunta de seguridad" else null
        val errorRespuesta = Validaciones.errorObligatorio(estado.respuesta, "Ingresa tu respuesta")
        if (errorCorreo != null || errorPregunta != null || errorRespuesta != null) {
            _uiState.update {
                it.copy(errorCorreo = errorCorreo, errorPregunta = errorPregunta, errorRespuesta = errorRespuesta)
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, errorGeneral = null) }
            val encontrado = usuarioRepository.obtenerPorCorreo(estado.correo)
            val coincide = encontrado != null &&
                    encontrado.preguntaSeguridad?.trim() == estado.pregunta?.trim() &&
                    encontrado.respuestaSeguridad?.trim().equals(estado.respuesta.trim(), ignoreCase = true)
            when {
                !coincide -> _uiState.update {
                    it.copy(cargando = false, errorGeneral = "Los datos no coinciden con ninguna cuenta. Revisa el correo, la pregunta y la respuesta.")
                }
                encontrado?.activo != true -> _uiState.update {
                    it.copy(cargando = false, errorGeneral = "La cuenta está desactivada. Contacta al Jefe técnico.")
                }
                else -> {
                    usuario = encontrado
                    _uiState.update { it.copy(cargando = false, paso = PasoRecuperacion.NUEVA_CONTRASENA) }
                }
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

    fun onGuardarContrasena() {
        val estado = _uiState.value
        val errorNueva = Validaciones.errorContrasena(estado.nuevaContrasena)
        val errorConfirmacion = when {
            estado.confirmacion.isEmpty() -> "Repite la nueva contraseña"
            estado.confirmacion != estado.nuevaContrasena -> "Las contraseñas no coinciden"
            else -> null
        }
        if (errorNueva != null || errorConfirmacion != null) {
            _uiState.update { it.copy(errorNueva = errorNueva, errorConfirmacion = errorConfirmacion) }
            return
        }
        val idUsuario = usuario?.id?.toLong() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            val guardada = usuarioRepository.actualizarContrasena(idUsuario, estado.nuevaContrasena)
            _uiState.update {
                if (guardada) {
                    it.copy(cargando = false, paso = PasoRecuperacion.LISTO)
                } else {
                    it.copy(cargando = false, errorNueva = "No se pudo actualizar la contraseña")
                }
            }
        }
    }
}
