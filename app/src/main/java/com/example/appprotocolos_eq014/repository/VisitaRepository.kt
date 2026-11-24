package com.example.appprotocolos_eq014.repository

import com.example.appprotocolos_eq014.data.remote.ProtocoloApi
import com.example.appprotocolos_eq014.data.remote.RecepcionRequest
import com.example.appprotocolos_eq014.data.remote.RetrofitClient
import com.example.appprotocolos_eq014.data.remote.VisitaRequest
import com.example.appprotocolos_eq014.data.remote.aVisita
import com.example.appprotocolos_eq014.data.remote.listaApi
import com.example.appprotocolos_eq014.data.remote.llamarApi
import com.example.appprotocolos_eq014.data.remote.sinContenidoApi
import com.example.appprotocolos_eq014.model.entidades.Visita

class VisitaRepository(
    private val protocoloApi: ProtocoloApi = RetrofitClient.protocoloApi
) {

    suspend fun visitasDeUsuario(idUsuario: Long): List<Visita> =
        listaApi { protocoloApi.listarVisitas(idUsuario, null) }
            .map { it.aVisita() }
            .sortedWith(compareBy<Visita> { it.cerrada }.thenByDescending { it.fecha })

    suspend fun obtenerVisita(idVisita: Long): Visita =
        llamarApi { protocoloApi.obtenerVisita(idVisita) }.aVisita()

    suspend fun obtenerActa(idVisita: Long, idUsuario: Long): Visita =
        llamarApi { protocoloApi.acta(idVisita, idUsuario) }.aVisita()

    suspend fun crearVisita(
        idUsuario: Long,
        numeroOt: String,
        idInstalacion: Long,
        fecha: String,
        tipoVisita: String,
        idIngeniero: Long,
        participantes: List<Long>
    ): Long {
        val datos = VisitaRequest(idUsuario, numeroOt, idInstalacion, fecha, tipoVisita, idIngeniero, participantes)
        return llamarApi { protocoloApi.crearVisita(datos) }.id
    }

    suspend fun actualizarVisita(
        idVisita: Long,
        idUsuario: Long,
        numeroOt: String,
        idInstalacion: Long,
        fecha: String,
        tipoVisita: String,
        idIngeniero: Long,
        participantes: List<Long>
    ): Long {
        val datos = VisitaRequest(idUsuario, numeroOt, idInstalacion, fecha, tipoVisita, idIngeniero, participantes)
        return llamarApi { protocoloApi.actualizarVisita(idVisita, datos) }.id
    }

    suspend fun eliminarVisita(idVisita: Long, idUsuario: Long) {
        sinContenidoApi { protocoloApi.eliminarVisita(idVisita, idUsuario) }
    }

    suspend fun registrarRecepcion(idVisita: Long, nombre: String, rut: String, cargo: String) {
        llamarApi { protocoloApi.registrarRecepcion(idVisita, RecepcionRequest(nombre, rut, cargo)) }
    }
}