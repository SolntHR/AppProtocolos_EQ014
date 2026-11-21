package com.example.appprotocolos_eq014.model

import com.example.appprotocolos_eq014.model.catalogos.EstadoProtocolo
import com.example.appprotocolos_eq014.model.catalogos.ResultadoPrueba
import com.example.appprotocolos_eq014.model.entidades.HistorialEstado
import com.example.appprotocolos_eq014.model.entidades.Prueba
import com.example.appprotocolos_eq014.util.Fechas


data class ProtocoloListado(
    val id: String,
    val numero: String,
    val equipoId: Long,
    val tagEquipo: String,
    val tipoEquipo: String,
    val instalacion: String,
    val cliente: String,
    val tecnicoId: Long,
    val tecnico: String,
    val fechaHora: String,
    val estado: EstadoProtocolo,
    val resultadoGlobal: ResultadoPrueba?,
    val reemplazadoPorNumero: String? = null,
    val reemplazaANumero: String? = null
) {
    val atrasado: Boolean
        get() = (estado == EstadoProtocolo.BORRADOR ||
                estado == EstadoProtocolo.REGISTRADO_TERRENO ||
                estado == EstadoProtocolo.OBSERVADO) &&
                Fechas.diasHabilesDesde(fechaHora) > 2
}

data class ProtocoloDetalle(
    val resumen: ProtocoloListado,
    val numeroOt: String,
    val temperatura: Double?,
    val humedad: Double?,
    val descripcionMantenimiento: String?,
    val observaciones: String?,
    val pruebas: List<Prueba>,
    val historialEstados: List<HistorialEstado>,
    val historialEquipo: List<ProtocoloListado>
) {
    val tieneVariacionSignificativa: Boolean
        get() = pruebas.any { p -> p.comparaciones.any { it.significativa } }
}
