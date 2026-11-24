package com.example.appprotocolos_eq014.data.remote

import com.example.appprotocolos_eq014.model.entidades.Usuario

data class LoginRequest(
    val correo: String,
    val contrasena: String
)

data class RegistroRequest(
    val nombre: String,
    val rut: String,
    val correoPersonal: String,
    val telefono: String,
    val preguntaSeguridad: String,
    val respuestaSeguridad: String
)

data class RegistroResponse(
    val id: Long,
    val correoInterno: String,
    val estado: String,
    val mensaje: String
)

data class CrearUsuarioRequest(
    val rol: String,
    val nombre: String,
    val rut: String,
    val correoPersonal: String,
    val telefono: String,
    val giro: String?,
    val contactoNombre: String?,
    val contactoCargo: String?
)

data class ActualizarUsuarioRequest(
    val nombre: String,
    val rut: String,
    val telefono: String,
    val giro: String?,
    val contactoNombre: String?,
    val contactoCargo: String?
)

data class CambiarRolRequest(val rol: String, val jefeId: Long, val contrasena: String)

data class CambiarActivoRequest(val activo: Boolean)

data class CambiarContrasenaRequest(
    val actual: String,
    val nueva: String
)

data class VerificarPreguntaRequest(
    val correo: String,
    val pregunta: String,
    val respuesta: String
)

data class RestablecerConPreguntaRequest(
    val correo: String,
    val pregunta: String,
    val respuesta: String,
    val nuevaContrasena: String
)

data class SolicitarCodigoRequest(val correo: String)

data class VerificarCodigoRequest(
    val correo: String,
    val codigo: String
)

data class RestablecerConCodigoRequest(
    val correo: String,
    val codigo: String,
    val nuevaContrasena: String
)

data class MensajeResponse(val mensaje: String)

data class UsuarioResponse(
    val id: Long,
    val rolCodigo: String,
    val rolNombre: String,
    val rut: String,
    val nombre: String,
    val correo: String,
    val correoPersonal: String?,
    val telefono: String,
    val activo: Boolean,
    val tieneContrasena: Boolean,
    val giro: String?,
    val contactoNombre: String?,
    val contactoCargo: String?
)

fun UsuarioResponse.aUsuario() = Usuario(
    id = id.toInt(),
    rolCodigo = rolCodigo,
    rolNombre = rolNombre,
    rut = rut,
    nombre = nombre,
    correo = correo,
    correoPersonal = correoPersonal,
    telefono = telefono,
    activo = activo,
    tieneContrasena = tieneContrasena,
    giro = giro,
    contactoNombre = contactoNombre,
    contactoCargo = contactoCargo
)