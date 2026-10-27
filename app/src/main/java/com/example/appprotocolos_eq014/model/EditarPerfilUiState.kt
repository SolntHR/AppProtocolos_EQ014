package com.example.appprotocolos_eq014.model

data class EditarPerfilUiState(
    val isLoading: Boolean = true,
    val esCliente: Boolean = false,
    val puedeEditarDatosProtegidos: Boolean = false,
    val nombre: String = "",
    val rut: String = "",
    val rol: String = "",
    val empresa: String = "",
    val cargo: String = "",
    val telefono: String = "",
    val correo: String = "",
    val errorNombre: String? = null,
    val errorRut: String? = null,
    val errorCargo: String? = null,
    val errorTelefono: String? = null,
    val errorGeneral: String? = null,
    val guardando: Boolean = false,
    val guardadoExitoso: Boolean = false
)
