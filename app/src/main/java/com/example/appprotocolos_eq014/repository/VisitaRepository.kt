package com.example.appprotocolos_eq014.repository

import com.example.appprotocolos_eq014.data.remote.ProtocoloApi
import com.example.appprotocolos_eq014.data.remote.RecepcionRequest
import com.example.appprotocolos_eq014.data.remote.RetrofitClient
import com.example.appprotocolos_eq014.data.remote.aVisita
import com.example.appprotocolos_eq014.data.remote.listaApi
import com.example.appprotocolos_eq014.data.remote.llamarApi
import com.example.appprotocolos_eq014.model.entidades.Visita

class VisitaRepository(private val protocoloApi: ProtocoloApi = RetrofitClient.protocoloApi
) {

    suspend fun visitasDeUsuario(idUsuario: Long): List<Visita> =
        listaApi { protocoloApi.listarVisitas(idUsuario, null) }
            .map { it.aVisita() }
            .sortedWith(compareBy<Visita> { it.cerrada }.thenByDescending { it.fecha })

    suspend fun obtenerActa(idVisita: Long, idUsuario: Long): Visita =
        llamarApi { protocoloApi.acta(idVisita, idUsuario) }.aVisita()

    suspend fun registrarRecepcion(idVisita: Long, nombre: String, rut: String, cargo: String) {
        llamarApi { protocoloApi.registrarRecepcion(idVisita, RecepcionRequest(nombre, rut, cargo)) }
    }
}