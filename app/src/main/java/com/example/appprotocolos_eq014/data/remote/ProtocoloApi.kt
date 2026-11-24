package com.example.appprotocolos_eq014.data.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ProtocoloApi {

    @GET("visitas")
    suspend fun listarVisitas(
        @Query("usuarioId") usuarioId: Long,
        @Query("abiertas") abiertas: Boolean?
    ): Response<List<VisitaResponse>>

    @GET("visitas/{id}")
    suspend fun obtenerVisita(@Path("id") id: Long): VisitaResponse

    @POST("visitas")
    suspend fun crearVisita(@Body datos: VisitaRequest): VisitaResponse

    @PUT("visitas/{id}")
    suspend fun actualizarVisita(@Path("id") id: Long, @Body datos: VisitaRequest): VisitaResponse

    @DELETE("visitas/{id}")
    suspend fun eliminarVisita(@Path("id") id: Long, @Query("usuarioId") usuarioId: Long): Response<Unit>

    @POST("visitas/{id}/recepcion")
    suspend fun registrarRecepcion(@Path("id") id: Long, @Body datos: RecepcionRequest): VisitaResponse

    @GET("visitas/{id}/acta")
    suspend fun acta(@Path("id") id: Long, @Query("usuarioId") usuarioId: Long): ActaResponse

    @GET("protocolos")
    suspend fun listarProtocolos(
        @Query("estado") estado: String?,
        @Query("equipoId") equipoId: Long?,
        @Query("visitaId") visitaId: Long?,
        @Query("usuarioId") usuarioId: Long?
    ): Response<List<ProtocoloResumenResponse>>

    @GET("protocolos/indicadores")
    suspend fun indicadores(): IndicadoresResponse

    @GET("protocolos/{id}")
    suspend fun obtenerProtocolo(@Path("id") id: String): ProtocoloDetalleResponse

    @POST("protocolos")
    suspend fun crearProtocolo(@Body datos: ProtocoloRequest): ProtocoloDetalleResponse

    @PUT("protocolos/{id}")
    suspend fun actualizarProtocolo(@Path("id") id: String, @Body datos: ProtocoloRequest): ProtocoloDetalleResponse

    @Multipart
    @POST("protocolos/{id}/evidencias")
    suspend fun subirEvidencia(
        @Path("id") id: String,
        @Query("usuarioId") usuarioId: Long,
        @Part archivo: MultipartBody.Part
    ): EvidenciaResponse

    @DELETE("evidencias/{id}")
    suspend fun eliminarEvidencia(@Path("id") id: Long, @Query("usuarioId") usuarioId: Long): Response<Unit>

    @PATCH("protocolos/{id}/estado")
    suspend fun cambiarEstado(@Path("id") id: String, @Body datos: CambioEstadoRequest): ProtocoloDetalleResponse

    @GET("protocolos/equipo/{equipoId}/historial")
    suspend fun historialEquipo(@Path("equipoId") equipoId: Long): Response<List<ProtocoloResumenResponse>>

    @GET("protocolos/equipo/{equipoId}/mediciones-anteriores")
    suspend fun medicionesAnteriores(
        @Path("equipoId") equipoId: Long,
        @Query("antesDe") antesDe: String?,
        @Query("excluir") excluir: String?
    ): Response<List<MedicionAnteriorResponse>>
}