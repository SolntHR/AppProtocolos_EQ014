package com.example.appprotocolos_eq014.model

data class Protocolo(
    val id: Long = 0,
    val numero: String,
    val equipoId: Long,
    val usuarioRegistraId: Long,
    val temperatura: Double?,
    val humedad: Double?,
    var estado: EstadoProtocolo // El Enum en vez del Int puro
)