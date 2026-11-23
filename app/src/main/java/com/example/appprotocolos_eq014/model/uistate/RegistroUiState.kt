package com.example.appprotocolos_eq014.model.uistate

import com.example.appprotocolos_eq014.model.registro.DatosRegistro
import com.example.appprotocolos_eq014.model.registro.ErroresRegistro

data class RegistroUiState(
    val datos: DatosRegistro = DatosRegistro(),
    val errores: ErroresRegistro = ErroresRegistro(),
    val respuestaVisible: Boolean = false,
    val cargando: Boolean = false,
    val errorGeneral: String? = null,
    val correoCreado: String? = null
)
