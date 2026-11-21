package com.example.appprotocolos_eq014.model.entidades

data class Instalacion (
    val id: Long = 0,
    val clienteId: Long,
    val nombre: String,
    val ubicacion: String? = null,
    val comunaId: Long,
    val observacionesAcceso: String? = null
)