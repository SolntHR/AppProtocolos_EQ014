package com.example.appprotocolos_eq014.util

import com.example.appprotocolos_eq014.model.reglas.ReglasProtocolo
import com.example.appprotocolos_eq014.model.uistate.FormularioProtocoloUiState

object ValidacionesProtocolo {
    const val PASO_VISITA = 0
    const val PASO_CONDICIONES = 1
    const val PASO_PRUEBAS = 2
    const val PASO_CIERRE = 3

    fun errorDelPaso(s: FormularioProtocoloUiState, paso: Int, registrar: Boolean): String? = when (paso) {
        PASO_VISITA -> when {
            s.visitaSeleccionada == null -> "Selecciona la visita (OT)."
            s.equipoSeleccionado == null -> "Selecciona el equipo."
            else -> null
        }
        PASO_CONDICIONES -> when {
            fueraDeRango(s.temperatura, -30.0..60.0) || fueraDeRango(s.humedad, 0.0..100.0) ->
                "Revisa la temperatura y la humedad."
            registrar && (ReglasProtocolo.numero(s.temperatura) == null || ReglasProtocolo.numero(s.humedad) == null) ->
                "La temperatura y la humedad son obligatorias para registrar en terreno (HU-02)."
            else -> null
        }
        PASO_PRUEBAS -> when {
            s.secciones.any { it.filasConProblema } ->
                "Hay mediciones incompletas o fuera de rango. Revisa las filas marcadas en rojo."
            s.secciones.any { it.errorInstrumento != null } -> "Revisa el instrumento de cada prueba."
            s.secciones.any { it.faltaMotivo } -> "Indica el motivo de cada prueba no realizada."
            registrar && s.secciones.any { !it.todasLasFilasCompletas } ->
                "Cada prueba debe quedar completa o marcada como No realizada con su motivo."
            registrar && s.secciones.none { it.filasCompletas.isNotEmpty() } ->
                "Para registrar en terreno se requiere al menos una prueba completa (RN-04)."
            else -> null
        }
        PASO_CIERRE -> when {
            registrar && s.visitaSeleccionada?.incluyeMantenimiento == true && s.descripcionMantenimiento.isBlank() ->
                "Describe el mantenimiento realizado."
            else -> null
        }
        else -> null
    }

    private fun fueraDeRango(valor: String, rango: ClosedFloatingPointRange<Double>): Boolean =
        valor.isNotBlank() && ReglasProtocolo.numero(valor).let { it == null || it !in rango }

    fun primerError(s: FormularioProtocoloUiState, registrar: Boolean): Pair<Int, String>? =
        FormularioProtocoloUiState.PASOS.indices.firstNotNullOfOrNull { paso ->
            errorDelPaso(s, paso, registrar)?.let { paso to it }
        }
}