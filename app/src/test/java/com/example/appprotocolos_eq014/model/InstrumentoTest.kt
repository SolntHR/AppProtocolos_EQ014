package com.example.appprotocolos_eq014.model

import com.example.appprotocolos_eq014.model.entidades.EstadoCalibracion
import com.example.appprotocolos_eq014.model.entidades.Instrumento
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class InstrumentoTest {

    private fun instrumentoQueVenceEn(dias: Long?) = Instrumento(
        id = 1,
        nombreTipo = "Megóhmetro 5 kV",
        marca = "Megger",
        modelo = "MIT525",
        numeroSerie = "MIT-0045821",
        fechaVencimiento = dias?.let { LocalDate.now().plusDays(it).toString() }
    )

    @Test
    fun estadoCalibracion_venceEn10Dias_quedaPorVencer() {
        val instrumento = instrumentoQueVenceEn(10)

        assertEquals(EstadoCalibracion.POR_VENCER, instrumento.estadoCalibracion)
    }

    @Test
    fun estadoCalibracion_vencidoOSinFecha_seMarcaCorrectamente() {
        val vencido = instrumentoQueVenceEn(-1)
        val sinCalibracion = instrumentoQueVenceEn(null)

        assertEquals(EstadoCalibracion.VENCIDA, vencido.estadoCalibracion)
        assertEquals(EstadoCalibracion.SIN_CALIBRACION, sinCalibracion.estadoCalibracion)
    }
}