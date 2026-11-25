package com.example.appprotocolos_eq014.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    const val URL_GATEWAY = "https://fjfh1k06-8090.brs.devtunnels.ms/"

    private fun crear(urlBase: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(urlBase)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val usuarioApi: UsuarioApi by lazy { crear(URL_GATEWAY).create(UsuarioApi::class.java) }
    val instalacionApi: InstalacionApi by lazy { crear(URL_GATEWAY).create(InstalacionApi::class.java) }
    val instrumentoApi: InstrumentoApi by lazy { crear(URL_GATEWAY).create(InstrumentoApi::class.java) }
    val protocoloApi: ProtocoloApi by lazy { crear(URL_GATEWAY).create(ProtocoloApi::class.java) }
}