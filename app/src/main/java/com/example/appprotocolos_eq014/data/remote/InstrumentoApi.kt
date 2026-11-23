package com.example.appprotocolos_eq014.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface InstrumentoApi {

    @GET("instrumentos")
    suspend fun listar(@Query("estado") estado: String?): Response<List<InstrumentoResponse>>

    @GET("instrumentos/{id}")
    suspend fun obtener(@Path("id") id: Long): InstrumentoResponse

    @POST("instrumentos")
    suspend fun crear(@Body datos: InstrumentoRequest): InstrumentoResponse

    @PUT("instrumentos/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body datos: InstrumentoRequest): InstrumentoResponse

    @GET("instrumentos/{id}/calibraciones")
    suspend fun calibraciones(@Path("id") id: Long): Response<List<CalibracionResponse>>

    @POST("instrumentos/{id}/calibraciones")
    suspend fun registrarCalibracion(@Path("id") id: Long, @Body datos: CalibracionRequest): CalibracionResponse

    @GET("instrumentos/{id}/vigencia")
    suspend fun vigencia(@Path("id") id: Long, @Query("fecha") fecha: String?): VigenciaResponse
}