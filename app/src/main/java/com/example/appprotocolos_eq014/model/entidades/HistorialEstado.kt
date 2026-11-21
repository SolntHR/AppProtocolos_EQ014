package com.example.appprotocolos_eq014.model.entidades

import com.example.appprotocolos_eq014.model.catalogos.EstadoProtocolo

data class HistorialEstado(
    val anterior: EstadoProtocolo?,
    val nuevo: EstadoProtocolo,
    val usuario: String,
    val fechaHora:String,
    val motivo: String?
)