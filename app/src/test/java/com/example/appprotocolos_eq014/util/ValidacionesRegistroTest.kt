package com.example.appprotocolos_eq014.util

import com.example.appprotocolos_eq014.model.registro.DatosRegistro
import com.example.appprotocolos_eq014.model.uistate.PreguntasSeguridad
import org.junit.Assert.*
import org.junit.Test

class ValidacionesRegistroTest {
    private val datosValidos = DatosRegistro(
        nombre = "Pedro Soto Vera",
        rut = "12.345.678-5",
        telefono = "+56 9 1234 5678",
        correoPersonal = "prueba@correo.cl",
        preguntaSeguridad = PreguntasSeguridad.lista.first(),
        respuestaSeguridad = "Respuesta de prueba"
    )

    @Test
    fun datosCompletosSonValidos() {
        assertFalse(ValidacionesRegistro.validar(datosValidos).hayErrores)
    }

    @Test
    fun formularioVacioExigeTodosLosCampos() {
        val errores = ValidacionesRegistro.validar(DatosRegistro())
        assertNotNull(errores.nombre)
        assertNotNull(errores.rut)
        assertNotNull(errores.telefono)
        assertNotNull(errores.correoPersonal)
        assertNotNull(errores.preguntaSeguridad)
        assertNotNull(errores.respuestaSeguridad)
    }

    @Test
    fun rutRechazaDigitoIncorrectoYLongitudImposible() {
        listOf("12.345.678-4", "1-9", "12345678905", "12.345.67X-5").forEach {
            assertNotNull(it, ValidacionesRegistro.validar(datosValidos.copy(rut = it)).rut)
        }
        assertNull(ValidacionesRegistro.validar(datosValidos.copy(rut = "123456785")).rut)
    }

    @Test
    fun telefonosConOSinPrefijoProducenElMismoNumero() {
        listOf("912345678", "9 1234 5678", "+56 9 1234 5678", "56912345678").forEach {
            assertEquals("+56 9 1234 5678", ValidacionesRegistro.telefonoCompleto(it))
            assertNull(ValidacionesRegistro.validar(datosValidos.copy(telefono = it)).telefono)
        }
    }

}
