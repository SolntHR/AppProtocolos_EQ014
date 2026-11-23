package com.example.appprotocolos_eq014.repository

/** Solicitud para el futuro servicio de usuarios. El rol y las credenciales los decide el servidor. */
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
    data object NoDisponible : ResultadoRegistroUsuario
}

interface RegistroUsuarioRepository {
    suspend fun registrar(solicitud: SolicitudRegistroUsuario): ResultadoRegistroUsuario
}

/** Sustituir en AppViewModelFactory por el adaptador HTTP cuando exista el contrato del servicio. */
class RegistroUsuarioNoDisponibleRepository : RegistroUsuarioRepository {
    override suspend fun registrar(solicitud: SolicitudRegistroUsuario): ResultadoRegistroUsuario =
        ResultadoRegistroUsuario.NoDisponible
}
