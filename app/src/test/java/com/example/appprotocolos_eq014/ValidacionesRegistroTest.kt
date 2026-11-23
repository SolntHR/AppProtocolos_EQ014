package com.example.appprotocolos_eq014

import com.example.appprotocolos_eq014.model.registro.DatosRegistro
import com.example.appprotocolos_eq014.model.uistate.PreguntasSeguridad
import com.example.appprotocolos_eq014.util.ValidacionesRegistro
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

    @Test
    fun telefonoNoRecortaDatosInvalidosParaAceptarlos() {
        listOf("+56 2 1234 5678", "+54 9 1234 5678", "9123456789", "abc912345678").forEach {
            assertNotNull(it, ValidacionesRegistro.validar(datosValidos.copy(telefono = it)).telefono)
        }
    }

    @Test
    fun nombreExigeApellidoYCorreoTieneFormatoValido() {
        assertNotNull(ValidacionesRegistro.validar(datosValidos.copy(nombre = "Pedro")).nombre)
        assertNotNull(ValidacionesRegistro.validar(datosValidos.copy(nombre = "Pedro 123")).nombre)
        assertNotNull(ValidacionesRegistro.validar(datosValidos.copy(correoPersonal = "correo@")).correoPersonal)
    }

    @Test
    fun preguntaDebePertenecerAlCatalogoYRespuestaNoPuedeSerVacia() {
        assertNotNull(ValidacionesRegistro.validar(datosValidos.copy(preguntaSeguridad = "Pregunta inventada")).preguntaSeguridad)
        assertNotNull(ValidacionesRegistro.validar(datosValidos.copy(respuestaSeguridad = "   ")).respuestaSeguridad)
    }

    @Test
    fun sugerenciaDeCorreoUsaPrimerApellidoYEliminaTildes() {
        assertEquals("psoto@induztek.ejemplo", ValidacionesRegistro.correoInternoSugerido("Pedro Soto Vera"))
        assertEquals("mmazzo@induztek.ejemplo", ValidacionesRegistro.correoInternoSugerido("Matías Javier Mazzo Velásquez"))
        assertEquals("jperez@induztek.ejemplo", ValidacionesRegistro.correoInternoSugerido("  Juan   Pérez  "))
        assertEquals("", ValidacionesRegistro.correoInternoSugerido("Pedro"))
    }
}
