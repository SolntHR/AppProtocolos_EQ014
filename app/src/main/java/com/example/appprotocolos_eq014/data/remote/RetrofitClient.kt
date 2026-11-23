package com.example.appprotocolos_eq014.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // url microservicios
    private const val URL_EUREKA = "" // URL DEL GITHUB DEL MICROSERVICIO EUREKA
    private const val URL_USUARIOS = "" // URL DEL GITHUB DEL MICROSERVICIO USUARIO
    private const val URL_INSTALACIONES = "" // URL DEL GITHUB DEL MICROSERVICIO INSTALACION
    private const val URL_INSTRUMENTOS = "" // URL DEL GITHUB DEL MICROSERVICIO INSTRUMENTO
    private const val URL_PROTOCOLOS = "" // URL DEL GITHUB DEL MICROSERVICIO PROTOCOLO

    // FUNCION RETROFIT PAARA CREAR LAS URL DE LAS API

    private fun crear(urlBase: String) Retrofit =
        Retrofit.Builder()
            .baseUrl(urlBase)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // lazy de comunicación con cada microservicio
    // cada vez que queramos comunicarlos con ese microservicio se usará esta variable
    val eurekaApi: EurekaApi by lazy { crear(URL_EUREKA).create(EurekaApi::class.java) }
    val usuarioApi: UsuarioApi by lazy { crear(URL_USUARIOS).create(UsuarioApi::class.java) }
    val instalacionApi: InstalacionApi by lazy { crear(URL_INSTALACIONES).create(InstalacionApi::class.java) }
    val instrumentoApi: InstrumentoApi by lazy { crear(URL_INSTRUMENTOS).create(InstrumentoApi::class.java) }
    val protocoloApi: ProtocoloApi by lazy { crear(URL_PROTOCOLOS).create(ProtocoloApi::class.java) }

    // aqui tambien se agrega la api para fotos en caso de querer agregarla.

}
