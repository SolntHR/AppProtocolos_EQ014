package com.example.appprotocolos_eq014.repository

import android.content.ContentValues
import com.example.appprotocolos_eq014.data.InduztekDbHelper
import com.example.appprotocolos_eq014.model.EstadoCalibracion
import com.example.appprotocolos_eq014.model.InstrumentoCalibracion
import com.example.appprotocolos_eq014.util.Formato
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InstrumentoRepository(private val dbHelper: InduztekDbHelper) {

    suspend fun listarConCalibracion(): List<InstrumentoCalibracion> = withContext(Dispatchers.IO) {
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
        val instrumentos = mutableListOf<InstrumentoCalibracion>()
        while (cursor.moveToNext()) {
            val vencimiento = cursor.getString(6)
            val dias = vencimiento?.let { Formato.diasHasta(it) }
            instrumentos.add(
                InstrumentoCalibracion(
                    id = cursor.getLong(0),
                    nombre = cursor.getString(1),
                    marcaModelo = listOfNotNull(cursor.getString(2), cursor.getString(3)).joinToString(" "),
                    numeroSerie = cursor.getString(4).orEmpty(),
                    fechaCalibracion = cursor.getString(5),
                    fechaVencimiento = vencimiento,
                    diasRestantes = dias,
                    estado = when {
                        dias == null -> EstadoCalibracion.SIN_CALIBRACION
                        dias < 0 -> EstadoCalibracion.VENCIDA
                        dias <= DIAS_AVISO -> EstadoCalibracion.POR_VENCER
                        else -> EstadoCalibracion.VIGENTE
                    }
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

    companion object {
        const val DIAS_AVISO = 30L
    }
}
