package com.example.appprotocolos_eq014.repository

import android.content.ContentValues
import android.database.Cursor
import com.example.appprotocolos_eq014.data.InduztekDbHelper
import com.example.appprotocolos_eq014.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsuarioRepository(private val dbHelper: InduztekDbHelper) {

    suspend fun login(correo: String, pass: String): Usuario? = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query("USUARIO", null, "correo = ? AND password_hash = ?", arrayOf(correo, pass), null, null, null)

        var usuario: Usuario? = null
        if (cursor.moveToFirst()) {
            usuario = Usuario(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id_usuario")),
                idRol = cursor.getInt(cursor.getColumnIndexOrThrow("id_rol")),
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
