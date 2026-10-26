package com.example.appprotocolos_eq014.model

data class ProtocoloUiState(
    val temperatura: String = "",
    val humedad: String = "",
    val errorTemperatura: String? = null,
    val errorHumedad: String? = null,
    val guardando: Boolean = false,
    val guardadoExitoso: Boolean = false
)
