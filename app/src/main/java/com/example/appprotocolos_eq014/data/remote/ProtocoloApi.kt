package com.example.appprotocolos_eq014.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
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
}