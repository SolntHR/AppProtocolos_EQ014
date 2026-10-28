package com.example.appprotocolos_eq014.repository

import android.content.ContentValues
import android.database.Cursor
import com.example.appprotocolos_eq014.data.InduztekDbHelper
import com.example.appprotocolos_eq014.model.EquipoDetalle
import com.example.appprotocolos_eq014.model.EstadoProtocolo
import com.example.appprotocolos_eq014.model.ProtocoloResumen
import com.example.appprotocolos_eq014.model.ResultadoPrueba
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProtocoloRepository(private val dbHelper: InduztekDbHelper) {

    suspend fun obtenerEquipo(idEquipo: Long): EquipoDetalle? = withContext(Dispatchers.IO) {
        val cursor = dbHelper.readableDatabase.rawQuery(
            """
            SELECT e.id_equipo, e.tag, t.nombre AS tipo, e.marca, e.modelo, e.numero_serie,
                   i.nombre AS instalacion, e.potencia_kva, e.tensiones
            FROM EQUIPO e
            INNER JOIN TIPO_EQUIPO t ON e.id_tipo_equipo = t.id_tipo_equipo
            INNER JOIN INSTALACION i ON e.id_instalacion = i.id_instalacion
            WHERE e.id_equipo = ?
            """,
            arrayOf(idEquipo.toString())
        )
        val equipo = if (cursor.moveToFirst()) {
            EquipoDetalle(
                id = cursor.getLong(0),
                tag = cursor.getString(1),
                tipo = cursor.getString(2),
                marca = cursor.getString(3),
                modelo = cursor.getString(4),
                numeroSerie = cursor.getString(5),
                instalacion = cursor.getString(6),
                potenciaKva = cursor.doubleONulo(7),
                tensionKv = cursor.getString(8)
            )
        } else {
            null
        }
        cursor.close()
        equipo
    }

    suspend fun historialEquipo(idEquipo: Long): List<ProtocoloResumen> = withContext(Dispatchers.IO) {
        val cursor = dbHelper.readableDatabase.rawQuery(
            """
            SELECT p.id_protocolo, p.numero, p.fecha_hora_prueba, p.id_estado, p.observaciones,
                   (SELECT MAX(pr.id_resultado) FROM PRUEBA pr
                    WHERE pr.id_protocolo = p.id_protocolo AND pr.realizada = 1) AS resultado,
                   (SELECT m.valor FROM MEDICION m
                    INNER JOIN PRUEBA pr ON m.id_prueba = pr.id_prueba
                    INNER JOIN PARAMETRO pa ON m.id_parametro = pa.id_parametro
                    WHERE pr.id_protocolo = p.id_protocolo AND pa.codigo = 'LECTURA_1MIN' LIMIT 1) AS lectura_1,
                   (SELECT m.valor FROM MEDICION m
                    INNER JOIN PRUEBA pr ON m.id_prueba = pr.id_prueba
                    INNER JOIN PARAMETRO pa ON m.id_parametro = pa.id_parametro
                    WHERE pr.id_protocolo = p.id_protocolo AND pa.codigo = 'LECTURA_10MIN' LIMIT 1) AS lectura_10
            FROM PROTOCOLO p
            WHERE p.id_equipo = ?
            ORDER BY p.fecha_hora_prueba DESC
            """,
            arrayOf(idEquipo.toString())
        )
        val protocolos = mutableListOf<ProtocoloResumen>()
        while (cursor.moveToNext()) {
            protocolos.add(
                ProtocoloResumen(
                    id = cursor.getString(0),
                    numero = cursor.getString(1),
                    fecha = cursor.getString(2),
                    estado = EstadoProtocolo.desdeId(cursor.getLong(3)),
                    observaciones = cursor.getString(4),
                    resultado = ResultadoPrueba.desdeId(cursor.longONulo(5)),
                    lectura1Min = cursor.doubleONulo(6),
                    lectura10Min = cursor.doubleONulo(7)
                )
            )
        }
        cursor.close()
        protocolos
    }

    suspend fun guardarObservacion(idProtocolo: String, observacion: String): Boolean = withContext(Dispatchers.IO) {
        val valores = ContentValues().apply { put("observaciones", observacion) }
        dbHelper.writableDatabase.update(
            "PROTOCOLO", valores, "id_protocolo = ?", arrayOf(idProtocolo)
        ) > 0
    }

    private fun Cursor.doubleONulo(indice: Int): Double? = if (isNull(indice)) null else getDouble(indice)

    private fun Cursor.longONulo(indice: Int): Long? = if (isNull(indice)) null else getLong(indice)
}
