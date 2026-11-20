package com.example.appprotocolos_eq014.model

import com.example.appprotocolos_eq014.model.catalogos.EstadoProtocolo
import com.example.appprotocolos_eq014.model.catalogos.ResultadoPrueba

data class ProtocoloResumen(
    val id: String,
    val numero: String,
    val fecha: String,
    val estado: EstadoProtocolo,
    val resultado: ResultadoPrueba?,
    val lectura1Min: Double?,
    val lectura10Min: Double?,
    val observaciones: String?
)
