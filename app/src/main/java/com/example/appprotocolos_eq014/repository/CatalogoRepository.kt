package com.example.appprotocolos_eq014.repository

import android.database.Cursor
import com.example.appprotocolos_eq014.data.InduztekDbHelper
import com.example.appprotocolos_eq014.model.Equipo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CatalogoRepository(private val dbHelper: InduztekDbHelper) {

    suspend fun obtenerEquiposPorCliente(idCliente: Long): List<Equipo> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val equipos = mutableListOf<Equipo>()

        val query = """
            SELECT e.id_equipo, e.tag, e.marca, e.numero_serie, t.nombre as tipo_nombre, i.nombre as instalacion_nombre 
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
                    tag = cursor.getString(cursor.getColumnIndexOrThrow("tag")),
                    tipoEquipoNombre = cursor.getString(cursor.getColumnIndexOrThrow("tipo_nombre")),
                    marca = cursor.getString(cursor.getColumnIndexOrThrow("marca")),
                    numeroSerie = cursor.getString(cursor.getColumnIndexOrThrow("numero_serie")),
                    instalacion = cursor.getString(cursor.getColumnIndexOrThrow("instalacion_nombre"))
                )
            )
        }
        cursor.close()
        equipos
    }
}