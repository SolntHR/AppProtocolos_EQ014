package com.example.appprotocolos_eq014.model

enum class EstadoCalibracion(val etiqueta: String) {
    VIGENTE("Vigente"),
    POR_VENCER("Por vencer"),
    VENCIDA("Vencida"),
    SIN_CALIBRACION("Sin calibración")
}

data class InstrumentoCalibracion(
    val id: Long,
    val nombre: String,
    val marcaModelo: String,
    val numeroSerie: String,
    val fechaCalibracion: String?,
    val fechaVencimiento: String?,
    val diasRestantes: Long?,
    val estado: EstadoCalibracion
)

data class InstrumentosUiState(
    val isLoading: Boolean = true,
    val instrumentos: List<InstrumentoCalibracion> = emptyList(),
    val instrumentoEditando: InstrumentoCalibracion? = null,
    val fechaCalibracion: String = "",
    val fechaVencimiento: String = "",
    val errorFechaCalibracion: String? = null,
    val errorFechaVencimiento: String? = null,
    val guardando: Boolean = false,
    val mensaje: String? = null
) {
    val porVencerOVencidos: Int
        get() = instrumentos.count { it.estado != EstadoCalibracion.VIGENTE }
}
