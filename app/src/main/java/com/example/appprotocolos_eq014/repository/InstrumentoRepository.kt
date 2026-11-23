package com.example.appprotocolos_eq014.repository

import com.example.appprotocolos_eq014.data.remote.CalibracionRequest
import com.example.appprotocolos_eq014.data.remote.InstrumentoApi
import com.example.appprotocolos_eq014.data.remote.RetrofitClient
import com.example.appprotocolos_eq014.data.remote.aInstrumento
import com.example.appprotocolos_eq014.data.remote.listaApi
import com.example.appprotocolos_eq014.data.remote.llamarApi
import com.example.appprotocolos_eq014.model.entidades.Instrumento

class InstrumentoRepository(private val instrumentoApi: InstrumentoApi = RetrofitClient.instrumentoApi
) {

    suspend fun listarConCalibracion(): List<Instrumento> =
        listaApi { instrumentoApi.listar(null) }
            .map { it.aInstrumento() }
            .sortedWith(compareBy(nullsLast()) { it.fechaVencimiento })

    suspend fun registrarCalibracion(idInstrumento: Long, fechaCalibracion: String, fechaVencimiento: String) {
        val datos = CalibracionRequest(fechaCalibracion, fechaVencimiento, null)
        llamarApi { instrumentoApi.registrarCalibracion(idInstrumento, datos) }
    }
}