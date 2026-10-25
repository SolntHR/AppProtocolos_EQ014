package com.example.appprotocolos_eq014.model

// Almacena exclusivamente el estado actual del formulario
data class LoginUiState(
    val correo: String = "",
    val contrasena: String = "",
    val contrasenaVisible: Boolean = false,
    val errorCorreo: String? = null,
    val errorContrasena: String? = null,
    val loginExitoso: Boolean = false
)