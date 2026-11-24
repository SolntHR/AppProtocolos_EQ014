package com.example.appprotocolos_eq014.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
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

    @POST("visitas/{id}/recepcion")
    suspend fun registrarRecepcion(@Path("id") id: Long, @Body datos: RecepcionRequest): VisitaResponse

    @GET("visitas/{id}/acta")
    suspend fun acta(@Path("id") id: Long, @Query("usuarioId") usuarioId: Long): ActaResponse
}