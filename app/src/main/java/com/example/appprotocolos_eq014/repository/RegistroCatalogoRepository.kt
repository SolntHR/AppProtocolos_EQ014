package com.example.appprotocolos_eq014.repository

import android.content.ContentValues
import com.example.appprotocolos_eq014.data.InduztekDbHelper
import com.example.appprotocolos_eq014.model.Equipo
import com.example.appprotocolos_eq014.model.Instalacion
import com.example.appprotocolos_eq014.model.OpcionCatalogo
import com.example.appprotocolos_eq014.model.Visita
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegistroCatalogoRepository(private val helper: InduztekDbHelper) {
    private fun opciones(sql: String, args: Array<String>? = null): List<OpcionCatalogo> =
        helper.readableDatabase.rawQuery(sql, args).use { c ->
            buildList { while (c.moveToNext()) add(OpcionCatalogo(c.getLong(0), c.getString(1))) }
        }

    suspend fun instalaciones(): List<OpcionCatalogo> = withContext(Dispatchers.IO) {
        opciones(
            """
            SELECT i.id_instalacion, i.nombre || ' · ' || c.nombre
            FROM INSTALACION i INNER JOIN USUARIO c ON c.id_usuario = i.id_usuario_cliente
            ORDER BY c.nombre, i.nombre
            """
        )
    }

    suspend fun tiposEquipo(): List<OpcionCatalogo> = withContext(Dispatchers.IO) {
        opciones("SELECT id_tipo_equipo, nombre FROM TIPO_EQUIPO ORDER BY id_tipo_equipo")
    }

    suspend fun clientes(): List<OpcionCatalogo> = withContext(Dispatchers.IO) {
        opciones(
            """
            SELECT u.id_usuario, u.nombre FROM USUARIO u INNER JOIN ROL r ON r.id_rol = u.id_rol
            WHERE r.codigo = 'CLIENTE' AND u.activo = 1
            ORDER BY u.nombre
            """
        )
    }

    suspend fun comunas(): List<OpcionCatalogo> = withContext(Dispatchers.IO) {
        opciones(
            """
            SELECT c.id_comuna, c.nombre || ' · ' || r.nombre
            FROM COMUNA c INNER JOIN REGION r ON r.id_region = c.id_region
            ORDER BY c.nombre
            """
        )
    }

    suspend fun buscarEquipo(idInstalacion: Long, tag: String): Equipo? = withContext(Dispatchers.IO) {
        helper.readableDatabase.rawQuery(
            """
            SELECT e.id_equipo, e.tag, t.nombre, e.marca, e.numero_serie
            FROM EQUIPO e INNER JOIN TIPO_EQUIPO t ON t.id_tipo_equipo = e.id_tipo_equipo
            WHERE e.id_instalacion = ? AND UPPER(e.tag) = UPPER(?)
            """,
            arrayOf(idInstalacion.toString(), tag)
        ).use { c ->
            if (!c.moveToFirst()) null
            else Equipo(
                id = c.getLong(0),
                instalacionId = idInstalacion,
                tag = c.getString(1),
                tipoEquipoNombre = c.getString(2),
                marca = c.getString(3),
                numeroSerie = c.getString(4),
            )
        }
    }

    suspend fun crearEquipo(e: Equipo): Long = withContext(Dispatchers.IO) {
        val valores = ContentValues().apply {
            put("id_instalacion", e.instalacionId)
            put("id_tipo_equipo", e.tipoEquipoId)
            put("tag", e.tag)
            put("marca", e.marca)
            put("modelo", e.modelo)
            put("numero_serie", e.numeroSerie)
            if (e.anio != null) put("anio", e.anio) else putNull("anio")
            if (e.potenciaKva != null) put("potencia_kva", e.potenciaKva) else putNull("potencia_kva")
            put("tensiones", e.tensiones)
            if (e.corriente != null) put("corriente", e.corriente) else putNull("corriente")
            if (e.maxContactosUohm != null) put("max_contactos_uohm", e.maxContactosUohm) else putNull("max_contactos_uohm")
            put("observaciones", e.observaciones)
        }
        helper.writableDatabase.insertOrThrow("EQUIPO", null, valores)
    }

    suspend fun existeInstalacion(idCliente: Long, nombre: String): Boolean = withContext(Dispatchers.IO) {
        helper.readableDatabase.rawQuery(
            "SELECT 1 FROM INSTALACION WHERE id_usuario_cliente = ? AND UPPER(TRIM(nombre)) = UPPER(TRIM(?)) LIMIT 1",
            arrayOf(idCliente.toString(), nombre)
        ).use { it.moveToFirst() }
    }

    suspend fun crearInstalacion(instalacion: Instalacion): Long = withContext(Dispatchers.IO) {
        val valores = ContentValues().apply {
            put("id_usuario_cliente", instalacion.clienteId)
            put("nombre", instalacion.nombre)
            put("ubicacion", instalacion.ubicacion)
            put("id_comuna", instalacion.comunaId)
            put("observaciones_acceso", instalacion.observacionesAcceso)
        }
        helper.writableDatabase.insertOrThrow("INSTALACION", null, valores)
    }

    suspend fun visitasCerradasDeCliente(idCliente: Long): List<Visita> = withContext(Dispatchers.IO) {
        helper.readableDatabase.rawQuery(
            """
            SELECT v.id_visita, v.numero_ot, c.nombre, i.nombre, v.fecha, v.fecha_hora_cierre,
                   (SELECT COUNT(*) FROM PROTOCOLO p WHERE p.id_visita = v.id_visita AND p.id_estado NOT IN (1, 5)),
                   (SELECT COUNT(*) FROM PROTOCOLO p WHERE p.id_visita = v.id_visita AND p.id_estado = 4),
                   v.id_instalacion, v.tipo_visita
            FROM VISITA v
            INNER JOIN INSTALACION i ON i.id_instalacion = v.id_instalacion
            INNER JOIN USUARIO c ON c.id_usuario = i.id_usuario_cliente
            WHERE i.id_usuario_cliente = ? AND v.fecha_hora_cierre IS NOT NULL
            ORDER BY v.fecha DESC
            """,
            arrayOf(idCliente.toString())
        ).use { c ->
            buildList {
                while (c.moveToNext()) {
                    add(
                        Visita(
                            id = c.getLong(0),
                            numeroOt = c.getString(1),
                            clienteNombre = c.getString(2),
                            instalacionNombre = c.getString(3),
                            fecha = c.getString(4),
                            fechaHoraCierre = c.getString(5),
                            totalProtocolos = c.getInt(6),
                            protocolosFormalizados = c.getInt(7),
                            instalacionId = c.getLong(8),
                            tipoVisita = c.getString(9)
                        )
                    )
                }
            }
        }
    }

    suspend fun visitaEsDeCliente(idVisita: Long, idCliente: Long): Boolean = withContext(Dispatchers.IO) {
        helper.readableDatabase.rawQuery(
            """
            SELECT 1 FROM VISITA v INNER JOIN INSTALACION i ON i.id_instalacion = v.id_instalacion
            WHERE v.id_visita = ? AND i.id_usuario_cliente = ? AND v.fecha_hora_cierre IS NOT NULL
            """,
            arrayOf(idVisita.toString(), idCliente.toString())
        ).use { it.moveToFirst() }
    }

    suspend fun equipoEsDeCliente(idEquipo: Long, idCliente: Long): Boolean = withContext(Dispatchers.IO) {
        helper.readableDatabase.rawQuery(
            """
            SELECT 1 FROM EQUIPO e INNER JOIN INSTALACION i ON i.id_instalacion = e.id_instalacion
            WHERE e.id_equipo = ? AND i.id_usuario_cliente = ?
            """,
            arrayOf(idEquipo.toString(), idCliente.toString())
        ).use { it.moveToFirst() }
    }
}
