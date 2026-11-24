package com.example.appprotocolos_eq014.repository

import com.example.appprotocolos_eq014.data.FakeUsuarioApi
import com.example.appprotocolos_eq014.util.Seguridad
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class RecuperacionCorreoRepositoryTest {

    private val correoAcceso = "respinoza@induztek.ejemplo"

    @Test
    fun solicitarCodigo_usaCorreoDeAcceso() = runBlocking {
        val api = FakeUsuarioApi()
        val repository = UsuarioRepository(api)

        repository.solicitarCodigo(" $correoAcceso ")

        assertEquals(correoAcceso, api.ultimaSolicitudCodigo?.correo)
    }

    @Test
    fun verificarCodigo_enviaCorreoDeAccesoYCodigo() = runBlocking {
        val api = FakeUsuarioApi()
        val repository = UsuarioRepository(api)

        repository.verificarCodigo(correoAcceso, " 123456 ")

        assertEquals(correoAcceso, api.ultimaVerificacionCodigo?.correo)
        assertEquals("123456", api.ultimaVerificacionCodigo?.codigo)
    }

    @Test
    fun restablecerConCodigo_cifraLaClaveUsandoElCorreoDeAcceso() = runBlocking {
        val api = FakeUsuarioApi()
        val repository = UsuarioRepository(api)
        val nuevaContrasena = "Segura#2026"

        repository.restablecerConCodigo(correoAcceso, "123456", nuevaContrasena)

        val request = requireNotNull(api.ultimoRestablecimientoCodigo)
        assertEquals(correoAcceso, request.correo)
        assertEquals("123456", request.codigo)
        assertEquals(Seguridad.cifrarContrasena(correoAcceso, nuevaContrasena), request.nuevaContrasena)
    }

    @Test
    fun recuperacionPorPregunta_sigueDisponible() = runBlocking {
        val api = FakeUsuarioApi()
        val repository = UsuarioRepository(api)
        val pregunta = "¿Cuál es el nombre de tu primera mascota?"

        repository.verificarPregunta(correoAcceso, pregunta, "Luna")
        repository.restablecerConPregunta(correoAcceso, pregunta, "Luna", "Segura#2026")

        assertEquals(correoAcceso, api.ultimaVerificacionPregunta?.correo)
        assertEquals(pregunta, api.ultimaVerificacionPregunta?.pregunta)
        assertEquals(correoAcceso, api.ultimoRestablecimientoPregunta?.correo)
        assertEquals(
            Seguridad.cifrarContrasena(correoAcceso, "Segura#2026"),
            api.ultimoRestablecimientoPregunta?.nuevaContrasena
        )
    }
}
