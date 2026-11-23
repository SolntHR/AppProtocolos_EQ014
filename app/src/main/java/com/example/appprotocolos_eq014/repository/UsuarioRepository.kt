package com.example.appprotocolos_eq014.repository

import com.example.appprotocolos_eq014.data.remote.ActualizarUsuarioRequest
import com.example.appprotocolos_eq014.data.remote.CambiarActivoRequest
import com.example.appprotocolos_eq014.data.remote.CambiarRolRequest
import com.example.appprotocolos_eq014.data.remote.CrearUsuarioRequest
import com.example.appprotocolos_eq014.data.remote.LoginRequest
import com.example.appprotocolos_eq014.data.remote.RegistroRequest
import com.example.appprotocolos_eq014.data.remote.RestablecerConCodigoRequest
import com.example.appprotocolos_eq014.data.remote.RestablecerConPreguntaRequest
import com.example.appprotocolos_eq014.data.remote.RetrofitClient
import com.example.appprotocolos_eq014.data.remote.SolicitarCodigoRequest
import com.example.appprotocolos_eq014.data.remote.UsuarioApi
import com.example.appprotocolos_eq014.data.remote.VerificarCodigoRequest
import com.example.appprotocolos_eq014.data.remote.VerificarPreguntaRequest
import com.example.appprotocolos_eq014.data.remote.aUsuario
import com.example.appprotocolos_eq014.data.remote.listaApi
import com.example.appprotocolos_eq014.data.remote.llamarApi
import com.example.appprotocolos_eq014.data.remote.sinContenidoApi
import com.example.appprotocolos_eq014.model.entidades.Usuario
import com.example.appprotocolos_eq014.util.Seguridad

class UsuarioRepository(private val usuarioApi: UsuarioApi = RetrofitClient.usuarioApi): RegistroUsuarioRepository{

    suspend fun login(correo: String, contrasena: String): Usuario {
        val datos = LoginRequest(correo.trim(), Seguridad.cifrarContrasena(correo, contrasena))
        return llamarApi { usuarioApi.login(datos) }.aUsuario()
    }

    override suspend fun registrar(solicitud: SolicitudRegistroUsuario): ResultadoRegistroUsuario {
        val datos = RegistroRequest(
            nombre = solicitud.nombre,
            rut = solicitud.rut,
            correoPersonal = solicitud.correoPersonal,
            telefono = solicitud.telefono,
            preguntaSeguridad = solicitud.preguntaSeguridad,
            respuestaSeguridad = solicitud.respuestaSeguridad
        )
        val respuesta = llamarApi { usuarioApi.registrar(datos) }
        return ResultadoRegistroUsuario.Creado(respuesta.correoInterno)
    }

    suspend fun obtenerPorId(idUsuario: Long): Usuario =
        llamarApi { usuarioApi.obtener(idUsuario) }.aUsuario()

    suspend fun listarUsuarios(buscar: String = ""): List<Usuario> =
        listaApi { usuarioApi.listar(null, null, buscar.trim().ifBlank { null }) }.map { it.aUsuario() }

    suspend fun crearUsuario(
        rolCodigo: String,
        nombre: String,
        rut: String,
        correoPersonal: String,
        telefono: String,
        contactoNombre: String?,
        contactoCargo: String?
    ): Usuario {
        val datos = CrearUsuarioRequest(
            rol = rolCodigo,
            nombre = nombre,
            rut = rut,
            correoPersonal = correoPersonal.trim(),
            telefono = telefono,
            giro = null,
            contactoNombre = contactoNombre,
            contactoCargo = contactoCargo
        )
        return llamarApi { usuarioApi.crear(datos) }.aUsuario()
    }

    suspend fun actualizarPerfil(
        idUsuario: Long,
        nombre: String,
        rut: String,
        telefono: String,
        giro: String?,
        contactoNombre: String?,
        contactoCargo: String?
    ): Usuario {
        val datos = ActualizarUsuarioRequest(nombre, rut, telefono, giro, contactoNombre, contactoCargo)
        return llamarApi { usuarioApi.actualizar(idUsuario, datos) }.aUsuario()
    }

    suspend fun cambiarRol(idUsuario: Long, rolCodigo: String): Usuario =
        llamarApi { usuarioApi.cambiarRol(idUsuario, CambiarRolRequest(rolCodigo)) }.aUsuario()

    suspend fun cambiarActivo(idUsuario: Long, activo: Boolean): Usuario =
        llamarApi { usuarioApi.cambiarActivo(idUsuario, CambiarActivoRequest(activo)) }.aUsuario()

    suspend fun verificarPregunta(correo: String, pregunta: String, respuesta: String) =
        sinContenidoApi { usuarioApi.verificarPregunta(VerificarPreguntaRequest(correo.trim(), pregunta, respuesta.trim())) }

    suspend fun restablecerConPregunta(correo: String, pregunta: String, respuesta: String, nueva: String) {
        val datos = RestablecerConPreguntaRequest(
            correo = correo.trim(),
            pregunta = pregunta,
            respuesta = respuesta.trim(),
            nuevaContrasena = Seguridad.cifrarContrasena(correo, nueva)
        )
        sinContenidoApi { usuarioApi.restablecerConPregunta(datos) }
    }

    suspend fun solicitarCodigo(correo: String): String =
        llamarApi { usuarioApi.solicitarCodigo(SolicitarCodigoRequest(correo.trim())) }.mensaje

    suspend fun verificarCodigo(correo: String, codigo: String) =
        sinContenidoApi { usuarioApi.verificarCodigo(VerificarCodigoRequest(correo.trim(), codigo.trim())) }

    suspend fun restablecerConCodigo(correo: String, codigo: String, nueva: String) {
        val datos = RestablecerConCodigoRequest(
            correo = correo.trim(),
            codigo = codigo.trim(),
            nuevaContrasena = Seguridad.cifrarContrasena(correo, nueva)
        )
        sinContenidoApi { usuarioApi.restablecerConCodigo(datos) }
    }
}