package com.example.appprotocolos_eq014.viewmodel

import android.database.sqlite.SQLiteException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.data.SesionPreferencias
import com.example.appprotocolos_eq014.model.FormularioInstalacionUiState
import com.example.appprotocolos_eq014.model.OpcionCatalogo
import com.example.appprotocolos_eq014.model.Rol
import com.example.appprotocolos_eq014.repository.InstalacionAGuardar
import com.example.appprotocolos_eq014.repository.RegistroCatalogoRepository
import com.example.appprotocolos_eq014.util.ValidacionesCatalogo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// RF-01: registrar una instalacion (planta, subestacion o faena) de un cliente
class FormularioInstalacionViewModel(
    private val sesion: SesionPreferencias,
    private val repository: RegistroCatalogoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FormularioInstalacionUiState())
    val uiState: StateFlow<FormularioInstalacionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(cargando = false, clientes = repository.clientes(), comunas = repository.comunas())
                }
            } catch (e: SQLiteException) {
                _uiState.update { it.copy(cargando = false, error = "No se pudo cargar el catálogo: ${e.message}") }
            }
        }
    }

    fun onCliente(valor: OpcionCatalogo) = actualizar(ValidacionesCatalogo.CAMPO_CLIENTE) { it.copy(cliente = valor) }
    fun onComuna(valor: OpcionCatalogo) = actualizar(ValidacionesCatalogo.CAMPO_COMUNA) { it.copy(comuna = valor) }
    fun onNombre(valor: String) = actualizar(ValidacionesCatalogo.CAMPO_NOMBRE) { it.copy(nombre = valor) }
    fun onUbicacion(valor: String) = _uiState.update { it.copy(ubicacion = valor) }
    fun onObservacionesAcceso(valor: String) = _uiState.update { it.copy(observacionesAcceso = valor) }

    private fun actualizar(campo: String, cambio: (FormularioInstalacionUiState) -> FormularioInstalacionUiState) {
        _uiState.update { cambio(it).copy(errores = it.errores - campo, error = null) }
    }

    fun guardar() {
        val s = _uiState.value
        if (!Rol.puedeCrearProtocolos(sesion.leerRolUsuario().orEmpty())) {
            _uiState.update { it.copy(error = "Tu perfil no puede registrar instalaciones.") }
            return
        }
        val errores = ValidacionesCatalogo.validarInstalacion(s)
        if (errores.isNotEmpty()) {
            _uiState.update { it.copy(errores = errores, error = "Revisa los campos marcados en rojo.") }
            return
        }
        val cliente = s.cliente ?: return
        val comuna = s.comuna ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            try {
                if (repository.existeInstalacion(cliente.id, s.nombre)) {
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            errores = it.errores + (ValidacionesCatalogo.CAMPO_NOMBRE to "Este cliente ya tiene una instalación con ese nombre")
                        )
                    }
                    return@launch
                }
                val id = repository.crearInstalacion(
                    InstalacionAGuardar(
                        idCliente = cliente.id,
                        nombre = s.nombre.trim(),
                        ubicacion = s.ubicacion.trim().ifBlank { null },
                        idComuna = comuna.id,
                        observacionesAcceso = s.observacionesAcceso.trim().ifBlank { null }
                    )
                )
                _uiState.update { it.copy(guardando = false, instalacionGuardadaId = id) }
            } catch (e: SQLiteException) {
                _uiState.update { it.copy(guardando = false, error = "No se pudo guardar: ${e.message}") }
            }
        }
    }
}
