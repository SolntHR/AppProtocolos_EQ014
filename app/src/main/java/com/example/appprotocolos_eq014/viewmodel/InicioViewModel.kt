package com.example.appprotocolos_eq014.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appprotocolos_eq014.data.SesionPreferencias
import com.example.appprotocolos_eq014.model.InicioUiState
import com.example.appprotocolos_eq014.repository.CatalogoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InicioViewModel(
    private val sesionPreferencias: SesionPreferencias,
    private val catalogoRepository: CatalogoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InicioUiState())
    val uiState: StateFlow<InicioUiState> = _uiState.asStateFlow()

    init {
        cargarDatosInicio()
    }

    private fun cargarDatosInicio() {
        val idUsuario = sesionPreferencias.leerIdUsuario()
        val rol = sesionPreferencias.leerRolUsuario() ?: ""

        _uiState.update { it.copy(rolUsuario = rol, isLoading = true) }

        viewModelScope.launch {
            when (rol) {
                "CLIENTE" -> {
                    // Llamamos a tu nueva función del repositorio
                    val equipos = catalogoRepository.obtenerEquiposPorCliente(idUsuario)
                    _uiState.update { it.copy(equiposCliente = equipos, isLoading = false) }
                }
                "TECNICO", "INGENIERO" -> {
                    // Más adelante agregaremos aquí la carga de Visitas
                    _uiState.update { it.copy(isLoading = false) }
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun onBusquedaCambia(query: String) {
        _uiState.update { it.copy(busqueda = query) }
        // Aquí podrías agregar lógica para filtrar la lista localmente
    }
}