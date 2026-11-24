package com.example.appprotocolos_eq014.util

import com.example.appprotocolos_eq014.model.entidades.Equipo
import com.example.appprotocolos_eq014.model.entidades.Visita
import com.example.appprotocolos_eq014.model.uistate.FormularioProtocoloUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ValidacionesProtocoloTest {

    private lateinit var borrador: FormularioProtocoloUiState

    @Before
    fun preparar() {
        borrador = FormularioProtocoloUiState(
            cargando = false,
            visitaSeleccionada = Visita(id = 6, numeroOt = "OT-2026-1215", instalacionId = 1, fecha = "2026-11-24", tipoVisita = "SOLO_PRUEBAS"),
            equipoSeleccionado = Equipo(id = 1, instalacionId = 1, tipoEquipoId = 1, tag = "TR-01")
        )
    }

    @Test
    fun primerError_borradorConVisitaYEquipo_noTieneErrores() {
        val error = ValidacionesProtocolo.primerError(borrador, registrar = false)

        assertNull(error)
    }

    @Test
    fun primerError_registrarSinTemperatura_vuelveAlPasoCondiciones() {
        val estado = borrador.copy(humedad = "45")

        val error = ValidacionesProtocolo.primerError(estado, registrar = true)

        assertEquals(ValidacionesProtocolo.PASO_CONDICIONES, error?.first)
        assertEquals("La temperatura y la humedad son obligatorias para registrar en terreno (HU-02).", error?.second)
    }
}