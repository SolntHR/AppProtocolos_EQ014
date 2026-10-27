package com.example.appprotocolos_eq014.repository

import android.database.Cursor
import com.example.appprotocolos_eq014.data.InduztekDbHelper
import com.example.appprotocolos_eq014.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsuarioRepository(private val dbHelper: InduztekDbHelper) {

    suspend fun login(correo: String, pass: String): Usuario? = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val query = """
            SELECT u.*, r.codigo as rol_codigo 
            FROM USUARIO u 
            INNER JOIN ROL r ON u.id_rol = r.id_rol 
            WHERE u.correo = ? AND u.password_hash = ?
        """
        val cursor: Cursor = db.rawQuery(query, arrayOf(correo, pass))

        var usuario: Usuario? = null
        if (cursor.moveToFirst()) {
            usuario = Usuario(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id_usuario")),
                idRol = cursor.getInt(cursor.getColumnIndexOrThrow("id_rol")),
                rolCodigo = cursor.getString(cursor.getColumnIndexOrThrow("rol_codigo")),
                rut = cursor.getString(cursor.getColumnIndexOrThrow("rut")),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                correo = cursor.getString(cursor.getColumnIndexOrThrow("correo")),
                telefono = cursor.getString(cursor.getColumnIndexOrThrow("telefono")),
                passwordHash = cursor.getString(cursor.getColumnIndexOrThrow("password_hash")),
                activo = cursor.getInt(cursor.getColumnIndexOrThrow("activo")) == 1
            )
        }
        cursor.close()
        usuario
    }
}
