package com.example.appprotocolos_eq014.model.entidades

data class Usuario(
    val id: Int,
    val rolCodigo: String,
    val rolNombre: String = "",
    val rut: String,
    val nombre: String,
    val correo: String,
    val correoPersonal: String? = null,
    val telefono: String,
    val activo: Boolean,
    val tieneContrasena: Boolean = true,
    val giro: String? = null,
    val contactoNombre: String? = null,
    val contactoCargo: String? = null,
)