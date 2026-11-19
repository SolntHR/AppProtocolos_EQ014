package com.example.appprotocolos_eq014.repository

import android.content.ContentValues
import com.example.appprotocolos_eq014.data.InduztekDbHelper
import com.example.appprotocolos_eq014.model.Instrumento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InstrumentoRepository(private val dbHelper: InduztekDbHelper) {

    suspend fun listarConCalibracion(): List<Instrumento> = withContext(Dispatchers.IO) {
        val cursor = dbHelper.readableDatabase.rawQuery(
            """
            SELECT i.id_instrumento, i.nombre_tipo, i.marca, i.modelo, i.numero_serie,
                   c.fecha_calibracion, c.fecha_vencimiento
            FROM INSTRUMENTO i
            LEFT JOIN CALIBRACION c ON c.id_calibracion = (
                SELECT c2.id_calibracion FROM CALIBRACION c2
                WHERE c2.id_instrumento = i.id_instrumento
                ORDER BY c2.fecha_vencimiento DESC LIMIT 1
            )
            ORDER BY c.fecha_vencimiento
            """,
            null
        )
        val instrumentos = mutableListOf<Instrumento>()
        while (cursor.moveToNext()) {
            instrumentos.add(
                Instrumento(
                    id = cursor.getLong(0),
                    nombreTipo = cursor.getString(1),
                    marca = cursor.getString(2),
                    modelo = cursor.getString(3),
                    numeroSerie = cursor.getString(4),
                    fechaCalibracion = cursor.getString(5),
                    fechaVencimiento = cursor.getString(6)
                )
            )
        }
        cursor.close()
        instrumentos
    }

    suspend fun registrarCalibracion(
        idInstrumento: Long,
        fechaCalibracion: String,
        fechaVencimiento: String
    ): Boolean = withContext(Dispatchers.IO) {
        val valores = ContentValues().apply {
            put("id_instrumento", idInstrumento)
            put("fecha_calibracion", fechaCalibracion)
            put("fecha_vencimiento", fechaVencimiento)
        }
        dbHelper.writableDatabase.insert("CALIBRACION", null, valores) != -1L
    }

}
