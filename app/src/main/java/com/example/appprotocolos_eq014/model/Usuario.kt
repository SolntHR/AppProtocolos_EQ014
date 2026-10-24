package com.example.appprotocolos_eq014.model

data class Usuario(
    val id: Long = 0,
    val rut: String,
    val nombre: String,
    val correo: String,
    val rolId: Long,
    val rolCodigo: String // Recordar que cargamos datitos de la tabla ROL en el Helper
)