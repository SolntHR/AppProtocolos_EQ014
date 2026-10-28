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
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null) }
    }

    fun onRespuestaCambia(valor: String) {
        _uiState.update { it.copy(respuesta = valor, errorRespuesta = null) }
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

    fun onBuscarCuenta() {
        val correo = _uiState.value.correo
        Validaciones.errorCorreo(correo)?.let { error ->
            _uiState.update { it.copy(errorCorreo = error) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            val encontrado = usuarioRepository.obtenerPorCorreo(correo)
            val error = when {
                encontrado == null -> "No existe una cuenta con ese correo"
                !encontrado.activo -> "La cuenta está desactivada. Contacta al Jefe técnico"
                encontrado.preguntaSeguridad.isNullOrBlank() -> "La cuenta no tiene pregunta de seguridad. Contacta al Jefe técnico"
                else -> null
            }
            usuario = encontrado.takeIf { error == null }
            _uiState.update {
                if (error != null) {
                    it.copy(cargando = false, errorCorreo = error)
                } else {
                    it.copy(
                        cargando = false,
                        paso = PasoRecuperacion.PREGUNTA,
                        pregunta = encontrado?.preguntaSeguridad.orEmpty()
                    )
                }
            }
        }
    }

    fun onVerificarRespuesta() {
        val respuesta = _uiState.value.respuesta.trim()
        val esperada = usuario?.respuestaSeguridad.orEmpty()
        val error = when {
            respuesta.isEmpty() -> "Ingresa tu respuesta"
            !respuesta.equals(esperada.trim(), ignoreCase = true) -> "La respuesta no coincide"
            else -> null
        }
        _uiState.update {
            if (error != null) it.copy(errorRespuesta = error) else it.copy(paso = PasoRecuperacion.NUEVA_CONTRASENA)
        }
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
