package com.example.appprotocolos_eq014.model.entidades

import com.example.appprotocolos_eq014.model.catalogos.EstadoProtocolo

data class Protocolo(
    val id: String = "",
    val numero: String = "",
    val visitaId: Long,
    val equipoId: Long,
    val usuarioRegistraId: Long = 0,
    val fechaHoraPrueba: String = "",
    val temperatura: Double? = null,
    val humedad: Double? = null,
    val descripcionMantenimiento: String? = null,
    val observaciones: String? = null,
    val estado: EstadoProtocolo = EstadoProtocolo.BORRADOR,

    val pruebas: List<Prueba> = emptyList()

){
    val esNuevo: Boolean get() = id.isBlank()
}
