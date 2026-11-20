package com.example.appprotocolos_eq014.model

import com.example.appprotocolos_eq014.model.entidades.Prueba

data class ProtocoloActa(
    val numero: String,
    val idEquipo: Long,
    val tag: String,
    val descripcionEquipo: String,
    val estado: EstadoProtocolo,
    val pruebas: List<Prueba>
) {
    val resultadoGlobal: ResultadoPrueba?
        get() = pruebas.filter { it.realizada }.mapNotNull { it.resultado }.maxByOrNull { it.idDb }
}

data class RecepcionActa(
    val nombre: String,
    val rut: String,
    val cargo: String,
    val fechaHora: String
)

