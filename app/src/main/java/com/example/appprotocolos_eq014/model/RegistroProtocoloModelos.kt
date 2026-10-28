package com.example.appprotocolos_eq014.model

object Rol {
    const val TECNICO = "TECNICO"
    const val INGENIERO = "INGENIERO"
    const val JEFE_TECNICO = "JEFE_TECNICO"
    const val CLIENTE = "CLIENTE"

    fun puedeCrearProtocolos(codigo: String) = codigo == TECNICO || codigo == INGENIERO || codigo == JEFE_TECNICO
    fun puedeFormalizar(codigo: String) = codigo == INGENIERO || codigo == JEFE_TECNICO
    fun esPersonalInduztek(codigo: String) = codigo.isNotBlank() && codigo != CLIENTE
}

data class VisitaAbierta(
    val id: Long,
    val numeroOt: String,
    val fecha: String,
    val tipoVisita: String,
    val instalacionId: Long,
    val instalacionNombre: String,
    val clienteNombre: String,
    val ingenieroNombre: String
) {
    val incluyeMantenimiento: Boolean get() = tipoVisita == "PRUEBAS_Y_MANT"
}

data class EquipoProtocolo(
    val id: Long,
    val tag: String,
    val tipoEquipoId: Long,
    val tipoEquipoNombre: String,
    val marca: String?,
    val numeroSerie: String?,
    val potenciaKva: Double?,
    val tensiones: String?,
    val corriente: Double?,
    val maxContactosUohm: Double?
)

data class InstrumentoOpcion(
    val id: Long,
    val nombreTipo: String,
    val marca: String?,
    val modelo: String?,
    val numeroSerie: String?,
    val fechaVencimiento: String?
) {
    val descripcion: String
        get() = listOfNotNull(marca, modelo).joinToString(" ").ifBlank { nombreTipo }

    fun vigenteEn(fechaIso: String): Boolean =
        fechaVencimiento != null && fechaIso.take(10) <= fechaVencimiento
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

data class MedicionDetalle(
    val codigo: String,
    val nombre: String,
    val valor: Double,
    val unidad: String
)

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

data class PruebaDetalle(
    val tipo: TipoPrueba,
    val elemento: String,
    val instrumento: String?,
    val resultado: ResultadoPrueba?,
    val comentario: String?,
    val ingresadoManualmente: Boolean,
    val realizada: Boolean,
    val motivoNoRealizada: String?,
    val mediciones: List<MedicionDetalle>,
    val calculo: String?,
    val comparaciones: List<Comparacion> = emptyList()
)

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
    val pruebas: List<PruebaDetalle>,
    val historialEstados: List<CambioEstado>,
    val historialEquipo: List<ProtocoloListado>
) {
    val tieneVariacionSignificativa: Boolean
        get() = pruebas.any { p -> p.comparaciones.any { it.significativa } }
}
