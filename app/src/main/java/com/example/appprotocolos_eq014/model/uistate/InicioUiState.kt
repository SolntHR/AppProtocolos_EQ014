package com.example.appprotocolos_eq014.model.uistate

import com.example.appprotocolos_eq014.model.entidades.Visita

data class EquipoCliente(
    val id: Long,
    val tag: String,
    val descripcion: String
)

data class InicioUiState(
    val isLoading: Boolean = true,
    val rolUsuario: String = "",
    val rolNombre: String = "",
    val primerNombre: String = "",
    val nombreCompleto: String = "",
    val empresa: String = "",
    val fechaHoy: String = "",
    val equiposCliente: List<EquipoCliente> = emptyList(),
    val visitas: List<Visita> = emptyList(),
    val actasCliente: List<Visita> = emptyList(),
    val usuariosPendientes: Int = 0,
    val error: String? = null
)
