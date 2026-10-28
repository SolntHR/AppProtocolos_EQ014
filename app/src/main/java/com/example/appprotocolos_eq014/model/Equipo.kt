package com.example.appprotocolos_eq014.model

data class Equipo(
    val id: Long = 0,
    val tag: String,
    val tipoEquipoNombre: String,
    val marca: String?,
    val numeroSerie: String?,
    val instalacion: String = ""
)