package com.example.appprotocolos_eq014.model

data class Prueba(
    val id: Long = 0,
    val protocoloId: Long,
    val tipoPruebaNombre: String, // Datos precargados en el helper
    val elemento: String,
    var realizada: Boolean = true,
    var resultado: ResultadoPrueba? = null
)