package com.example.appprotocolos_eq014.viewmodel

import com.example.appprotocolos_eq014.data.FakeUsuarioApi
import com.example.appprotocolos_eq014.repository.UsuarioRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel

    @Before
    fun preparar() {
        viewModel = LoginViewModel(
            usuarioRepository = UsuarioRepository(FakeUsuarioApi()),
            guardarSesion = { _, _ -> }
        )
    }

    @Test
    fun validar_conCamposVacios_devuelveFalseYMuestraErrores() {
        val resultado = viewModel.validar()

        assertFalse(resultado)
        assertEquals("Ingresa tu correo electrónico", viewModel.uiState.value.errorCorreo)
        assertEquals("Ingresa tu contraseña", viewModel.uiState.value.errorContrasena)
    }

    @Test
    fun validar_conDatosCompletos_devuelveTrueSinErrores() {
        viewModel.onCorreoCambia("respinoza@induztek.ejemplo")
        viewModel.onContrasenaCambia("Induztek#2026")

        val resultado = viewModel.validar()

        assertTrue(resultado)
        assertNull(viewModel.uiState.value.errorCorreo)
        assertNull(viewModel.uiState.value.errorContrasena)
    }
}