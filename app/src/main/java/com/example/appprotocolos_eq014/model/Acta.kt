package com.example.appprotocolos_eq014.model

data class PruebaActa(
    val nombre: String,
    val elemento: String,
    val realizada: Boolean,
    val motivoNoRealizada: String?,
    val resultado: ResultadoPrueba?,
    val mediciones: List<Medicion>
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

