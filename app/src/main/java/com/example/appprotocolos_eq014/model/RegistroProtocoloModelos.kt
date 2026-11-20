package com.example.appprotocolos_eq014.model

import com.example.appprotocolos_eq014.model.entidades.Prueba

object Rol {
    const val TECNICO = "TECNICO"
    const val INGENIERO = "INGENIERO"
    const val JEFE_TECNICO = "JEFE_TECNICO"
    const val CLIENTE = "CLIENTE"

    fun puedeCrearProtocolos(codigo: String) = codigo == TECNICO || codigo == INGENIERO || codigo == JEFE_TECNICO
    fun puedeFormalizar(codigo: String) = codigo == INGENIERO || codigo == JEFE_TECNICO
    fun esPersonalInduztek(codigo: String) = codigo.isNotBlank() && codigo != CLIENTE
}

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

data class Comparacion(
    val codigo: String,
    val nombre: String,
    val valorAnterior: Double,
    val valorActual: Double,
    val unidad: String,
    val numeroAnterior: String?
) {
    val variacionPct: Double
        get() = if (valorAnterior == 0.0) 0.0 else (valorActual - valorAnterior) / valorAnterior * 100.0

    val significativa: Boolean get() = kotlin.math.abs(variacionPct) > 25.0
}

data class CambioEstado(
    val anterior: EstadoProtocolo?,
    val nuevo: EstadoProtocolo,
    val usuario: String,
    val fechaHora: String,
    val motivo: String?
)

data class ProtocoloDetalle(
    val resumen: ProtocoloListado,
    val numeroOt: String,
    val temperatura: Double?,
    val humedad: Double?,
    val descripcionMantenimiento: String?,
    val observaciones: String?,
    val pruebas: List<Prueba>,
    val historialEstados: List<CambioEstado>,
    val historialEquipo: List<ProtocoloListado>
) {
    val tieneVariacionSignificativa: Boolean
        get() = pruebas.any { p -> p.comparaciones.any { it.significativa } }
}
