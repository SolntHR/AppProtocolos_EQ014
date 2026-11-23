package com.example.appprotocolos_eq014

import com.example.appprotocolos_eq014.model.uistate.PreguntasSeguridad
import com.example.appprotocolos_eq014.repository.RegistroUsuarioRepository
import com.example.appprotocolos_eq014.repository.ResultadoRegistroUsuario
import com.example.appprotocolos_eq014.repository.SolicitudRegistroUsuario
import com.example.appprotocolos_eq014.viewmodel.RegistroViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistroViewModelTest {
    @Test
    fun formularioIncompletoNuncaSeEnviaAlServicio() {
        var llamadas = 0
        val repository = object : RegistroUsuarioRepository {
            override suspend fun registrar(solicitud: SolicitudRegistroUsuario): ResultadoRegistroUsuario {
                llamadas++
                error("Un formulario incompleto no debe llegar al servicio")
            }
        }
        val viewModel = RegistroViewModel(repository)
        viewModel.onNombreCambia("Pedro Soto")
        viewModel.onRegistrar()

        assertEquals(0, llamadas)
        assertTrue(viewModel.uiState.value.errores.hayErrores)
        assertNull(viewModel.uiState.value.correoCreado)
    }

    @Test
    fun cambiarPreguntaNoReutilizaLaRespuestaAnterior() {
        val viewModel = RegistroViewModel(repositorySinUso())
        viewModel.onPreguntaCambia(PreguntasSeguridad.lista.first())
        viewModel.onRespuestaCambia("Respuesta para la primera pregunta")
        viewModel.onPreguntaCambia(PreguntasSeguridad.lista.last())

        assertEquals("", viewModel.uiState.value.datos.respuestaSeguridad)
        viewModel.onRegistrar()
        assertNotNull(viewModel.uiState.value.errores.respuestaSeguridad)
    }

    private fun repositorySinUso() = object : RegistroUsuarioRepository {
        override suspend fun registrar(solicitud: SolicitudRegistroUsuario): ResultadoRegistroUsuario =
            error("Esta prueba no debe enviar datos al servicio")
    }
}
