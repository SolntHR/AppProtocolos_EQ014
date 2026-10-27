package com.example.appprotocolos_eq014.model

data class Visita(
    val id: Long = 0,
    val numeroOt: String,
    val fecha: String,
    val instalacionNombre: String,
    val receptorNombre: String? = null
)