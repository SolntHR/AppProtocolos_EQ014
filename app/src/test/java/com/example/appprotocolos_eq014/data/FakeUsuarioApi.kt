package com.example.appprotocolos_eq014.data

import com.example.appprotocolos_eq014.data.remote.ActualizarUsuarioRequest
import com.example.appprotocolos_eq014.data.remote.CambiarActivoRequest
import com.example.appprotocolos_eq014.data.remote.CambiarContrasenaRequest
import com.example.appprotocolos_eq014.data.remote.CambiarRolRequest
import com.example.appprotocolos_eq014.data.remote.CrearUsuarioRequest
import com.example.appprotocolos_eq014.data.remote.LoginRequest
import com.example.appprotocolos_eq014.data.remote.MensajeResponse
import com.example.appprotocolos_eq014.data.remote.RegistroRequest
import com.example.appprotocolos_eq014.data.remote.RegistroResponse
import com.example.appprotocolos_eq014.data.remote.RestablecerConCodigoRequest
import com.example.appprotocolos_eq014.data.remote.RestablecerConPreguntaRequest
import com.example.appprotocolos_eq014.data.remote.SolicitarCodigoRequest
import com.example.appprotocolos_eq014.data.remote.UsuarioApi
import com.example.appprotocolos_eq014.data.remote.UsuarioResponse
import com.example.appprotocolos_eq014.data.remote.VerificarCodigoRequest
import com.example.appprotocolos_eq014.data.remote.VerificarPreguntaRequest
import retrofit2.Response

class FakeUsuarioApi : UsuarioApi {

    var ultimaSolicitudCambioRol: CambiarRolRequest? = null
    var ultimaSolicitudCodigo: SolicitarCodigoRequest? = null
    var ultimaVerificacionCodigo: VerificarCodigoRequest? = null
    var ultimoRestablecimientoCodigo: RestablecerConCodigoRequest? = null
    var ultimaVerificacionPregunta: VerificarPreguntaRequest? = null
    var ultimoRestablecimientoPregunta: RestablecerConPreguntaRequest? = null

    private val jefe = UsuarioResponse(
        7, "JEFE_TECNICO", "Jefe técnico", "13.871.905-7", "Ricardo Espinoza Lagos",
        "respinoza@induztek.ejemplo", "respinoza@induztek.ejemplo", "+56 9 9345 1187",
        true, true, null, null, null
    )

    override suspend fun login(datos: LoginRequest): UsuarioResponse = jefe

    override suspend fun registrar(datos: RegistroRequest): RegistroResponse = TODO()
    override suspend fun preguntas(): List<String> = TODO()
    override suspend fun listar(rol: String?, activo: Boolean?, buscar: String?): Response<List<UsuarioResponse>> = TODO()
    override suspend fun obtener(id: Long): UsuarioResponse = jefe.copy(id = id)
    override suspend fun crear(datos: CrearUsuarioRequest): UsuarioResponse = TODO()
    override suspend fun actualizar(id: Long, datos: ActualizarUsuarioRequest): UsuarioResponse = TODO()
    override suspend fun cambiarRol(id: Long, datos: CambiarRolRequest): UsuarioResponse {
        ultimaSolicitudCambioRol = datos
        return jefe.copy(id = id, rolCodigo = datos.rol)
    }
    override suspend fun cambiarActivo(id: Long, datos: CambiarActivoRequest): UsuarioResponse = TODO()
    override suspend fun cambiarContrasena(id: Long, datos: CambiarContrasenaRequest): Response<Unit> = TODO()
    override suspend fun verificarPregunta(datos: VerificarPreguntaRequest): Response<Unit> {
        ultimaVerificacionPregunta = datos
        return Response.success(Unit)
    }

    override suspend fun restablecerConPregunta(datos: RestablecerConPreguntaRequest): Response<Unit> {
        ultimoRestablecimientoPregunta = datos
        return Response.success(Unit)
    }

    override suspend fun solicitarCodigo(datos: SolicitarCodigoRequest): MensajeResponse {
        ultimaSolicitudCodigo = datos
        return MensajeResponse("Código enviado")
    }

    override suspend fun verificarCodigo(datos: VerificarCodigoRequest): Response<Unit> {
        ultimaVerificacionCodigo = datos
        return Response.success(Unit)
    }

    override suspend fun restablecerConCodigo(datos: RestablecerConCodigoRequest): Response<Unit> {
        ultimoRestablecimientoCodigo = datos
        return Response.success(Unit)
    }
}
