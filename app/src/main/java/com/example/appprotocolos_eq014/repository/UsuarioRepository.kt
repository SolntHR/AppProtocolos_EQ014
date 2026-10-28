package com.example.appprotocolos_eq014.repository

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
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

    suspend fun obtenerPorCorreo(correo: String): Usuario? = withContext(Dispatchers.IO) {
        buscarUno("$consultaBase WHERE lower(u.correo) = lower(?)", arrayOf(correo.trim()))
    }

    suspend fun actualizarContrasena(idUsuario: Long, contrasena: String): Boolean = withContext(Dispatchers.IO) {
        val valores = ContentValues().apply { put("password_hash", contrasena) }
        dbHelper.writableDatabase.update(
            "USUARIO", valores, "id_usuario = ?", arrayOf(idUsuario.toString())
        ) > 0
    }

    suspend fun listarUsuarios(): List<Usuario> = withContext(Dispatchers.IO) {
        val cursor = dbHelper.readableDatabase.rawQuery(
            "$consultaBase ORDER BY u.id_rol, u.nombre",
            null
        )
        val usuarios = mutableListOf<Usuario>()
        while (cursor.moveToNext()) {
            usuarios.add(cursor.aUsuario())
        }
        cursor.close()
        usuarios
    }

    suspend fun cambiarActivo(idUsuario: Long, activo: Boolean): Boolean = withContext(Dispatchers.IO) {
        val valores = ContentValues().apply { put("activo", if (activo) 1 else 0) }
        dbHelper.writableDatabase.update(
            "USUARIO", valores, "id_usuario = ?", arrayOf(idUsuario.toString())
        ) > 0
    }

    suspend fun existeCorreo(correo: String, excluirId: Long = -1): Boolean = withContext(Dispatchers.IO) {
        existe("SELECT 1 FROM USUARIO WHERE lower(correo) = lower(?) AND id_usuario <> ?", correo, excluirId)
    }

    suspend fun existeRut(rut: String, excluirId: Long = -1): Boolean = withContext(Dispatchers.IO) {
        existe("SELECT 1 FROM USUARIO WHERE upper(rut) = upper(?) AND id_usuario <> ?", rut, excluirId)
    }

    suspend fun crearUsuario(
        rolCodigo: String,
        nombre: String,
        rut: String,
        correo: String,
        telefono: String,
        contactoNombre: String?,
        contactoCargo: String?,
        contrasenaInicial: String
    ): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cursorRol = db.rawQuery("SELECT id_rol FROM ROL WHERE codigo = ?", arrayOf(rolCodigo))
        val idRol = if (cursorRol.moveToFirst()) cursorRol.getLong(0) else -1L
        cursorRol.close()
        if (idRol == -1L) return@withContext -1L

        val valores = ContentValues().apply {
            put("id_rol", idRol)
            put("nombre", nombre)
            put("rut", rut)
            put("correo", correo)
            put("telefono", telefono)
            put("password_hash", contrasenaInicial)
            put("activo", 1)
            put("contacto_nombre", contactoNombre)
            put("contacto_cargo", contactoCargo)
        }
        try {
            db.insertOrThrow("USUARIO", null, valores)
        } catch (e: SQLiteConstraintException) {
            -1L
        }
    }

    private fun existe(query: String, valor: String, excluirId: Long): Boolean {
        val cursor = dbHelper.readableDatabase.rawQuery(query, arrayOf(valor.trim(), excluirId.toString()))
        val encontrado = cursor.moveToFirst()
        cursor.close()
        return encontrado
    }

    suspend fun actualizarPerfil(
        idUsuario: Long,
        nombre: String,
        rut: String,
        telefono: String,
        contactoNombre: String?,
        contactoCargo: String?
    ): Boolean = withContext(Dispatchers.IO) {
        val valores = ContentValues().apply {
            put("nombre", nombre)
            put("rut", rut)
            put("telefono", telefono)
            put("contacto_nombre", contactoNombre)
            put("contacto_cargo", contactoCargo)
        }
        try {
            dbHelper.writableDatabase.update(
                "USUARIO", valores, "id_usuario = ?", arrayOf(idUsuario.toString())
            ) > 0
        } catch (e: SQLiteConstraintException) {
            false
        }
    }

    suspend fun actualizarFoto(idUsuario: Long, rutaFoto: String?): Boolean = withContext(Dispatchers.IO) {
        val valores = ContentValues().apply { put("foto_perfil", rutaFoto) }
        dbHelper.writableDatabase.update(
            "USUARIO", valores, "id_usuario = ?", arrayOf(idUsuario.toString())
        ) > 0
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
        respuestaSeguridad = getString(getColumnIndexOrThrow("respuesta_seguridad")),
        fotoPerfil = getString(getColumnIndexOrThrow("foto_perfil"))
    )
}
