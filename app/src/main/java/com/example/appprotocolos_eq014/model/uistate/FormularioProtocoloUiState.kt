package com.example.appprotocolos_eq014.model.uistate

import com.example.appprotocolos_eq014.model.catalogos.EstadoProtocolo
import com.example.appprotocolos_eq014.model.catalogos.ResultadoPrueba
import com.example.appprotocolos_eq014.model.entidades.Equipo
import com.example.appprotocolos_eq014.model.entidades.Instrumento
import com.example.appprotocolos_eq014.model.entidades.Visita
import com.example.appprotocolos_eq014.model.reglas.EstadoFila
import com.example.appprotocolos_eq014.model.reglas.Evaluacion
import com.example.appprotocolos_eq014.model.reglas.MedicionAnterior
import com.example.appprotocolos_eq014.model.reglas.TipoPrueba
import com.example.appprotocolos_eq014.model.reglas.peorResultado

data class FilaPruebaUi(
    val elemento: String,
    val valores: Map<String, String> = emptyMap(),
    val opera: Boolean = true,
    val resultadoManual: ResultadoPrueba? = null,
    val evaluacion: Evaluacion = Evaluacion()
)

data class SeccionPruebaUi(
    val tipo: TipoPrueba,
    val filas: List<FilaPruebaUi>,
    val instrumentoId: Long? = null,
    val comunes: Map<String, String> = emptyMap(),
    val unidadAislacion: String = "MΩ",
    val comentario: String = "",
    val errorInstrumento: String? = null,
    val noRealizada: Boolean = false,
    val motivoNoRealizada: String = ""
) {
    val filasCompletas: List<FilaPruebaUi>
        get() = if (noRealizada) emptyList() else filas.filter { it.evaluacion.estado == EstadoFila.COMPLETA }

    val filasConProblema: Boolean
        get() = !noRealizada && filas.any {
            it.evaluacion.estado == EstadoFila.INCOMPLETA || it.evaluacion.estado == EstadoFila.INVALIDA
        }

    val todasLasFilasCompletas: Boolean
        get() = noRealizada || filas.all { it.evaluacion.estado == EstadoFila.COMPLETA }
    val resultado: ResultadoPrueba?
        get() = peorResultado(filasCompletas.map { it.evaluacion.resultado })
}

data class FormularioProtocoloUiState(
    val cargando: Boolean = true,
    val guardando: Boolean = false,

    val idProtocolo: String? = null,
    val numero: String? = null,
    val estadoActual: EstadoProtocolo? = null,
    val motivoObservacion: String? = null,
    val fechaHoraPrueba: String = "",
    val tecnicoNombre: String = "",
    val rolUsuario: String = "",

    val visitas: List<Visita> = emptyList(),
    val visitaSeleccionada: Visita? = null,
    val equipos: List<Equipo> = emptyList(),
    val equipoSeleccionado: Equipo? = null,

    val temperatura: String = "",
    val humedad: String = "",
    val errorTemperatura: String? = null,
    val errorHumedad: String? = null,

    val instrumentos: List<Instrumento> = emptyList(),
    val secciones: List<SeccionPruebaUi> = emptyList(),
    val anteriores: Map<String, MedicionAnterior> = emptyMap(),

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
