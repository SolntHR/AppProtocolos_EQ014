package com.example.appprotocolos_eq014.data

import android.content.Context
import androidx.core.content.edit

class SesionPreferencias (context: Context) {

    // Abre el archivo xml
    private val prefs = context.getSharedPreferences("sesion_induztek", Context.MODE_PRIVATE)

    companion object {
        private const val CLAVE_ID_USUARIO = "id_usuario"
        private const val CLAVE_ROL_USUARIO = "rol_usuario"
        private const val SIN_SESION = -1L
    }

    // Guarda el ID y el código del rol al hacer Login
    fun guardarSesion(idUsuario: Long, codigoRol: String) {
        prefs.edit {
            putLong(CLAVE_ID_USUARIO, idUsuario)
            putString(CLAVE_ROL_USUARIO, codigoRol)
        }
    }

    // Devuelve el ID del usuario actual, o -1 si nadie ha iniciado sesión
    fun leerIdUsuario(): Long = prefs.getLong(CLAVE_ID_USUARIO, SIN_SESION)

    // Devuelve el rol para validar permisos visuales
    fun leerRolUsuario(): String? = prefs.getString(CLAVE_ROL_USUARIO, null)

    // Limpia las preferencias al cerrar sesión
    fun cerrarSesion() {
        prefs.edit { clear() }
    }


}