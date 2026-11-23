package com.example.appprotocolos_eq014.model.registro

data class DatosRegistro(
    val nombre: String = "",
    val rut: String = "",
    val telefono: String = "",
    val correoPersonal: String = "",
    val preguntaSeguridad: String? = null,
    val respuestaSeguridad: String = ""
)

data class ErroresRegistro(
    val nombre: String? = null,
    val rut: String? = null,
    val telefono: String? = null,
    val correoPersonal: String? = null,
    val preguntaSeguridad: String? = null,
    val respuestaSeguridad: String? = null
) {
    val hayErrores: Boolean
        get() = listOf(nombre, rut, telefono, correoPersonal, preguntaSeguridad, respuestaSeguridad).any { it != null }
}
