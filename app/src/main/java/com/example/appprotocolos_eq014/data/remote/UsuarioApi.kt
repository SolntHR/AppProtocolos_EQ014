package com.example.appprotocolos_eq014.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UsuarioApi {
    // meter metodos
    @POST("usuarios/login")
    suspend fun login(@Body datos: LoginRequest): UsuarioResponse

    @POST("usuarios/registro")
    suspend fun registrar(@Body datos: RegistroRequest): RegistroResponse

    @GET("usuarios/preguntas")
    suspend fun preguntas(): List<String>

    @GET("usuarios")
    suspend fun listar(
        @Query("rol") rol: String?,
        @Query("activo") activo: Boolean?,
        @Query("buscar") buscar: String?
    ): Response<List<UsuarioResponse>>
    @GET("usuarios/{id}")
    suspend fun obtener(@Path("id") id: Long): UsuarioResponse

    @POST("usuarios")
    suspend fun crear(@Body datos: CrearUsuarioRequest): UsuarioResponse

    @PUT("usuarios/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body datos: ActualizarUsuarioRequest): UsuarioResponse

    @PATCH("usuarios/{id}/rol")
    suspend fun cambiarRol(@Path("id") id: Long, @Body datos: CambiarRolRequest): UsuarioResponse

    @PATCH("usuarios/{id}/activo")
    suspend fun cambiarActivo(@Path("id") id: Long, @Body datos: CambiarActivoRequest): UsuarioResponse

    @PUT("usuarios/{id}/contrasena")
    suspend fun cambiarContrasena(@Path("id") id: Long, @Body datos: CambiarContrasenaRequest): Response<Unit>

    @POST("usuarios/recuperacion/pregunta/verificar")
    suspend fun verificarPregunta(@Body datos: VerificarPreguntaRequest): Response<Unit>

    @POST("usuarios/recuperacion/pregunta")
    suspend fun restablecerConPregunta(@Body datos: RestablecerConPreguntaRequest): Response<Unit>

    @POST("usuarios/recuperacion/codigo")
    suspend fun solicitarCodigo(@Body datos: SolicitarCodigoRequest): MensajeResponse

    @POST("usuarios/recuperacion/codigo/verificar")
    suspend fun verificarCodigo(@Body datos: VerificarCodigoRequest): Response<Unit>

    @POST("usuarios/recuperacion/codigo/restablecer")
    suspend fun restablecerConCodigo(@Body datos: RestablecerConCodigoRequest): Response<Unit>
}