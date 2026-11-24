package com.example.appprotocolos_eq014.model.uistate

import android.graphics.Bitmap

enum class PestanaUsuarios {
    PERSONAL,
    CLIENTES
}

enum class FiltroRolUsuario(val etiqueta: String, val rolCodigo: String?) {
    TODOS("Todos", null),
    PENDIENTES("Pendientes", "SIN_ASIGNAR"),
    TECNICOS("Técnicos", "TECNICO"),
    INGENIEROS("Ingenieros", "INGENIERO"),
    JEFES("Jefes técnicos", "JEFE_TECNICO")
}

data class UsuarioFila(
    val id: Long,
    val nombre: String,
    val detalle: String,
    val activo: Boolean,
    val esUsuarioActual: Boolean,
    val foto: Bitmap? = null,
    val rolCodigo: String = ""
) {
    val pendiente: Boolean get() = rolCodigo == "SIN_ASIGNAR"
}

data class GestionUsuariosUiState(
    val isLoading: Boolean = true,
    val pestana: PestanaUsuarios = PestanaUsuarios.PERSONAL,
    val personal: List<UsuarioFila> = emptyList(),
    val clientes: List<UsuarioFila> = emptyList(),
    val activos: Int = 0,
    val inactivos: Int = 0,
    val busqueda: String = "",
    val filtroRol: FiltroRolUsuario = FiltroRolUsuario.TODOS,
    val mensaje: String? = null
) {
    val usuariosVisibles: List<UsuarioFila>
        get() {
            val texto = busqueda.trim()
            val base = if (pestana == PestanaUsuarios.PERSONAL) {
                personal.filter { filtroRol.rolCodigo == null || it.rolCodigo == filtroRol.rolCodigo }
            } else {
                clientes
            }
            return base.filter {
                texto.isEmpty() || it.nombre.contains(texto, ignoreCase = true) || it.detalle.contains(texto, ignoreCase = true)
            }
        }

    fun cantidadCon(filtro: FiltroRolUsuario): Int =
        personal.count { filtro.rolCodigo == null || it.rolCodigo == filtro.rolCodigo }
}