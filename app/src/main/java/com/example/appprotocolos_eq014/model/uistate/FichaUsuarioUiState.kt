package com.example.appprotocolos_eq014.model.uistate

enum class RolPersonal(val codigo: String, val etiqueta: String) {
    TECNICO("TECNICO", "Técnico"),
    INGENIERO("INGENIERO", "Ingeniero"),
    JEFE_TECNICO("JEFE_TECNICO", "Jefe técnico")
}

data class FichaUsuarioUiState(
    val isLoading: Boolean = false,
    val esNuevo: Boolean = true,
    val pendiente: Boolean = false,
    val esCliente: Boolean = false,
    val rolPersonal: RolPersonal = RolPersonal.TECNICO,
    val rolOriginal: RolPersonal? = null,
    val esMiCuenta: Boolean = false,
    val pedirContrasena: Boolean = false,
    val contrasenaJefe: String = "",
    val errorContrasenaJefe: String? = null,
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
){
    val puedeEditarRol: Boolean
        get() = esNuevo || pendiente || (!esCliente && !esMiCuenta)

    val cambiaRol: Boolean
        get() = !esNuevo && (pendiente || (!esCliente && rolPersonal != rolOriginal))
}
