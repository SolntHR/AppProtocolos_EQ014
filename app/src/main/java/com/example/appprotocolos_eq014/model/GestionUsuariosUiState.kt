package com.example.appprotocolos_eq014.model

import android.graphics.Bitmap

enum class PestanaUsuarios {
    PERSONAL,
    CLIENTES
}

data class UsuarioFila(
    val id: Long,
    val nombre: String,
    val detalle: String,
    val activo: Boolean,
    val esUsuarioActual: Boolean,
    val foto: Bitmap? = null
)

data class GestionUsuariosUiState(
    val isLoading: Boolean = true,
    val pestana: PestanaUsuarios = PestanaUsuarios.PERSONAL,
    val personal: List<UsuarioFila> = emptyList(),
    val clientes: List<UsuarioFila> = emptyList(),
    val activos: Int = 0,
    val inactivos: Int = 0,
    val mensaje: String? = null
) {
    val usuariosVisibles: List<UsuarioFila>
        get() = if (pestana == PestanaUsuarios.PERSONAL) personal else clientes
}
