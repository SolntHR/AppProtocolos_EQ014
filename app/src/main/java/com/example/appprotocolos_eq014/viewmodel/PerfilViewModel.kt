package com.example.appprotocolos_eq014.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.data.SesionPreferencias
import com.example.appprotocolos_eq014.data.remote.ErrorApi
import com.example.appprotocolos_eq014.model.uistate.DatoPerfil
import com.example.appprotocolos_eq014.model.uistate.PerfilUiState
import com.example.appprotocolos_eq014.model.entidades.Usuario
import com.example.appprotocolos_eq014.repository.FotoPerfilRepository
import com.example.appprotocolos_eq014.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PerfilViewModel(
    private val sesionPreferencias: SesionPreferencias,
    private val usuarioRepository: UsuarioRepository,
    private val fotoPerfilRepository: FotoPerfilRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    private val idUsuario = sesionPreferencias.leerIdUsuario()
    private var rutaFotoActual: String? = null

    init {
        cargarPerfil()
    }

    fun cargarPerfil() {
        _uiState.update { it.copy(isLoading = it.nombre.isEmpty(), error = null) }

        viewModelScope.launch {
            try {
                val usuario = usuarioRepository.obtenerPorId(idUsuario)
                rutaFotoActual = fotoPerfilRepository.rutaDe(idUsuario)
                val foto = fotoPerfilRepository.cargar(rutaFotoActual)
                _uiState.update { construirEstado(usuario, it).copy(foto = foto) }
            } catch (e: ErrorApi) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onPerfilActualizado() {
        cargarPerfil()
        _uiState.update { it.copy(mensaje = R.string.mensaje_perfil_actualizado) }
    }

    fun onFotoCapturada(foto: Bitmap?) {
        if (foto != null) {
            _uiState.update { it.copy(fotoPendiente = foto) }
        }
    }

    fun onFotoElegida(uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            _uiState.update { it.copy(procesandoFoto = true) }
            val foto = fotoPerfilRepository.leerDesdeGaleria(uri)
            _uiState.update { it.copy(procesandoFoto = false, fotoPendiente = foto) }
        }
    }

    fun onUsarFoto() {
        val foto = _uiState.value.fotoPendiente ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(procesandoFoto = true) }
            rutaFotoActual = fotoPerfilRepository.guardar(idUsuario, foto)
            _uiState.update {
                it.copy(
                    foto = foto,
                    fotoPendiente = null,
                    procesandoFoto = false,
                    mensaje = R.string.mensaje_foto_actualizada
                )
            }
        }
    }

    fun onCancelarFoto() {
        _uiState.update { it.copy(fotoPendiente = null) }
    }

    fun onEliminarFoto() {
        viewModelScope.launch {
            fotoPerfilRepository.eliminar(rutaFotoActual)
            rutaFotoActual = null
            _uiState.update { it.copy(foto = null, mensaje = R.string.mensaje_foto_eliminada) }
        }
    }

    fun onPermisoCamaraDenegado() {
        _uiState.update { it.copy(mensaje = R.string.mensaje_permiso_camara) }
    }

    fun onMensajeMostrado() {
        _uiState.update { it.copy(mensaje = null) }
    }

    fun onCerrarSesion() {
        sesionPreferencias.cerrarSesion()
        _uiState.update { it.copy(sesionCerrada = true) }
    }

    private fun construirEstado(usuario: Usuario, actual: PerfilUiState): PerfilUiState {
        val esCliente = usuario.rolCodigo == "CLIENTE"
        val nombrePersona = if (esCliente) usuario.contactoNombre ?: usuario.nombre else usuario.nombre

        val datos = if (esCliente) {
            listOf(
                DatoPerfil("Nombre", nombrePersona),
                DatoPerfil("RUT", usuario.rut),
                DatoPerfil("Empresa cliente", usuario.nombre),
                DatoPerfil("Cargo", usuario.contactoCargo.orEmpty()),
                DatoPerfil("Correo", usuario.correo),
                DatoPerfil("Teléfono", usuario.telefono)
            )
        } else {
            listOf(
                DatoPerfil("Nombre", usuario.nombre),
                DatoPerfil("RUT", usuario.rut),
                DatoPerfil("Perfil", usuario.rolNombre),
                DatoPerfil("Correo", usuario.correo),
                DatoPerfil("Teléfono", usuario.telefono)
            )
        }

        return actual.copy(
            isLoading = false,
            nombre = nombrePersona,
            correo = usuario.correo,
            rol = usuario.rolNombre,
            esCliente = esCliente,
            esJefeTecnico = usuario.rolCodigo == "JEFE_TECNICO",
            datos = datos
        )
    }
}
