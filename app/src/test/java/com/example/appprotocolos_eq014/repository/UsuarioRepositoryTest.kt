package com.example.appprotocolos_eq014.repository

import com.example.appprotocolos_eq014.data.FakeUsuarioApi
import com.example.appprotocolos_eq014.util.Seguridad
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class UsuarioRepositoryTest {

    @Test
    fun cambiarRol_enviaJefeYContrasenaCifradaComoRequiereElMicroservicio() = runBlocking {
        val api = FakeUsuarioApi()
        val repository = UsuarioRepository(api)

        repository.cambiarRol(
            idUsuario = 25L,
            rolCodigo = "TECNICO",
            idJefe = 7L,
            correoJefe = "respinoza@induztek.ejemplo",
            contrasena = "Induztek#2026"
        )

        val solicitud = requireNotNull(api.ultimaSolicitudCambioRol)
        assertEquals("TECNICO", solicitud.rol)
        assertEquals(7L, solicitud.jefeId)
        assertEquals(
            Seguridad.cifrarContrasena("respinoza@induztek.ejemplo", "Induztek#2026"),
            solicitud.contrasena
        )
    }
}
