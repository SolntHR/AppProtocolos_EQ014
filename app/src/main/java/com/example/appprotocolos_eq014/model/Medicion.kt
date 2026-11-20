package com.example.appprotocolos_eq014.model

data class Medicion(
    val id: Long = 0,
    val pruebaId: Long = 0,
    val parametroId: Long = 0,
    val elemento: String? = null,
    val valor: Double,
    val unidad: String,
    val orden: Int = 0,

    val parametroCodigo: String = "",
    val parametroNombre: String = ""
)