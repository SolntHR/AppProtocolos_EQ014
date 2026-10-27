package com.example.appprotocolos_eq014.model

import java.util.UUID

data class Protocolo(
    val id: String = UUID.randomUUID().toString(),
    val numero: String,
    val equipoId: Long,
    val usuarioRegistraId: Long,
    val temperatura: Double?,
    val humedad: Double?,
    var estado: EstadoProtocolo
)
