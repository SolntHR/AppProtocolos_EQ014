package com.example.appprotocolos_eq014.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // url gateway con todos los microservicios
    const val URL_GATEWAY = "https://q6tvtkkd-8090.brs.devtunnels.ms/" // URL DEL GITHUB DEL MICROSERVICIO GATEWAY

    private fun crear(urlBase: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(urlBase)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // lazy de comunicación con cada microservicio
    // cada vez que queramos comunicarlos con ese microservicio se usará esta variable
    val usuarioApi: UsuarioApi by lazy { crear(URL_GATEWAY).create(UsuarioApi::class.java) }
    val instalacionApi: InstalacionApi by lazy { crear(URL_GATEWAY).create(InstalacionApi::class.java) }
    val instrumentoApi: InstrumentoApi by lazy { crear(URL_GATEWAY).create(InstrumentoApi::class.java) }
    val protocoloApi: ProtocoloApi by lazy { crear(URL_GATEWAY).create(ProtocoloApi::class.java) }

    // aqui tambien se agrega la api para fotos en caso de querer agregarla.

}
