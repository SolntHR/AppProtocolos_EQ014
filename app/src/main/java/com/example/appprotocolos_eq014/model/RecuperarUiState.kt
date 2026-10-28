package com.example.appprotocolos_eq014.model

enum class PasoRecuperacion {
    CORREO,
    PREGUNTA,
    NUEVA_CONTRASENA,
    LISTO
}

data class RecuperarUiState(
    val paso: PasoRecuperacion = PasoRecuperacion.CORREO,
    val correo: String = "",
    val pregunta: String = "",
    val respuesta: String = "",
    val nuevaContrasena: String = "",
    val confirmacion: String = "",
    val contrasenaVisible: Boolean = false,
    val errorCorreo: String? = null,
    val errorRespuesta: String? = null,
    val errorNueva: String? = null,
    val errorConfirmacion: String? = null,
    val cargando: Boolean = false
)
