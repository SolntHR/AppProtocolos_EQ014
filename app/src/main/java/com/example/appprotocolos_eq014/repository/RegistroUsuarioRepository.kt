package com.example.appprotocolos_eq014.repository

class SolicitudRegistroUsuario(
    val nombre: String,
    val rut: String,
    val telefono: String,
    val correoPersonal: String,
    val preguntaSeguridad: String,
    val respuestaSeguridad: String
)

sealed interface ResultadoRegistroUsuario {
    data class Creado(val correoInterno: String) : ResultadoRegistroUsuario
}

interface RegistroUsuarioRepository {
    suspend fun registrar(solicitud: SolicitudRegistroUsuario): ResultadoRegistroUsuario
}

