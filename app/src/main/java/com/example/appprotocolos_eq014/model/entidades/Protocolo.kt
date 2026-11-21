package com.example.appprotocolos_eq014.model.entidades

import com.example.appprotocolos_eq014.model.catalogos.EstadoProtocolo
import com.example.appprotocolos_eq014.model.catalogos.ResultadoPrueba

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

    val tagEquipo: String = "",
    val equipoDescripcion: String = "",

    val resultadoGlobal: ResultadoPrueba? = null,
    val lectura1Min: Double? = null,
    val lectura10Min: Double? = null,

    val pruebas: List<Prueba> = emptyList()

){
    val esNuevo: Boolean get() = id.isBlank()
}
