package com.example.appprotocolos_eq014.util

import com.example.appprotocolos_eq014.model.reglas.ReglasProtocolo
import com.example.appprotocolos_eq014.model.uistate.FormularioEquipoUiState
import com.example.appprotocolos_eq014.model.uistate.OpcionCatalogo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ValidacionesCatalogoTest {

    private lateinit var transformador: FormularioEquipoUiState

    @Before
    fun preparar() {
        transformador = FormularioEquipoUiState(
            instalacion = OpcionCatalogo(1, "Planta Lampa · Envasadora del Pacífico S.A."),
            tipo = OpcionCatalogo(ReglasProtocolo.TIPO_TRANSFORMADOR, "Transformador"),
            tag = "TR-03",
            potenciaKva = "1500",
            tensiones = "12"
        )
    }

    @Test
    fun validarEquipo_transformadorCompleto_noTieneErrores() {
        val errores = ValidacionesCatalogo.validarEquipo(transformador)

        assertTrue(errores.isEmpty())
    }

    @Test
    fun validarEquipo_sinTagYConTensionNoNumerica_marcaAmbosCampos() {
        val equipo = transformador.copy(tag = "", tensiones = "doce")

        val errores = ValidacionesCatalogo.validarEquipo(equipo)

        assertEquals("Ingresa el TAG del equipo", errores[ValidacionesCatalogo.CAMPO_TAG])
        assertEquals("Debe ser un número", errores[ValidacionesCatalogo.CAMPO_TENSIONES])
    }
}