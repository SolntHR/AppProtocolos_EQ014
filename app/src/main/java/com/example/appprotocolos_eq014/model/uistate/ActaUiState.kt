package com.example.appprotocolos_eq014.model.uistate

import com.example.appprotocolos_eq014.model.entidades.Visita

data class ActaUiState(
    val isLoading: Boolean = true,
    val acta: Visita? = null,
    val puedeRegistrarRecepcion: Boolean = false,
    val receptorNombre: String = "",
    val receptorRut: String = "",
    val receptorCargo: String = "",
    val fechaHora: String = "",
    val errorNombre: String? = null,
    val errorRut: String? = null,
    val errorCargo: String? = null,
    val errorGeneral: String? = null,
    val guardando: Boolean = false,
    val recienRegistrada: Boolean = false
)
