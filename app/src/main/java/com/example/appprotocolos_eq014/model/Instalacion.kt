package com.example.appprotocolos_eq014.model

data class Instalacion (
    val id: Long = 0,
    val nombre: String,
    val ubicacion: String,
    val clienteId: Long
)