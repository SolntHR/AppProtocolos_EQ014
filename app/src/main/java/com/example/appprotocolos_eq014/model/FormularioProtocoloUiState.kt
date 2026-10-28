package com.example.appprotocolos_eq014.model

// ---------------------------------------------------------------
// Estado del formulario de protocolo (RF-03 a RF-06, RF-17)
// ---------------------------------------------------------------

data class FilaPruebaUi(
    val elemento: String,
    val valores: Map<String, String> = emptyMap(),
    val opera: Boolean = true,                    // solo relé
    val resultadoManual: ResultadoPrueba? = null, // solo devanados (RN-08c)
    val evaluacion: Evaluacion = Evaluacion()
)

data class SeccionPruebaUi(
    val tipo: TipoPrueba,
    val filas: List<FilaPruebaUi>,
    val instrumentoId: Long? = null,
    val comunes: Map<String, String> = emptyMap(), // tensión de prueba, tap, máximo de placa
    val unidadAislacion: String = "MΩ",
    val comentario: String = "",
    val errorInstrumento: String? = null
) {
    val filasCompletas: List<FilaPruebaUi>
        get() = filas.filter { it.evaluacion.estado == EstadoFila.COMPLETA }
    val resultado: ResultadoPrueba?
        get() = peorResultado(filasCompletas.map { it.evaluacion.resultado })
}

data class FormularioProtocoloUiState(
    val cargando: Boolean = true,
    val guardando: Boolean = false,

    // Si se edita un protocolo existente (RN-17)
    val idProtocolo: String? = null,
    val numero: String? = null,
    val estadoActual: EstadoProtocolo? = null,
    val fechaHoraPrueba: String = "",
    val tecnicoNombre: String = "",
    val rolUsuario: String = "",

    val visitas: List<VisitaAbierta> = emptyList(),
    val visitaSeleccionada: VisitaAbierta? = null,
    val equipos: List<EquipoProtocolo> = emptyList(),
    val equipoSeleccionado: EquipoProtocolo? = null,

    val temperatura: String = "",
    val humedad: String = "",
    val errorTemperatura: String? = null,
    val errorHumedad: String? = null,

    val instrumentos: List<InstrumentoOpcion> = emptyList(),
    val secciones: List<SeccionPruebaUi> = emptyList(),
    val anteriores: Map<String, MedicionAnterior> = emptyMap(), // clave "idTipoPrueba|elemento"

    val descripcionMantenimiento: String = "",
    val observaciones: String = "",

    val error: String? = null,
    val protocoloGuardadoId: String? = null,
    val mensajeGuardado: String? = null
) {
    val resultadoGlobal: ResultadoPrueba?
        get() = peorResultado(secciones.map { it.resultado })

    val hayVariacionSignificativa: Boolean
        get() = secciones.any { s -> s.filas.any { f -> f.evaluacion.comparaciones.any { it.significativa } } }

    val esEdicion: Boolean get() = idProtocolo != null
}
