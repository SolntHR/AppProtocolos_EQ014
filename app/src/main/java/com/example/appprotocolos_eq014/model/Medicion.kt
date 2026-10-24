package com.example.appprotocolos_eq014.model

data class Medicion(
    val id: Long = 0,
    val pruebaId: Long,
    val parametroNombre: String, // Ej: "Lectura 1 min, Lectura 10 min"
    val valor: Double,
    val unidad: String // Lo más fakin importante - Ej: "MΩ"
)