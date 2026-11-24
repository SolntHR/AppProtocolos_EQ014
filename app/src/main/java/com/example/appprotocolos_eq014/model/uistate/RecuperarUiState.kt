package com.example.appprotocolos_eq014.model.uistate

enum class MetodoRecuperacion(val texto: String) {
    CORREO("Por correo"),
    PREGUNTA("Por pregunta")
}

enum class PasoRecuperacion {
    VERIFICAR,
    NUEVA_CONTRASENA,
    LISTO
}

object PreguntasSeguridad {
    val lista = listOf(
        "¿Cuál es el nombre de tu primera mascota?",
        "¿En qué ciudad nacieron tus padres?",
        "¿Cuál es tu comida favorita?",
        "¿Cuál fue el modelo de tu primer auto?",
        "¿Cuál es el nombre de tu mejor amigo de la infancia?"
    )
}

data class RecuperarUiState(
    val paso: PasoRecuperacion = PasoRecuperacion.VERIFICAR,
    val metodo: MetodoRecuperacion = MetodoRecuperacion.CORREO,
    val correo: String = "",
    val codigo: String = "",
    val codigoEnviado: Boolean = false,
    val mensajeCodigo: String? = null,
    val errorCodigo: String? = null,
    val preguntas: List<String> = PreguntasSeguridad.lista,
    val pregunta: String? = null,
    val respuesta: String = "",
    val nuevaContrasena: String = "",
    val confirmacion: String = "",
    val contrasenaVisible: Boolean = false,
    val errorCorreo: String? = null,
    val errorPregunta: String? = null,
    val errorRespuesta: String? = null,
    val errorGeneral: String? = null,
    val errorNueva: String? = null,
    val errorConfirmacion: String? = null,
    val cargando: Boolean = false
)
