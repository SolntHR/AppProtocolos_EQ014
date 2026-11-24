package com.example.appprotocolos_eq014.util

import com.example.appprotocolos_eq014.model.reglas.EstadoFila
import com.example.appprotocolos_eq014.model.reglas.ReglasProtocolo
import com.example.appprotocolos_eq014.model.uistate.FormularioProtocoloUiState

object ValidacionesProtocolo {
    const val PASO_VISITA = 0
    const val PASO_CONDICIONES = 1
    const val COMPLETA_ESTE_DATO = "Completa este dato"

    fun faltantes(s: FormularioProtocoloUiState, registrar: Boolean): Map<Int, List<String>> {
        val mapa = sortedMapOf<Int, MutableList<String>>()
        fun agregar(paso: Int, dato: String) {
            mapa.getOrPut(paso) { mutableListOf() }.add(dato)
        }
        fun condicion(valor: String, rango: ClosedFloatingPointRange<Double>, nombre: String) {
            val numero = ReglasProtocolo.numero(valor)
            when {
                valor.isBlank() -> if (registrar) agregar(PASO_CONDICIONES, nombre)
                numero == null || numero !in rango -> agregar(PASO_CONDICIONES, "$nombre (no válida)")
            }
        }

        if (s.visitaSeleccionada == null) agregar(PASO_VISITA, "Visita (OT)")
        if (s.equipoSeleccionado == null) agregar(PASO_VISITA, "Equipo")
        condicion(s.temperatura, -30.0..60.0, "Temperatura")
        condicion(s.humedad, 0.0..100.0, "Humedad")

        s.secciones.forEachIndexed { indice, seccion ->
            val paso = indice + 2
            if (seccion.noRealizada) {
                if (seccion.faltaMotivo) agregar(paso, "Motivo de no realización")
                return@forEachIndexed
            }
            if (seccion.errorInstrumento != null || (registrar && seccion.instrumentoId == null)) agregar(paso, "Instrumento")
            seccion.filas.forEach { fila ->
                when (fila.evaluacion.estado) {
                    EstadoFila.INVALIDA -> agregar(paso, "${fila.elemento} (no válida)")
                    EstadoFila.INCOMPLETA -> agregar(paso, fila.elemento)
                    EstadoFila.VACIA -> if (registrar) agregar(paso, fila.elemento)
                    EstadoFila.COMPLETA -> Unit
                }
            }
        }

        if (registrar && s.visitaSeleccionada?.incluyeMantenimiento == true && s.descripcionMantenimiento.isBlank()) {
            agregar(s.pasoObservaciones, "Descripción del mantenimiento")
        }
        return mapa
    }

    fun primerError(s: FormularioProtocoloUiState, registrar: Boolean): Pair<Int, String>? {
        val primero = faltantes(s, registrar).entries.firstOrNull()
        if (primero != null) return primero.key to "Falta completar: ${primero.value.joinToString(", ")}."
        if (registrar && s.secciones.none { it.filasCompletas.isNotEmpty() }) {
            return s.pasoResumen to "Para registrar en terreno se requiere al menos una prueba completa (RN-04)."
        }
        return null
    }
}