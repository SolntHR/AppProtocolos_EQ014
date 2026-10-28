package com.example.appprotocolos_eq014.model

data class EquipoDetalle(
    val id: Long,
    val tag: String,
    val tipo: String,
    val marca: String?,
    val modelo: String?,
    val numeroSerie: String?,
    val instalacion: String,
    val potenciaKva: Double?,
    val tensionKv: String?
)

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
