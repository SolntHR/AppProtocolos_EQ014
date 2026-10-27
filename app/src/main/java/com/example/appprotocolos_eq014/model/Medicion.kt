package com.example.appprotocolos_eq014.model

data class Medicion(
    val id: Long = 0,
    val pruebaId: Long,
    val parametroNombre: String,
    val valor: Double,
    val unidad: String
)