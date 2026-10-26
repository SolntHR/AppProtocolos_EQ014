package com.example.appprotocolos_eq014.model

data class Usuario(
    val id: Int,
    val idRol: Int,
    val rut: String,
    val nombre: String,
    val correo: String,
    val telefono: String,
    val passwordHash: String,
    val activo: Boolean,
    val giro: String? = null,
    val contactoNombre: String? = null,
    val contactoCargo: String? = null,
    val preguntaSeguridad: String? = null,
    val respuestaSeguridad: String? = null
)