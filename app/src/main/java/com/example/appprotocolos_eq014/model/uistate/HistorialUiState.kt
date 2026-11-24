package com.example.appprotocolos_eq014.model.uistate

import com.example.appprotocolos_eq014.model.catalogos.ResultadoPrueba

data class FilaHistorial(
    val numero: String,
    val fecha: String,
    val estado: String,
    val formalizado: Boolean,
    val resultado: ResultadoPrueba?,
    val detalle: String?
)

data class FilaComparacion(
    val numero: String,
    val fecha: String,
    val valor: String,
    val anterior: String?,
    val variacion: String?,
    val significativa: Boolean
)

data class HistorialUiState(
    val isLoading: Boolean = true,
    val tag: String = "",
    val descripcion: String = "",
    val esCliente: Boolean = false,
    val protocolos: List<FilaHistorial> = emptyList(),
    val comparacion: List<FilaComparacion> = emptyList(),
    val haySignificativa: Boolean = false,
    val error: String? = null
)