package com.example.appprotocolos_eq014.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface InstalacionApi {

    @GET("regiones")
    suspend fun regiones(): Response<List<RegionResponse>>

    @GET("regiones/{id}/comunas")
    suspend fun comunasDeRegion(@Path("id") id: Long): Response<List<ComunaResponse>>

    @GET("comunas")
    suspend fun comunas(@Query("regionId") regionId: Long?): Response<List<ComunaResponse>>

    @GET("tipos-equipo")
    suspend fun tiposEquipo(): Response<List<TipoEquipoResponse>>

    @GET("tipos-equipo/{id}")
    suspend fun tipoEquipo(@Path("id") id: Long): TipoEquipoResponse

    @GET("instalaciones")
    suspend fun listarInstalaciones(@Query("clienteId") clienteId: Long?): Response<List<InstalacionResponse>>

    @GET("instalaciones/{id}")
    suspend fun obtenerInstalacion(@Path("id") id: Long): InstalacionResponse

    @GET("instalaciones/{id}/equipos")
    suspend fun equiposDeInstalacion(@Path("id") id: Long): Response<List<EquipoResponse>>

    @POST("instalaciones")
    suspend fun crearInstalacion(@Body datos: InstalacionRequest): InstalacionResponse

    @PUT("instalaciones/{id}")
    suspend fun actualizarInstalacion(@Path("id") id: Long, @Body datos: InstalacionRequest): InstalacionResponse

    @GET("equipos")
    suspend fun listarEquipos(
        @Query("instalacionId") instalacionId: Long?,
        @Query("clienteId") clienteId: Long?,
        @Query("tipoEquipoId") tipoEquipoId: Long?
    ): Response<List<EquipoResponse>>

    @GET("equipos/{id}")
    suspend fun obtenerEquipo(@Path("id") id: Long): EquipoResponse

    @POST("equipos")
    suspend fun crearEquipo(@Body datos: EquipoRequest): EquipoResponse

    @PUT("equipos/{id}")
    suspend fun actualizarEquipo(@Path("id") id: Long, @Body datos: EquipoRequest): EquipoResponse
}