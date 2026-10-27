package com.example.appprotocolos_eq014.model

data class DatoPerfil(
    val etiqueta: String,
    val valor: String
)

data class PerfilUiState(
    val isLoading: Boolean = true,
    val nombre: String = "",
    val correo: String = "",
    val rol: String = "",
    val esCliente: Boolean = false,
    val esJefeTecnico: Boolean = false,
    val datos: List<DatoPerfil> = emptyList(),
    val error: String? = null,
    val sesionCerrada: Boolean = false
)
