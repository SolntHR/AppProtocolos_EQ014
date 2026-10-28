package com.example.appprotocolos_eq014.model

data class VisitaResumen(
    val id: Long,
    val numeroOt: String,
    val cliente: String,
    val instalacion: String,
    val fecha: String,
    val cerrada: Boolean,
    val totalProtocolos: Int,
    val protocolosRegistrados: Int
)

data class MedicionActa(
    val parametro: String,
    val valor: Double,
    val unidad: String
)

data class PruebaActa(
    val nombre: String,
    val elemento: String,
    val realizada: Boolean,
    val motivoNoRealizada: String?,
    val resultado: ResultadoPrueba?,
    val mediciones: List<MedicionActa>
)

data class ProtocoloActa(
    val numero: String,
    val idEquipo: Long,
    val tag: String,
    val descripcionEquipo: String,
    val estado: EstadoProtocolo,
    val pruebas: List<PruebaActa>
) {
    val resultadoGlobal: ResultadoPrueba?
        get() = pruebas.filter { it.realizada }.mapNotNull { it.resultado }.maxByOrNull { it.idDb }
}

data class RecepcionActa(
    val nombre: String,
    val rut: String,
    val cargo: String,
    val fechaHora: String
)

data class ActaVisita(
    val idVisita: Long,
    val numeroOt: String,
    val cliente: String,
    val instalacion: String,
    val fecha: String,
    val tipoVisita: String,
    val tecnicos: String,
    val ingenieroCargo: String,
    val protocolos: List<ProtocoloActa>,
    val recepcion: RecepcionActa?
)
