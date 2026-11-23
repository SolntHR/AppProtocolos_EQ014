package com.example.appprotocolos_eq014.repository

import android.database.Cursor
import com.example.appprotocolos_eq014.data.InduztekDbHelper
import com.example.appprotocolos_eq014.model.entidades.Equipo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CatalogoRepository(private val dbHelper: InduztekDbHelper) {

    suspend fun listarEquipos(idCliente: Long? = null): List<Equipo> = withContext(Dispatchers.IO) {
        val filtro = if (idCliente == null) "" else "WHERE i.id_usuario_cliente = ?"
        val args = idCliente?.let { arrayOf(it.toString()) }
        dbHelper.readableDatabase.rawQuery(
            """
            SELECT e.*, t.nombre AS tipo_nombre, i.nombre AS instalacion_nombre, c.nombre AS cliente_nombre
            FROM EQUIPO e
            INNER JOIN TIPO_EQUIPO t ON t.id_tipo_equipo = e.id_tipo_equipo
            INNER JOIN INSTALACION i ON i.id_instalacion = e.id_instalacion
            INNER JOIN USUARIO c ON c.id_usuario = i.id_usuario_cliente
            $filtro
            ORDER BY c.nombre COLLATE NOCASE, i.nombre COLLATE NOCASE, e.tag COLLATE NOCASE
            """.trimIndent(), args
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    fun texto(columna: String) = cursor.getString(cursor.getColumnIndexOrThrow(columna))
                    fun entero(columna: String) = cursor.getLong(cursor.getColumnIndexOrThrow(columna))
                    fun real(columna: String): Double? {
                        val indice = cursor.getColumnIndexOrThrow(columna)
                        return if (cursor.isNull(indice)) null else cursor.getDouble(indice)
                    }
                    val indiceAnio = cursor.getColumnIndexOrThrow("anio")
                    add(Equipo(
                        id = entero("id_equipo"), instalacionId = entero("id_instalacion"),
                        tipoEquipoId = entero("id_tipo_equipo"), tag = texto("tag"),
                        marca = texto("marca"), modelo = texto("modelo"), numeroSerie = texto("numero_serie"),
                        anio = if (cursor.isNull(indiceAnio)) null else cursor.getInt(indiceAnio),
                        potenciaKva = real("potencia_kva"), tensiones = texto("tensiones"),
                        corriente = real("corriente"), maxContactosUohm = real("max_contactos_uohm"),
                        observaciones = texto("observaciones"), tipoEquipoNombre = texto("tipo_nombre"),
                        instalacionNombre = texto("instalacion_nombre"), clienteNombre = texto("cliente_nombre")
                    ))
                }
            }
        }
    }

    suspend fun obtenerEquiposPorCliente(idCliente: Long): List<Equipo> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val equipos = mutableListOf<Equipo>()

        val query = """
            SELECT e.id_equipo, e.id_instalacion, e.id_tipo_equipo, e.tag, e.marca, e.numero_serie,
                   t.nombre as tipo_nombre, i.nombre as instalacion_nombre
            FROM EQUIPO e
            INNER JOIN TIPO_EQUIPO t ON e.id_tipo_equipo = t.id_tipo_equipo
            INNER JOIN INSTALACION i ON e.id_instalacion = i.id_instalacion
            WHERE i.id_usuario_cliente = ?
            ORDER BY e.tag
        """

        val cursor: Cursor = db.rawQuery(query, arrayOf(idCliente.toString()))

        while (cursor.moveToNext()) {
            equipos.add(
                Equipo(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id_equipo")),
                    instalacionId = cursor.getLong(cursor.getColumnIndexOrThrow("id_instalacion")),
                    tipoEquipoId = cursor.getLong(cursor.getColumnIndexOrThrow("id_tipo_equipo")),
                    tag = cursor.getString(cursor.getColumnIndexOrThrow("tag")),
                    marca = cursor.getString(cursor.getColumnIndexOrThrow("marca")),
                    numeroSerie = cursor.getString(cursor.getColumnIndexOrThrow("numero_serie")),
                    tipoEquipoNombre = cursor.getString(cursor.getColumnIndexOrThrow("tipo_nombre")),
                    instalacionNombre = cursor.getString(cursor.getColumnIndexOrThrow("instalacion_nombre"))
                )
            )
        }
        cursor.close()
        equipos
    }
}
