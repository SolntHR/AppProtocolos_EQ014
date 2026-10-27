package com.example.appprotocolos_eq014.model

data class LoginUiState(
    val correo: String = "",
    val contrasena: String = "",
    val contrasenaVisible: Boolean = false,
    val errorCorreo: String? = null,
    val errorContrasena: String? = null,
    val isLoading: Boolean = false,
    val loginExitoso: Boolean = false
)