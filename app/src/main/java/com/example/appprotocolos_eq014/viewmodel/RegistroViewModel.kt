package com.example.appprotocolos_eq014.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.data.remote.ErrorApi
import com.example.appprotocolos_eq014.model.registro.DatosRegistro
import com.example.appprotocolos_eq014.model.registro.ErroresRegistro
import com.example.appprotocolos_eq014.model.uistate.RegistroUiState
import com.example.appprotocolos_eq014.repository.RegistroUsuarioRepository
import com.example.appprotocolos_eq014.repository.ResultadoRegistroUsuario
import com.example.appprotocolos_eq014.repository.SolicitudRegistroUsuario
import com.example.appprotocolos_eq014.util.ValidacionesRegistro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegistroViewModel(private val repository: RegistroUsuarioRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState = _uiState.asStateFlow()
    private var validado = false

    private fun actualizarDatos(cambio: (DatosRegistro) -> DatosRegistro) {
        if (_uiState.value.cargando || _uiState.value.correoCreado != null) return
        _uiState.update {
            val datos = cambio(it.datos)
            it.copy(datos = datos, errores = if (validado) ValidacionesRegistro.validar(datos) else it.errores, errorGeneral = null)
        }
    }

    fun onNombreCambia(valor: String) = actualizarDatos { it.copy(nombre = valor) }
    fun onRutCambia(valor: String) = actualizarDatos { it.copy(rut = valor) }
    fun onTelefonoCambia(valor: String) = actualizarDatos { it.copy(telefono = valor) }
    fun onCorreoCambia(valor: String) = actualizarDatos { it.copy(correoPersonal = valor.trim()) }
    fun onPreguntaCambia(valor: String) = actualizarDatos { it.copy(preguntaSeguridad = valor, respuestaSeguridad = "") }
    fun onRespuestaCambia(valor: String) = actualizarDatos { it.copy(respuestaSeguridad = valor) }
    fun onAlternarRespuesta() = _uiState.update { it.copy(respuestaVisible = !it.respuestaVisible) }

    fun validar(): Boolean {
        validado = true
        val errores = ValidacionesRegistro.validar(_uiState.value.datos)
        _uiState.update { it.copy(errores = errores, errorGeneral = null) }
        return !errores.hayErrores
    }

    fun onRegistrar() {
        val estado = _uiState.value
        if (estado.cargando || estado.correoCreado != null || !validar()) return

        val datos = estado.datos
        val solicitud = SolicitudRegistroUsuario(
            nombre = ValidacionesRegistro.nombreNormalizado(datos.nombre),
            rut = ValidacionesRegistro.normalizarRut(datos.rut),
            telefono = ValidacionesRegistro.telefonoCompleto(datos.telefono),
            correoPersonal = datos.correoPersonal.trim(),
            preguntaSeguridad = checkNotNull(datos.preguntaSeguridad),
            respuestaSeguridad = datos.respuestaSeguridad.trim()
        )
        _uiState.update { it.copy(cargando = true, respuestaVisible = false) }
        viewModelScope.launch {
            try {
                when (val resultado = repository.registrar(solicitud)) {
                    is ResultadoRegistroUsuario.Creado -> _uiState.update {
                        it.copy(cargando = false, correoCreado = resultado.correoInterno, datos = DatosRegistro())
                    }
                }
            } catch (e: ErrorApi) {
                _uiState.update {
                    it.copy(cargando = false, errores = erroresDelServidor(e.campos), errorGeneral = e.message)
                }
            }
        }

    }

    private fun erroresDelServidor(campos: Map<String, String>) = ErroresRegistro(
        nombre = campos["nombre"],
        rut = campos["rut"],
        telefono = campos["telefono"],
        correoPersonal = campos["correoPersonal"],
        preguntaSeguridad = campos["preguntaSeguridad"],
        respuestaSeguridad = campos["respuestaSeguridad"]
    )
}
