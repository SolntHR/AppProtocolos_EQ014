package com.example.appprotocolos_eq014.viewmodel

import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.data.SesionPreferencias
import com.example.appprotocolos_eq014.model.FormularioEquipoUiState
import com.example.appprotocolos_eq014.model.OpcionCatalogo
import com.example.appprotocolos_eq014.model.ReglasProtocolo
import com.example.appprotocolos_eq014.model.Rol
import com.example.appprotocolos_eq014.repository.EquipoAGuardar
import com.example.appprotocolos_eq014.repository.RegistroCatalogoRepository
import com.example.appprotocolos_eq014.util.ValidacionesCatalogo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val ARG_ID_INSTALACION = "idInstalacion"
const val SIN_INSTALACION = -1L

class FormularioEquipoViewModel(
    private val sesion: SesionPreferencias,
    private val repository: RegistroCatalogoRepository,
    private val idInstalacionInicial: Long
) : ViewModel() {
    private val _uiState = MutableStateFlow(FormularioEquipoUiState())
    val uiState: StateFlow<FormularioEquipoUiState> = _uiState.asStateFlow()

    init {
        cargar(idInstalacionInicial.takeIf { it != SIN_INSTALACION })
    }

    private fun cargar(idSeleccionar: Long?) {
        viewModelScope.launch {
            try {
                val instalaciones = repository.instalaciones()
                val tipos = repository.tiposEquipo()
                _uiState.update { s ->
                    s.copy(
                        cargando = false,
                        instalaciones = instalaciones,
                        tipos = tipos,
                        instalacion = instalaciones.firstOrNull { it.id == idSeleccionar } ?: s.instalacion,
                        instalacionFija = idInstalacionInicial != SIN_INSTALACION
                    )
                }
            } catch (e: SQLiteException) {
                _uiState.update { it.copy(cargando = false, error = "No se pudo cargar el catálogo: ${e.message}") }
            }
        }
    }

    fun onInstalacionCreada(idInstalacion: Long) = cargar(idInstalacion)

    fun onInstalacion(valor: OpcionCatalogo) = actualizar(ValidacionesCatalogo.CAMPO_INSTALACION) { it.copy(instalacion = valor) }
    fun onTipo(valor: OpcionCatalogo) = actualizar(ValidacionesCatalogo.CAMPO_TIPO) { it.copy(tipo = valor) }
    fun onTag(valor: String) = actualizar(ValidacionesCatalogo.CAMPO_TAG) { it.copy(tag = valor.uppercase()) }
    fun onMarca(valor: String) = _uiState.update { it.copy(marca = valor) }
    fun onModelo(valor: String) = _uiState.update { it.copy(modelo = valor) }
    fun onNumeroSerie(valor: String) = _uiState.update { it.copy(numeroSerie = valor) }
    fun onAnio(valor: String) = actualizar(ValidacionesCatalogo.CAMPO_ANIO) { it.copy(anio = valor.filter(Char::isDigit).take(4)) }
    fun onPotencia(valor: String) = actualizar(ValidacionesCatalogo.CAMPO_POTENCIA) { it.copy(potenciaKva = valor) }
    fun onTensiones(valor: String) = actualizar(ValidacionesCatalogo.CAMPO_TENSIONES) { it.copy(tensiones = valor) }
    fun onCorriente(valor: String) = actualizar(ValidacionesCatalogo.CAMPO_CORRIENTE) { it.copy(corriente = valor) }
    fun onMaxContactos(valor: String) = actualizar(ValidacionesCatalogo.CAMPO_CONTACTOS) { it.copy(maxContactos = valor) }
    fun onObservaciones(valor: String) = _uiState.update { it.copy(observaciones = valor) }

    private fun actualizar(campo: String, cambio: (FormularioEquipoUiState) -> FormularioEquipoUiState) {
        _uiState.update { cambio(it).copy(errores = it.errores - campo, equipoExistente = null, error = null) }
    }

    fun usarEquipoExistente() {
        val existente = _uiState.value.equipoExistente ?: return
        _uiState.update { it.copy(equipoGuardadoId = existente.id) }
    }

    fun guardar() {
        val s = _uiState.value
        if (!Rol.puedeCrearProtocolos(sesion.leerRolUsuario().orEmpty())) {
            _uiState.update { it.copy(error = "Tu perfil no puede registrar equipos.") }
            return
        }
        val errores = ValidacionesCatalogo.validarEquipo(s)
        if (errores.isNotEmpty()) {
            _uiState.update { it.copy(errores = errores, error = "Revisa los campos marcados en rojo.") }
            return
        }
        val instalacion = s.instalacion ?: return
        val tipo = s.tipo ?: return
        val tag = ValidacionesCatalogo.normalizarTag(s.tag)

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, error = null) }
            try {
                val existente = repository.buscarEquipo(instalacion.id, tag)
                if (existente != null) {
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            equipoExistente = existente,
                            errores = it.errores + (ValidacionesCatalogo.CAMPO_TAG to "Ya existe en esta instalación")
                        )
                    }
                    return@launch
                }
                val id = repository.crearEquipo(
                    EquipoAGuardar(
                        idInstalacion = instalacion.id,
                        idTipoEquipo = tipo.id,
                        tag = tag,
                        marca = s.marca.trim().ifBlank { null },
                        modelo = s.modelo.trim().ifBlank { null },
                        numeroSerie = s.numeroSerie.trim().ifBlank { null },
                        anio = s.anio.trim().toIntOrNull(),
                        potenciaKva = ReglasProtocolo.numero(s.potenciaKva),
                        tensiones = s.tensiones.trim().ifBlank { null },
                        corriente = ReglasProtocolo.numero(s.corriente),
                        maxContactosUohm = ReglasProtocolo.numero(s.maxContactos),
                        observaciones = s.observaciones.trim().ifBlank { null }
                    )
                )
                _uiState.update { it.copy(guardando = false, equipoGuardadoId = id) }
            } catch (e: SQLiteConstraintException) {
                _uiState.update { it.copy(guardando = false, error = "Ya existe un equipo con ese TAG en la instalación (RN-01).") }
            } catch (e: SQLiteException) {
                _uiState.update { it.copy(guardando = false, error = "No se pudo guardar: ${e.message}") }
            }
        }
    }
}
