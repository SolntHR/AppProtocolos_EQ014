package com.example.appprotocolos_eq014.model

enum class RolPersonal(val codigo: String, val etiqueta: String) {
    TECNICO("TECNICO", "Técnico"),
    INGENIERO("INGENIERO", "Ingeniero"),
    JEFE_TECNICO("JEFE_TECNICO", "Jefe técnico")
}

data class FichaUsuarioUiState(
    val isLoading: Boolean = false,
    val esNuevo: Boolean = true,
    val esCliente: Boolean = false,
    val rolPersonal: RolPersonal = RolPersonal.TECNICO,
    val rolNombre: String = "",
    val nombre: String = "",
    val rut: String = "",
    val empresa: String = "",
    val cargo: String = "",
    val telefono: String = "",
    val correo: String = "",
    val errorNombre: String? = null,
    val errorRut: String? = null,
    val errorEmpresa: String? = null,
    val errorCargo: String? = null,
    val errorTelefono: String? = null,
    val errorCorreo: String? = null,
    val errorGeneral: String? = null,
    val guardando: Boolean = false,
    val guardadoExitoso: Boolean = false
)
