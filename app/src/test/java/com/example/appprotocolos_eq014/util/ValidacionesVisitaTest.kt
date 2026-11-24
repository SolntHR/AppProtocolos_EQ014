package com.example.appprotocolos_eq014.util

import com.example.appprotocolos_eq014.model.uistate.FormularioVisitaUiState
import com.example.appprotocolos_eq014.model.uistate.OpcionCatalogo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ValidacionesVisitaTest {

    private lateinit var visitaCompleta: FormularioVisitaUiState

    @Before
    fun preparar() {
        visitaCompleta = FormularioVisitaUiState(
            numeroOt = "ot-2026-1300",
            instalacion = OpcionCatalogo(1, "Planta Lampa · Envasadora del Pacífico S.A."),
            fecha = Formato.hoyPantalla(),
            ingeniero = OpcionCatalogo(5, "Andrea Contreras Silva · Ingeniero"),
            participantes = setOf(1L, 2L)
        )
    }

    @Test
    fun validar_visitaCompletaParaHoy_noTieneErrores() {
        val errores = ValidacionesVisita.validar(visitaCompleta)

        assertTrue(errores.isEmpty())
    }

    @Test
    fun validar_otMalEscritaYSinTecnicos_marcaAmbosCampos() {
        val visita = visitaCompleta.copy(numeroOt = "1300", participantes = emptySet())

        val errores = ValidacionesVisita.validar(visita)

        assertEquals("Usa el formato OT-2026-0001", errores[ValidacionesVisita.CAMPO_OT])
        assertEquals("Asigna al menos un técnico", errores[ValidacionesVisita.CAMPO_PARTICIPANTES])
    }
}