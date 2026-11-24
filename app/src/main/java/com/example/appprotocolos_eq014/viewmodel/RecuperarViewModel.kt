package com.example.appprotocolos_eq014.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.data.remote.ErrorApi
import com.example.appprotocolos_eq014.model.uistate.MetodoRecuperacion
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

    fun onMetodoCambia(metodo: MetodoRecuperacion) {
        if (_uiState.value.cargando || _uiState.value.paso != PasoRecuperacion.VERIFICAR) return
        _uiState.update {
            it.copy(
                metodo = metodo,
                codigo = "",
                codigoEnviado = false,
                mensajeCodigo = null,
                errorCodigo = null,
                errorPregunta = null,
                errorRespuesta = null,
                errorGeneral = null
            )
        }
    }

    fun onCorreoCambia(valor: String) {
        _uiState.update {
            it.copy(
                correo = valor.trim(),
                codigo = "",
                codigoEnviado = false,
                mensajeCodigo = null,
                errorCorreo = null,
                errorCodigo = null,
                errorGeneral = null
            )
        }
    }

    fun onCodigoCambia(valor: String) {
        val codigo = valor.filter(Char::isDigit).take(LARGO_CODIGO)
        _uiState.update { it.copy(codigo = codigo, errorCodigo = null, errorGeneral = null) }
    }

    fun onEnviarCodigo() {
        val estado = _uiState.value
        if (estado.cargando || estado.metodo != MetodoRecuperacion.CORREO) return
        val errorCorreo = Validaciones.errorCorreo(estado.correo)
        if (errorCorreo != null) {
            _uiState.update { it.copy(errorCorreo = errorCorreo) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, errorGeneral = null, mensajeCodigo = null) }
            try {
                val mensaje = usuarioRepository.solicitarCodigo(estado.correo)
                _uiState.update {
                    it.copy(
                        cargando = false,
                        codigoEnviado = true,
                        codigo = "",
                        mensajeCodigo = mensaje
                    )
                }
            } catch (e: ErrorApi) {
                _uiState.update { it.copy(cargando = false, errorGeneral = e.message) }
            }
        }
    }

    fun onYaTengoCodigo() {
        val estado = _uiState.value
        if (estado.cargando || estado.metodo != MetodoRecuperacion.CORREO) return
        val errorCorreo = Validaciones.errorCorreo(estado.correo)
        if (errorCorreo != null) {
            _uiState.update { it.copy(errorCorreo = errorCorreo) }
            return
        }
        _uiState.update {
            it.copy(
                codigoEnviado = true,
                codigo = "",
                mensajeCodigo = "Ingresa el código de activación recibido en tu correo personal.",
                errorCodigo = null,
                errorGeneral = null
            )
        }
    }

    fun onVerificarCodigo() {
        val estado = _uiState.value
        if (estado.cargando || !estado.codigoEnviado) return
        if (estado.codigo.length != LARGO_CODIGO) {
            _uiState.update { it.copy(errorCodigo = "Ingresa los 6 dígitos del código") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, errorGeneral = null, errorCodigo = null) }
            try {
                usuarioRepository.verificarCodigo(estado.correo, estado.codigo)
                _uiState.update { it.copy(cargando = false, paso = PasoRecuperacion.NUEVA_CONTRASENA) }
            } catch (e: ErrorApi) {
                _uiState.update { it.copy(cargando = false, errorCodigo = e.message) }
            }
        }
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

    fun onVerificarPregunta() {
        val estado = _uiState.value
        if (estado.cargando || estado.metodo != MetodoRecuperacion.PREGUNTA || !validarVerificacion()) return
        val validado = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, errorGeneral = null) }
            try {
                usuarioRepository.verificarPregunta(
                    validado.correo,
                    checkNotNull(validado.pregunta),
                    validado.respuesta
                )
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
                if (estado.metodo == MetodoRecuperacion.CORREO) {
                    usuarioRepository.restablecerConCodigo(
                        estado.correo,
                        estado.codigo,
                        estado.nuevaContrasena
                    )
                } else {
                    usuarioRepository.restablecerConPregunta(
                        estado.correo,
                        checkNotNull(estado.pregunta),
                        estado.respuesta,
                        estado.nuevaContrasena
                    )
                }
                _uiState.update { it.copy(cargando = false, paso = PasoRecuperacion.LISTO) }
            } catch (e: ErrorApi) {
                _uiState.update { it.copy(cargando = false, errorNueva = e.message) }
            }
        }
    }

    private companion object {
        const val LARGO_CODIGO = 6
    }
}
