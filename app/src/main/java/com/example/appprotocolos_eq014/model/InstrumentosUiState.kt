package com.example.appprotocolos_eq014.model

data class InstrumentosUiState(
    val isLoading: Boolean = true,
    val instrumentos: List<Instrumento> = emptyList(),
    val instrumentoEditando: Instrumento? = null,
    val fechaCalibracion: String = "",
    val fechaVencimiento: String = "",
    val errorFechaCalibracion: String? = null,
    val errorFechaVencimiento: String? = null,
    val guardando: Boolean = false,
    val mensaje: String? = null
) {
    val porVencerOVencidos: Int
        get() = instrumentos.count { it.estadoCalibracion != EstadoCalibracion.VIGENTE }
}
