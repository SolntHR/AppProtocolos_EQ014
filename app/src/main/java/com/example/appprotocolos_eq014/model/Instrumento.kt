package com.example.appprotocolos_eq014.model

import com.example.appprotocolos_eq014.util.Formato

enum class EstadoCalibracion(val etiqueta: String) {
    VIGENTE("Vigente"),
    POR_VENCER("Por vencer"),
    VENCIDA("Vencida"),
    SIN_CALIBRACION("Sin calibración")
}

data class Instrumento(
    val id: Long = 0,
    val nombreTipo: String,
    val marca: String,
    val modelo: String,
    val numeroSerie: String,
    val fechaCalibracion: String? = null,
    val fechaVencimiento: String? = null
){
    val marcaModelo: String = "$marca $modelo".trim()

    val descripcion: String = marcaModelo.ifBlank { nombreTipo }

    val diasRestantes: Long? = fechaVencimiento?.let { Formato.diasHasta(it) }

    val estadoCalibracion: EstadoCalibracion = when {
        diasRestantes == null -> EstadoCalibracion.SIN_CALIBRACION
        diasRestantes < 0 -> EstadoCalibracion.VENCIDA
        diasRestantes <= DIAS_AVISO -> EstadoCalibracion.POR_VENCER
        else -> EstadoCalibracion.VIGENTE
    }

    fun vigenteEn(fechaIso: String): Boolean =
        fechaVencimiento != null && fechaIso.take(10) <= fechaVencimiento

    companion object {
        const val DIAS_AVISO = 30L
    }
}

