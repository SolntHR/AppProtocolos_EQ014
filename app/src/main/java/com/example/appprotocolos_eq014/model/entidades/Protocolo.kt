package com.example.appprotocolos_eq014.model.entidades

import com.example.appprotocolos_eq014.model.catalogos.EstadoProtocolo
import com.example.appprotocolos_eq014.model.catalogos.ResultadoPrueba
import com.example.appprotocolos_eq014.util.Fechas

data class Protocolo(
    val id: String = "",
    val numero: String = "",
    val visitaId: Long,
    val equipoId: Long,
    val usuarioRegistraId: Long = 0,
    val fechaHoraPrueba: String = "",
    val temperatura: Double? = null,
    val humedad: Double? = null,
    val descripcionMantenimiento: String? = null,
    val observaciones: String? = null,
    val estado: EstadoProtocolo = EstadoProtocolo.BORRADOR,

    val numeroOt: String = "",
    val tagEquipo: String = "",
    val tipoEquipoNombre: String = "",
    val equipoDescripcion: String = "",
    val instalacionNombre: String = "",
    val clienteNombre: String = "",
    val tecnicoNombre: String = "",

    val resultadoGlobal: ResultadoPrueba? = null,
    val lectura1Min: Double? = null,
    val lectura10Min: Double? = null,
    val reemplazadoPorNumero: String? = null,
    val reemplazaANumero: String? = null,

    val pruebas: List<Prueba> = emptyList(),
    val historialEstados: List<HistorialEstado> = emptyList(),
    val evidencias: List<Evidencia> = emptyList()

){
    val esNuevo: Boolean get() = id.isBlank()

    val atrasado: Boolean
        get() = (estado == EstadoProtocolo.BORRADOR ||
                estado == EstadoProtocolo.REGISTRADO_TERRENO ||
                estado == EstadoProtocolo.OBSERVADO) &&
                Fechas.diasHabilesDesde(fechaHoraPrueba) > 2

    val tieneVariacionSignificativa: Boolean
        get() = pruebas.any { p -> p.comparaciones.any { it.significativa }}

    val motivoObservacion: String?
        get() = if (estado != EstadoProtocolo.OBSERVADO) null
                else historialEstados.lastOrNull { it.nuevo == EstadoProtocolo.OBSERVADO }?.motivo
}
