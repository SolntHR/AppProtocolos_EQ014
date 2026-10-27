package com.example.appprotocolos_eq014.model

data class InicioUiState(
    val isLoading: Boolean = true,
    val rolUsuario: String = "",
    val equiposCliente: List<Equipo> = emptyList(),
    val busqueda: String = ""
)