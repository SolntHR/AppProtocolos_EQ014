package com.example.appprotocolos_eq014.data.remote

import com.example.appprotocolos_eq014.model.entidades.Instrumento

data class InstrumentoRequest(
    val nombreTipo: String,
    val marca: String,
    val modelo: String,
    val numeroSerie: String
)

data class InstrumentoResponse(
    val id: Long,
    val nombreTipo: String,
    val marca: String,
    val modelo: String,
    val numeroSerie: String,
    val fechaCalibracion: String?,
    val fechaVencimiento: String?,
    val diasRestantes: Long?,
    val estado: String,
    val estadoEtiqueta: String
)

data class CalibracionRequest(
    val fechaCalibracion: String,
    val fechaVencimiento: String,
    val certificado: String?
)

data class CalibracionResponse(
    val id: Long,
    val instrumentoId: Long,
    val fechaCalibracion: String,
    val fechaVencimiento: String,
    val certificado: String?
)

data class VigenciaResponse(
    val instrumentoId: Long,
    val fecha: String,
    val vigente: Boolean,
    val fechaVencimiento: String?
)

fun InstrumentoResponse.aInstrumento() = Instrumento(
    id = id,
    nombreTipo = nombreTipo,
    marca = marca,
    modelo = modelo,
    numeroSerie = numeroSerie,
    fechaCalibracion = fechaCalibracion,
    fechaVencimiento = fechaVencimiento
)