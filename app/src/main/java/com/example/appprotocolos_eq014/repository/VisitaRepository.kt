package com.example.appprotocolos_eq014.repository

import android.content.ContentValues
import com.example.appprotocolos_eq014.data.InduztekDbHelper
import com.example.appprotocolos_eq014.model.ActaVisita
import com.example.appprotocolos_eq014.model.EstadoProtocolo
import com.example.appprotocolos_eq014.model.MedicionActa
import com.example.appprotocolos_eq014.model.ProtocoloActa
import com.example.appprotocolos_eq014.model.PruebaActa
import com.example.appprotocolos_eq014.model.RecepcionActa
import com.example.appprotocolos_eq014.model.ResultadoPrueba
import com.example.appprotocolos_eq014.model.VisitaResumen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VisitaRepository(private val dbHelper: InduztekDbHelper) {

    suspend fun visitasDeUsuario(idUsuario: Long): List<VisitaResumen> = withContext(Dispatchers.IO) {
        val cursor = dbHelper.readableDatabase.rawQuery(
            """
            SELECT v.id_visita, v.numero_ot, c.nombre, i.nombre, v.fecha, v.fecha_hora_cierre,
                   (SELECT COUNT(*) FROM PROTOCOLO p WHERE p.id_visita = v.id_visita AND p.id_estado <> 5),
                   (SELECT COUNT(*) FROM PROTOCOLO p WHERE p.id_visita = v.id_visita AND p.id_estado IN (2, 3, 4))
            FROM VISITA v
            INNER JOIN INSTALACION i ON v.id_instalacion = i.id_instalacion
            INNER JOIN USUARIO c ON i.id_usuario_cliente = c.id_usuario
            WHERE v.id_ingeniero_cargo = ?
               OR v.id_visita IN (SELECT vp.id_visita FROM VISITA_PARTICIPANTE vp WHERE vp.id_usuario = ?)
            ORDER BY v.fecha_hora_cierre IS NOT NULL, v.fecha DESC
            """,
            arrayOf(idUsuario.toString(), idUsuario.toString())
        )
        val visitas = mutableListOf<VisitaResumen>()
        while (cursor.moveToNext()) {
            visitas.add(
                VisitaResumen(
                    id = cursor.getLong(0),
                    numeroOt = cursor.getString(1),
                    cliente = cursor.getString(2),
                    instalacion = cursor.getString(3),
                    fecha = cursor.getString(4),
                    cerrada = !cursor.isNull(5),
                    totalProtocolos = cursor.getInt(6),
                    protocolosRegistrados = cursor.getInt(7)
                )
            )
        }
        cursor.close()
        visitas
    }

    suspend fun obtenerActa(idVisita: Long): ActaVisita? = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursorVisita = db.rawQuery(
            """
            SELECT v.numero_ot, c.nombre, i.nombre, v.fecha, v.tipo_visita, ing.nombre
            FROM VISITA v
            INNER JOIN INSTALACION i ON v.id_instalacion = i.id_instalacion
            INNER JOIN USUARIO c ON i.id_usuario_cliente = c.id_usuario
            INNER JOIN USUARIO ing ON v.id_ingeniero_cargo = ing.id_usuario
            WHERE v.id_visita = ?
            """,
            arrayOf(idVisita.toString())
        )
        if (!cursorVisita.moveToFirst()) {
            cursorVisita.close()
            return@withContext null
        }
        val numeroOt = cursorVisita.getString(0)
        val cliente = cursorVisita.getString(1)
        val instalacion = cursorVisita.getString(2)
        val fecha = cursorVisita.getString(3)
        val tipoVisita = if (cursorVisita.getString(4) == "PRUEBAS_Y_MANT") "Pruebas y mantenimiento" else "Solo pruebas"
        val ingeniero = cursorVisita.getString(5)
        cursorVisita.close()

        ActaVisita(
            idVisita = idVisita,
            numeroOt = numeroOt,
            cliente = cliente,
            instalacion = instalacion,
            fecha = fecha,
            tipoVisita = tipoVisita,
            tecnicos = tecnicosDeVisita(idVisita),
            ingenieroCargo = ingeniero,
            protocolos = protocolosDeVisita(idVisita),
            recepcion = recepcionDeVisita(idVisita)
        )
    }

    suspend fun registrarRecepcion(
        idVisita: Long,
        nombre: String,
        rut: String,
        cargo: String,
        fechaHora: String
    ): Boolean = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val recepcion = ContentValues().apply {
                put("id_visita", idVisita)
                put("receptor_nombre", nombre)
                put("receptor_rut", rut)
                put("receptor_cargo", cargo)
                put("fecha_hora_recepcion", fechaHora)
            }
            db.insertOrThrow("RECEPCION_ACTA", null, recepcion)
            val cierre = ContentValues().apply { put("fecha_hora_cierre", fechaHora) }
            db.update("VISITA", cierre, "id_visita = ?", arrayOf(idVisita.toString()))
            db.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            false
        } finally {
            db.endTransaction()
        }
    }

    private fun tecnicosDeVisita(idVisita: Long): String {
        val cursor = dbHelper.readableDatabase.rawQuery(
            """
            SELECT u.nombre FROM VISITA_PARTICIPANTE vp
            INNER JOIN USUARIO u ON vp.id_usuario = u.id_usuario
            WHERE vp.id_visita = ?
            """,
            arrayOf(idVisita.toString())
        )
        val nombres = mutableListOf<String>()
        while (cursor.moveToNext()) nombres.add(cursor.getString(0))
        cursor.close()
        return nombres.joinToString(", ").ifEmpty { "—" }
    }

    private fun recepcionDeVisita(idVisita: Long): RecepcionActa? {
        val cursor = dbHelper.readableDatabase.rawQuery(
            "SELECT receptor_nombre, receptor_rut, receptor_cargo, fecha_hora_recepcion FROM RECEPCION_ACTA WHERE id_visita = ?",
            arrayOf(idVisita.toString())
        )
        val recepcion = if (cursor.moveToFirst()) {
            RecepcionActa(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3))
        } else {
            null
        }
        cursor.close()
        return recepcion
    }

    private fun protocolosDeVisita(idVisita: Long): List<ProtocoloActa> {
        val cursor = dbHelper.readableDatabase.rawQuery(
            """
            SELECT p.id_protocolo, p.numero, e.id_equipo, e.tag, t.nombre, e.marca, e.numero_serie, p.id_estado
            FROM PROTOCOLO p
            INNER JOIN EQUIPO e ON p.id_equipo = e.id_equipo
            INNER JOIN TIPO_EQUIPO t ON e.id_tipo_equipo = t.id_tipo_equipo
            WHERE p.id_visita = ?
            ORDER BY e.tag
            """,
            arrayOf(idVisita.toString())
        )
        val protocolos = mutableListOf<ProtocoloActa>()
        while (cursor.moveToNext()) {
            val descripcion = listOfNotNull(
                listOfNotNull(cursor.getString(4), cursor.getString(5)).joinToString(" "),
                cursor.getString(6)?.let { "serie $it" }
            ).joinToString(" · ")
            protocolos.add(
                ProtocoloActa(
                    numero = cursor.getString(1),
                    idEquipo = cursor.getLong(2),
                    tag = cursor.getString(3),
                    descripcionEquipo = descripcion,
                    estado = EstadoProtocolo.desdeId(cursor.getLong(7)),
                    pruebas = pruebasDeProtocolo(cursor.getString(0))
                )
            )
        }
        cursor.close()
        return protocolos
    }

    private fun pruebasDeProtocolo(idProtocolo: String): List<PruebaActa> {
        val cursor = dbHelper.readableDatabase.rawQuery(
            """
            SELECT pr.id_prueba, tp.nombre, pr.elemento, pr.realizada, pr.motivo_no_realizada, pr.id_resultado
            FROM PRUEBA pr
            INNER JOIN TIPO_PRUEBA tp ON pr.id_tipo_prueba = tp.id_tipo_prueba
            WHERE pr.id_protocolo = ?
            ORDER BY pr.id_prueba
            """,
            arrayOf(idProtocolo)
        )
        val pruebas = mutableListOf<PruebaActa>()
        while (cursor.moveToNext()) {
            pruebas.add(
                PruebaActa(
                    nombre = cursor.getString(1),
                    elemento = cursor.getString(2),
                    realizada = cursor.getInt(3) == 1,
                    motivoNoRealizada = cursor.getString(4),
                    resultado = if (cursor.isNull(5)) null else ResultadoPrueba.desdeId(cursor.getLong(5)),
                    mediciones = medicionesDePrueba(cursor.getLong(0))
                )
            )
        }
        cursor.close()
        return pruebas
    }

    private fun medicionesDePrueba(idPrueba: Long): List<MedicionActa> {
        val cursor = dbHelper.readableDatabase.rawQuery(
            """
            SELECT pa.nombre, m.valor, m.unidad
            FROM MEDICION m
            INNER JOIN PARAMETRO pa ON m.id_parametro = pa.id_parametro
            WHERE m.id_prueba = ?
            ORDER BY m.orden
            """,
            arrayOf(idPrueba.toString())
        )
        val mediciones = mutableListOf<MedicionActa>()
        while (cursor.moveToNext()) {
            mediciones.add(MedicionActa(cursor.getString(0), cursor.getDouble(1), cursor.getString(2)))
        }
        cursor.close()
        return mediciones
    }
}
