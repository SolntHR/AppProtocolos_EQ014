package com.example.appprotocolos_eq014.repository

import com.example.appprotocolos_eq014.data.remote.CambioEstadoRequest
import com.example.appprotocolos_eq014.data.remote.InstalacionApi
import com.example.appprotocolos_eq014.data.remote.InstrumentoApi
import com.example.appprotocolos_eq014.data.remote.ProtocoloApi
import com.example.appprotocolos_eq014.data.remote.RetrofitClient
import com.example.appprotocolos_eq014.data.remote.aProtocolo
import com.example.appprotocolos_eq014.data.remote.listaApi
import com.example.appprotocolos_eq014.data.remote.llamarApi
import com.example.appprotocolos_eq014.model.catalogos.EstadoProtocolo
import com.example.appprotocolos_eq014.model.entidades.Protocolo
import com.example.appprotocolos_eq014.model.reglas.MedicionAnterior
import com.example.appprotocolos_eq014.model.reglas.ReglasProtocolo

class ProtocoloRepository(
    private val protocoloApi: ProtocoloApi = RetrofitClient.protocoloApi,
    private val instalacionApi: InstalacionApi = RetrofitClient.instalacionApi,
    private val instrumentoApi: InstrumentoApi = RetrofitClient.instrumentoApi
) {

    suspend fun listar(): List<Protocolo> =
        conReemplazos(listaApi { protocoloApi.listarProtocolos(null, null, null, null) }.map { it.aProtocolo() })
            .sortedByDescending { it.fechaHoraPrueba }

    suspend fun promedioDiasFormalizacion(): Double? =
        llamarApi { protocoloApi.indicadores() }.promedioDiasFormalizacion

    suspend fun historialEquipo(idEquipo: Long): List<Protocolo> =
        conReemplazos(listaApi { protocoloApi.historialEquipo(idEquipo) }.map { it.aProtocolo() })

    suspend fun detalle(idProtocolo: String): Protocolo {
        val respuesta = llamarApi { protocoloApi.obtenerProtocolo(idProtocolo) }
        val equipo = llamarApi { instalacionApi.obtenerEquipo(respuesta.equipoId) }
        val instrumentos = listaApi { instrumentoApi.listar(null) }
            .associate { it.id to "${it.marca} ${it.modelo} · Serie ${it.numeroSerie}" }
        val anteriores = listaApi {
            protocoloApi.medicionesAnteriores(respuesta.equipoId, respuesta.fechaHoraPrueba, respuesta.id)
        }
            .groupBy { "${it.tipoPruebaId}|${it.elemento}" }
            .mapValues { (_, filas) ->
                MedicionAnterior(
                    filas.first().protocoloNumero,
                    filas.mapNotNull { fila -> fila.valor?.let { fila.parametroCodigo to it } }.toMap()
                )
            }
        val protocolo = respuesta.aProtocolo()
        return protocolo.copy(
            reemplazaANumero = respuesta.protocoloReemplazadoId,
            pruebas = protocolo.pruebas.map { prueba ->
                val anterior = anteriores["${prueba.tipo.idDb}|${prueba.elemento}"]
                prueba.copy(
                    instrumentoNombre = prueba.instrumentoId?.let { instrumentos[it] },
                    calculo = ReglasProtocolo.resumenCalculo(prueba.tipo, prueba.valores, equipo.maxContactosUohm),
                    comparaciones = if (anterior == null || !prueba.realizada) emptyList()
                    else ReglasProtocolo.comparar(prueba.tipo, prueba.valores, anterior)
                )
            }
        )
    }

    suspend fun cambiarEstado(idProtocolo: String, nuevo: EstadoProtocolo, usuarioId: Long, motivo: String?) {
        val datos = CambioEstadoRequest(usuarioId, nuevo.name, motivo?.trim()?.ifEmpty { null })
        llamarApi { protocoloApi.cambiarEstado(idProtocolo, datos) }
    }

    private fun conReemplazos(protocolos: List<Protocolo>): List<Protocolo> {
        val numeros = protocolos.associate { it.id to it.numero }
        val reemplazadoPor = protocolos.filter { it.reemplazaANumero != null }
            .associate { it.reemplazaANumero to it.numero }
        return protocolos.map {
            it.copy(
                reemplazaANumero = it.reemplazaANumero?.let { id -> numeros[id] ?: id },
                reemplazadoPorNumero = reemplazadoPor[it.id]
            )
        }
    }
}