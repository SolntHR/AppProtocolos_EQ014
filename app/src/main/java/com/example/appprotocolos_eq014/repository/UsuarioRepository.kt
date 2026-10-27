package com.example.appprotocolos_eq014.repository

import android.database.Cursor
import com.example.appprotocolos_eq014.data.InduztekDbHelper
import com.example.appprotocolos_eq014.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsuarioRepository(private val dbHelper: InduztekDbHelper) {

    private val consultaBase = """
        SELECT u.*, r.codigo AS rol_codigo, r.nombre AS rol_nombre
        FROM USUARIO u
        INNER JOIN ROL r ON u.id_rol = r.id_rol
    """

    suspend fun login(correo: String, pass: String): Usuario? = withContext(Dispatchers.IO) {
        buscarUno("$consultaBase WHERE u.correo = ? AND u.password_hash = ?", arrayOf(correo, pass))
    }

    suspend fun obtenerPorId(idUsuario: Long): Usuario? = withContext(Dispatchers.IO) {
        buscarUno("$consultaBase WHERE u.id_usuario = ?", arrayOf(idUsuario.toString()))
    }

    private fun buscarUno(query: String, argumentos: Array<String>): Usuario? {
        val cursor = dbHelper.readableDatabase.rawQuery(query, argumentos)
        val usuario = if (cursor.moveToFirst()) cursor.aUsuario() else null
        cursor.close()
        return usuario
    }

    private fun Cursor.aUsuario(): Usuario = Usuario(
        id = getInt(getColumnIndexOrThrow("id_usuario")),
        idRol = getInt(getColumnIndexOrThrow("id_rol")),
        rolCodigo = getString(getColumnIndexOrThrow("rol_codigo")),
        rolNombre = getString(getColumnIndexOrThrow("rol_nombre")),
        rut = getString(getColumnIndexOrThrow("rut")),
        nombre = getString(getColumnIndexOrThrow("nombre")),
        correo = getString(getColumnIndexOrThrow("correo")),
        telefono = getString(getColumnIndexOrThrow("telefono")),
        passwordHash = getString(getColumnIndexOrThrow("password_hash")),
        activo = getInt(getColumnIndexOrThrow("activo")) == 1,
        giro = getString(getColumnIndexOrThrow("giro")),
        contactoNombre = getString(getColumnIndexOrThrow("contacto_nombre")),
        contactoCargo = getString(getColumnIndexOrThrow("contacto_cargo")),
        preguntaSeguridad = getString(getColumnIndexOrThrow("pregunta_seguridad")),
        respuestaSeguridad = getString(getColumnIndexOrThrow("respuesta_seguridad"))
    )
}
