package com.example.appprotocolos_eq014.repository

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.appprotocolos_eq014.data.InduztekDbHelper
import com.example.appprotocolos_eq014.model.CambioEstado
import com.example.appprotocolos_eq014.model.EquipoProtocolo
import com.example.appprotocolos_eq014.model.EstadoProtocolo
import com.example.appprotocolos_eq014.model.Fechas
import com.example.appprotocolos_eq014.model.InstrumentoOpcion
import com.example.appprotocolos_eq014.model.MedicionAnterior
import com.example.appprotocolos_eq014.model.MedicionDetalle
import com.example.appprotocolos_eq014.model.Parametros
import com.example.appprotocolos_eq014.model.ProtocoloDetalle
import com.example.appprotocolos_eq014.model.ProtocoloListado
import com.example.appprotocolos_eq014.model.PruebaDetalle
import com.example.appprotocolos_eq014.model.ReglasProtocolo
import com.example.appprotocolos_eq014.model.ResultadoPrueba
import com.example.appprotocolos_eq014.model.Rol
import com.example.appprotocolos_eq014.model.TipoPrueba
import com.example.appprotocolos_eq014.model.VisitaAbierta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale
import java.util.UUID

data class PruebaAGuardar(
    val tipo: TipoPrueba,
    val elemento: String,
    val instrumentoId: Long?,
    val resultado: ResultadoPrueba?,
    val comentario: String?,
    val ingresadoManualmente: Boolean,
    val valores: Map<String, Double>,
    val realizada: Boolean = true,
    val motivoNoRealizada: String? = null
)

data class ProtocoloAGuardar(
    val idProtocolo: String?,
    val visitaId: Long,
    val equipoId: Long,
    val temperatura: Double?,
    val humedad: Double?,
    val descripcionMantenimiento: String?,
    val observaciones: String?,
    val pruebas: List<PruebaAGuardar>
)

data class ProtocoloEditable(
    val idProtocolo: String,
    val numero: String,
    val estado: EstadoProtocolo,
    val visitaId: Long,
    val equipoId: Long,
    val creadorId: Long,
    val fechaHoraPrueba: String,
    val temperatura: Double?,
    val humedad: Double?,
    val descripcionMantenimiento: String?,
    val observaciones: String?,
    val pruebas: List<PruebaAGuardar>
)

class RegistroProtocoloRepository(private val helper: InduztekDbHelper) {
    suspend fun numeroDe(idProtocolo: String): String = withContext(Dispatchers.IO) {
        helper.readableDatabase.rawQuery("SELECT numero FROM PROTOCOLO WHERE id_protocolo = ?", arrayOf(idProtocolo))
            .use { c -> if (c.moveToFirst()) c.getString(0) else idProtocolo }
    }

    suspend fun nombreUsuario(idUsuario: Long): String = withContext(Dispatchers.IO) {
        helper.readableDatabase.rawQuery("SELECT nombre FROM USUARIO WHERE id_usuario = ?", arrayOf(idUsuario.toString()))
            .use { c -> if (c.moveToFirst()) c.getString(0) else "" }
    }

    private val selectVisita = """
        SELECT v.id_visita, v.numero_ot, v.fecha, v.tipo_visita, v.id_instalacion,
               i.nombre AS instalacion, c.nombre AS cliente, ing.nombre AS ingeniero
        FROM VISITA v
        INNER JOIN INSTALACION i ON i.id_instalacion = v.id_instalacion
        INNER JOIN USUARIO c ON c.id_usuario = i.id_usuario_cliente
        INNER JOIN USUARIO ing ON ing.id_usuario = v.id_ingeniero_cargo
    """

    suspend fun visitasAbiertas(usuarioId: Long, rol: String): List<VisitaAbierta> = withContext(Dispatchers.IO) {
        val todas = if (rol == Rol.JEFE_TECNICO) "1" else "0"
        val sql = """
            $selectVisita
            WHERE v.fecha_hora_cierre IS NULL
              AND (? = '1'
                   OR v.id_ingeniero_cargo = ?
                   OR EXISTS (SELECT 1 FROM VISITA_PARTICIPANTE vp
                              WHERE vp.id_visita = v.id_visita AND vp.id_usuario = ?))
            ORDER BY v.fecha DESC, v.numero_ot
        """
        helper.readableDatabase.rawQuery(sql, arrayOf(todas, usuarioId.toString(), usuarioId.toString()))
            .use { c -> buildList { while (c.moveToNext()) add(leerVisita(c)) } }
    }

    suspend fun visita(idVisita: Long): VisitaAbierta? = withContext(Dispatchers.IO) {
        helper.readableDatabase.rawQuery("$selectVisita WHERE v.id_visita = ?", arrayOf(idVisita.toString()))
            .use { c -> if (c.moveToFirst()) leerVisita(c) else null }
    }

    private fun leerVisita(c: Cursor) = VisitaAbierta(
        id = c.getLong(0),
        numeroOt = c.getString(1),
        fecha = c.getString(2),
        tipoVisita = c.getString(3),
        instalacionId = c.getLong(4),
        instalacionNombre = c.getString(5),
        clienteNombre = c.getString(6),
        ingenieroNombre = c.getString(7)
    )

    suspend fun equiposDisponibles(
        idVisita: Long,
        idInstalacion: Long,
        idProtocoloEditando: String?
    ): List<EquipoProtocolo> = withContext(Dispatchers.IO) {
        val sql = """
            SELECT e.id_equipo, e.tag, e.id_tipo_equipo, t.nombre, e.marca, e.numero_serie,
                   e.potencia_kva, e.tensiones, e.corriente, e.max_contactos_uohm
            FROM EQUIPO e
            INNER JOIN TIPO_EQUIPO t ON t.id_tipo_equipo = e.id_tipo_equipo
            WHERE e.id_instalacion = ?
              AND NOT EXISTS (
                SELECT 1 FROM PROTOCOLO p
                WHERE p.id_visita = ? AND p.id_equipo = e.id_equipo AND p.id_protocolo <> ?
              )
            ORDER BY e.tag
        """
        helper.readableDatabase.rawQuery(
            sql, arrayOf(idInstalacion.toString(), idVisita.toString(), idProtocoloEditando ?: "")
        ).use { c ->
            buildList {
                while (c.moveToNext()) {
                    add(
                        EquipoProtocolo(
                            id = c.getLong(0),
                            tag = c.getString(1),
                            tipoEquipoId = c.getLong(2),
                            tipoEquipoNombre = c.getString(3),
                            marca = c.getString(4),
                            numeroSerie = c.getString(5),
                            potenciaKva = c.realONulo(6),
                            tensiones = c.getString(7),
                            corriente = c.realONulo(8),
                            maxContactosUohm = c.realONulo(9)
                        )
                    )
                }
            }
        }
    }

    suspend fun instrumentos(): List<InstrumentoOpcion> = withContext(Dispatchers.IO) {
        val sql = """
            SELECT ins.id_instrumento, ins.nombre_tipo, ins.marca, ins.modelo, ins.numero_serie,
                   (SELECT MAX(c.fecha_vencimiento) FROM CALIBRACION c WHERE c.id_instrumento = ins.id_instrumento)
            FROM INSTRUMENTO ins
            ORDER BY ins.nombre_tipo
        """
        helper.readableDatabase.rawQuery(sql, null).use { c ->
            buildList {
                while (c.moveToNext()) {
                    add(InstrumentoOpcion(c.getLong(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5)))
                }
            }
        }
    }

    private val selectListado = """
        SELECT p.id_protocolo, p.numero, p.fecha_hora_prueba, p.id_estado, p.id_usuario_registra,
               e.id_equipo, e.tag, te.nombre AS tipo_equipo, i.nombre AS instalacion,
               c.nombre AS cliente, u.nombre AS tecnico,
               (SELECT MAX(pr.id_resultado) FROM PRUEBA pr WHERE pr.id_protocolo = p.id_protocolo AND pr.realizada = 1) AS resultado,
               (SELECT r.numero FROM PROTOCOLO r WHERE r.id_protocolo_reemplazado = p.id_protocolo LIMIT 1) AS reemplazado_por,
               (SELECT o.numero FROM PROTOCOLO o WHERE o.id_protocolo = p.id_protocolo_reemplazado) AS reemplaza_a
        FROM PROTOCOLO p
        INNER JOIN EQUIPO e ON e.id_equipo = p.id_equipo
        INNER JOIN TIPO_EQUIPO te ON te.id_tipo_equipo = e.id_tipo_equipo
        INNER JOIN INSTALACION i ON i.id_instalacion = e.id_instalacion
        INNER JOIN USUARIO c ON c.id_usuario = i.id_usuario_cliente
        INNER JOIN USUARIO u ON u.id_usuario = p.id_usuario_registra
    """

    private fun leerListado(c: Cursor): ProtocoloListado {
        val idxRes = c.getColumnIndexOrThrow("resultado")
        return ProtocoloListado(
            id = c.getString(c.getColumnIndexOrThrow("id_protocolo")),
            numero = c.getString(c.getColumnIndexOrThrow("numero")),
            equipoId = c.getLong(c.getColumnIndexOrThrow("id_equipo")),
            tagEquipo = c.getString(c.getColumnIndexOrThrow("tag")),
            tipoEquipo = c.getString(c.getColumnIndexOrThrow("tipo_equipo")),
            instalacion = c.getString(c.getColumnIndexOrThrow("instalacion")),
            cliente = c.getString(c.getColumnIndexOrThrow("cliente")),
            tecnicoId = c.getLong(c.getColumnIndexOrThrow("id_usuario_registra")),
            tecnico = c.getString(c.getColumnIndexOrThrow("tecnico")),
            fechaHora = c.getString(c.getColumnIndexOrThrow("fecha_hora_prueba")),
            estado = EstadoProtocolo.desdeId(c.getLong(c.getColumnIndexOrThrow("id_estado"))),
            resultadoGlobal = if (c.isNull(idxRes)) null else ResultadoPrueba.desdeId(c.getLong(idxRes)),
            reemplazadoPorNumero = c.getString(c.getColumnIndexOrThrow("reemplazado_por")),
            reemplazaANumero = c.getString(c.getColumnIndexOrThrow("reemplaza_a"))
        )
    }

    private fun consultarListado(db: SQLiteDatabase, where: String, args: Array<String>): List<ProtocoloListado> =
        db.rawQuery("$selectListado WHERE $where ORDER BY p.fecha_hora_prueba DESC", args).use { c ->
            buildList { while (c.moveToNext()) add(leerListado(c)) }
        }

    suspend fun todos(): List<ProtocoloListado> = withContext(Dispatchers.IO) {
        consultarListado(helper.readableDatabase, "1 = 1", emptyArray())
    }

    suspend fun promedioDiasFormalizacion(): Double? = withContext(Dispatchers.IO) {
        helper.readableDatabase.rawQuery(
            """
            SELECT AVG(julianday(h.fecha_hora) - julianday(p.fecha_hora_prueba))
            FROM HISTORIAL_ESTADO h INNER JOIN PROTOCOLO p ON p.id_protocolo = h.id_protocolo
            WHERE h.id_estado_nuevo = 4
            """.trimIndent(),
            null
        ).use { c -> if (c.moveToFirst() && !c.isNull(0)) c.getDouble(0) else null }
    }

    suspend fun medicionesAnteriores(
        idEquipo: Long,
        idProtocoloExcluir: String?,
        antesDe: String
    ): Map<String, MedicionAnterior> = withContext(Dispatchers.IO) {
        anteriores(helper.readableDatabase, idEquipo, idProtocoloExcluir, antesDe)
    }

    private fun anteriores(db: SQLiteDatabase, idEquipo: Long, idExcluir: String?, antesDe: String): Map<String, MedicionAnterior> {
        val sql = """
            SELECT pr.id_tipo_prueba, pr.elemento, p.numero, m.id_parametro, m.valor
            FROM MEDICION m
            INNER JOIN PRUEBA pr ON pr.id_prueba = m.id_prueba
            INNER JOIN PROTOCOLO p ON p.id_protocolo = pr.id_protocolo
            WHERE p.id_equipo = ?
              AND p.id_protocolo <> ?
              AND p.id_estado NOT IN (1, 5)
              AND p.fecha_hora_prueba < ?
              AND pr.realizada = 1
              AND NOT EXISTS (SELECT 1 FROM PROTOCOLO r WHERE r.id_protocolo_reemplazado = p.id_protocolo)
            ORDER BY p.fecha_hora_prueba DESC
        """
        val numeroPorClave = mutableMapOf<String, String>()
        val valoresPorClave = mutableMapOf<String, MutableMap<String, Double>>()
        db.rawQuery(sql, arrayOf(idEquipo.toString(), idExcluir ?: "", antesDe)).use { c ->
            while (c.moveToNext()) {
                val clave = "${c.getLong(0)}|${c.getString(1)}"
                val numero = c.getString(2)
                val primero = numeroPorClave.getOrPut(clave) { numero }
                if (primero != numero) continue
                val campo = Parametros.porId(c.getLong(3)) ?: continue
                valoresPorClave.getOrPut(clave) { mutableMapOf() }[campo.codigo] = c.getDouble(4)
            }
        }
        return valoresPorClave.mapValues { (clave, valores) -> MedicionAnterior(numeroPorClave.getValue(clave), valores) }
    }

    private data class Cabecera(
        val numeroOt: String,
        val temperatura: Double?,
        val humedad: Double?,
        val descripcion: String?,
        val observaciones: String?,
        val maxContactos: Double?
    )

    private data class PruebaLeida(
        val datos: PruebaAGuardar,
        val instrumento: String?,
        val realizada: Boolean,
        val motivoNoRealizada: String?
    )

    suspend fun detalle(idProtocolo: String): ProtocoloDetalle? = withContext(Dispatchers.IO) {
        val db = helper.readableDatabase
        val resumen = consultarListado(db, "p.id_protocolo = ?", arrayOf(idProtocolo)).firstOrNull()
            ?: return@withContext null

        val cabecera = db.rawQuery(
            """
            SELECT v.numero_ot, p.temperatura_c, p.humedad_pct, p.descripcion_mantenimiento, p.observaciones,
                   e.max_contactos_uohm
            FROM PROTOCOLO p
            INNER JOIN VISITA v ON v.id_visita = p.id_visita
            INNER JOIN EQUIPO e ON e.id_equipo = p.id_equipo
            WHERE p.id_protocolo = ?
            """.trimIndent(),
            arrayOf(idProtocolo)
        ).use { c ->
            c.moveToFirst()
            Cabecera(c.getString(0), c.realONulo(1), c.realONulo(2), c.getString(3), c.getString(4), c.realONulo(5))
        }

        val previos = anteriores(db, resumen.equipoId, idProtocolo, resumen.fechaHora)
        val pruebas = leerPruebas(db, idProtocolo, soloRealizadas = false).map { leida ->
            val p = leida.datos
            val anterior = previos["${p.tipo.idDb}|${p.elemento}"]
            PruebaDetalle(
                tipo = p.tipo,
                elemento = p.elemento,
                instrumento = leida.instrumento,
                resultado = p.resultado,
                comentario = p.comentario,
                ingresadoManualmente = p.ingresadoManualmente,
                realizada = leida.realizada,
                motivoNoRealizada = leida.motivoNoRealizada,
                mediciones = p.valores.mapNotNull { (codigo, valor) ->
                    Parametros.porCodigo(codigo)?.let { MedicionDetalle(codigo, it.etiqueta, valor, it.unidad) }
                },
                calculo = ReglasProtocolo.resumenCalculo(p.tipo, p.valores, cabecera.maxContactos),
                comparaciones = if (anterior == null || !leida.realizada) emptyList()
                else ReglasProtocolo.comparar(p.tipo, p.valores, anterior)
            )
        }

        val historial = db.rawQuery(
            """
            SELECT h.id_estado_anterior, h.id_estado_nuevo, u.nombre, h.fecha_hora, h.motivo
            FROM HISTORIAL_ESTADO h INNER JOIN USUARIO u ON u.id_usuario = h.id_usuario
            WHERE h.id_protocolo = ?
            ORDER BY h.fecha_hora, h.id_historial
            """.trimIndent(),
            arrayOf(idProtocolo)
        ).use { c ->
            buildList {
                while (c.moveToNext()) {
                    add(
                        CambioEstado(
                            anterior = if (c.isNull(0)) null else EstadoProtocolo.desdeId(c.getLong(0)),
                            nuevo = EstadoProtocolo.desdeId(c.getLong(1)),
                            usuario = c.getString(2),
                            fechaHora = c.getString(3),
                            motivo = c.getString(4)
                        )
                    )
                }
            }
        }

        ProtocoloDetalle(
            resumen = resumen,
            numeroOt = cabecera.numeroOt,
            temperatura = cabecera.temperatura,
            humedad = cabecera.humedad,
            descripcionMantenimiento = cabecera.descripcion,
            observaciones = cabecera.observaciones,
            pruebas = pruebas,
            historialEstados = historial,
            historialEquipo = consultarListado(db, "p.id_equipo = ?", arrayOf(resumen.equipoId.toString()))
        )
    }

    private fun leerPruebas(db: SQLiteDatabase, idProtocolo: String, soloRealizadas: Boolean): List<PruebaLeida> {
        val valoresPorPrueba = mutableMapOf<Long, MutableMap<String, Double>>()
        db.rawQuery(
            """
            SELECT m.id_prueba, m.id_parametro, m.valor
            FROM MEDICION m INNER JOIN PRUEBA pr ON pr.id_prueba = m.id_prueba
            WHERE pr.id_protocolo = ?
            ORDER BY m.id_prueba, m.orden
            """.trimIndent(),
            arrayOf(idProtocolo)
        ).use { c ->
            while (c.moveToNext()) {
                val campo = Parametros.porId(c.getLong(1)) ?: continue
                valoresPorPrueba.getOrPut(c.getLong(0)) { linkedMapOf() }[campo.codigo] = c.getDouble(2)
            }
        }
        val filtro = if (soloRealizadas) " AND pr.realizada = 1" else ""
        return db.rawQuery(
            """
            SELECT pr.id_prueba, pr.id_tipo_prueba, pr.elemento, pr.id_instrumento, pr.id_resultado,
                   pr.comentario, pr.ingresado_manualmente, pr.realizada, pr.motivo_no_realizada,
                   ins.marca, ins.modelo, ins.numero_serie
            FROM PRUEBA pr LEFT JOIN INSTRUMENTO ins ON ins.id_instrumento = pr.id_instrumento
            WHERE pr.id_protocolo = ?$filtro
            ORDER BY pr.id_tipo_prueba, pr.id_prueba
            """.trimIndent(),
            arrayOf(idProtocolo)
        ).use { c ->
            buildList {
                while (c.moveToNext()) {
                    val tipo = TipoPrueba.desdeId(c.getLong(1)) ?: continue
                    val instrumento = if (c.isNull(3)) null else
                        listOfNotNull(c.getString(9), c.getString(10)).joinToString(" ") +
                                (c.getString(11)?.let { " · Serie $it" } ?: "")
                    add(
                        PruebaLeida(
                            datos = PruebaAGuardar(
                                tipo = tipo,
                                elemento = c.getString(2),
                                instrumentoId = if (c.isNull(3)) null else c.getLong(3),
                                resultado = if (c.isNull(4)) null else ResultadoPrueba.desdeId(c.getLong(4)),
                                comentario = c.getString(5),
                                ingresadoManualmente = c.getInt(6) == 1,
                                valores = valoresPorPrueba[c.getLong(0)].orEmpty()
                            ),
                            instrumento = instrumento,
                            realizada = c.getInt(7) == 1,
                            motivoNoRealizada = c.getString(8)
                        )
                    )
                }
            }
        }
    }

    suspend fun paraEditar(idProtocolo: String): ProtocoloEditable? = withContext(Dispatchers.IO) {
        val db = helper.readableDatabase
        val pruebas = leerPruebas(db, idProtocolo, soloRealizadas = false).map {
            it.datos.copy(realizada = it.realizada, motivoNoRealizada = it.motivoNoRealizada)
        }
        db.rawQuery(
            """
            SELECT id_protocolo, numero, id_estado, id_visita, id_equipo, id_usuario_registra,
                   fecha_hora_prueba, temperatura_c, humedad_pct, descripcion_mantenimiento, observaciones
            FROM PROTOCOLO WHERE id_protocolo = ?
            """.trimIndent(),
            arrayOf(idProtocolo)
        ).use { c ->
            if (!c.moveToFirst()) return@use null
            ProtocoloEditable(
                idProtocolo = c.getString(0),
                numero = c.getString(1),
                estado = EstadoProtocolo.desdeId(c.getLong(2)),
                visitaId = c.getLong(3),
                equipoId = c.getLong(4),
                creadorId = c.getLong(5),
                fechaHoraPrueba = c.getString(6),
                temperatura = c.realONulo(7),
                humedad = c.realONulo(8),
                descripcionMantenimiento = c.getString(9),
                observaciones = c.getString(10),
                pruebas = pruebas
            )
        }
    }

    suspend fun guardar(
        datos: ProtocoloAGuardar,
        usuarioId: Long,
        rol: String,
        registrarEnTerreno: Boolean
    ): String = withContext(Dispatchers.IO) {
        require(usuarioId > 0) { "La sesión no tiene un usuario válido." }
        require(Rol.puedeCrearProtocolos(rol)) { "Tu perfil no puede registrar protocolos." }
        if (registrarEnTerreno) {
            require(datos.temperatura != null && datos.humedad != null) {
                "La temperatura ambiente y la humedad son obligatorias para registrar en terreno."
            }
            require(datos.pruebas.any { it.realizada }) { "Registra al menos una prueba completa." }
        }

        val db = helper.writableDatabase
        val ahora = Fechas.ahora()
        db.beginTransaction()
        try {
            val idProtocolo: String
            val estadoActual: EstadoProtocolo

            if (datos.idProtocolo == null) {
                validarVisitaAsignada(db, datos.visitaId, datos.equipoId, usuarioId, rol)
                val yaExiste = db.rawQuery(
                    "SELECT 1 FROM PROTOCOLO WHERE id_visita = ? AND id_equipo = ? LIMIT 1",
                    arrayOf(datos.visitaId.toString(), datos.equipoId.toString())
                ).use { it.moveToFirst() }
                check(!yaExiste) { "Ya existe un protocolo para ese equipo en esta visita." }

                idProtocolo = UUID.randomUUID().toString()
                val valores = ContentValues().apply {
                    put("id_protocolo", idProtocolo)
                    put("numero", generarNumeroUnico(db, idProtocolo))
                    put("id_visita", datos.visitaId)
                    put("id_equipo", datos.equipoId)
                    put("id_usuario_registra", usuarioId)
                    put("fecha_hora_prueba", ahora)
                    putCabecera(datos)
                    put("id_estado", EstadoProtocolo.BORRADOR.idDb)
                }
                db.insertOrThrow("PROTOCOLO", null, valores)
                insertarHistorial(db, idProtocolo, null, EstadoProtocolo.BORRADOR, usuarioId, ahora, "Creación del protocolo")
                estadoActual = EstadoProtocolo.BORRADOR
            } else {
                idProtocolo = datos.idProtocolo
                val (estado, creador) = db.rawQuery(
                    "SELECT id_estado, id_usuario_registra FROM PROTOCOLO WHERE id_protocolo = ?",
                    arrayOf(idProtocolo)
                ).use { c ->
                    check(c.moveToFirst()) { "El protocolo ya no existe." }
                    EstadoProtocolo.desdeId(c.getLong(0)) to c.getLong(1)
                }
                check(creador == usuarioId) { "Solo quien creó el protocolo puede modificarlo." }
                check(estado == EstadoProtocolo.BORRADOR || estado == EstadoProtocolo.OBSERVADO) {
                    "Solo se pueden modificar protocolos en Borrador u Observado."
                }
                db.update("PROTOCOLO", ContentValues().apply { putCabecera(datos) }, "id_protocolo = ?", arrayOf(idProtocolo))
                estadoActual = estado
            }

            db.delete("PRUEBA", "id_protocolo = ?", arrayOf(idProtocolo))
            for (prueba in datos.pruebas) {
                val idPrueba = db.insertOrThrow("PRUEBA", null, ContentValues().apply {
                    put("id_protocolo", idProtocolo)
                    put("id_tipo_prueba", prueba.tipo.idDb)
                    put("elemento", prueba.elemento)
                    put("momento", "UNICA")
                    put("realizada", if (prueba.realizada) 1 else 0)
                    put("motivo_no_realizada", prueba.motivoNoRealizada)
                    if (prueba.instrumentoId != null) put("id_instrumento", prueba.instrumentoId) else putNull("id_instrumento")
                    if (prueba.resultado != null) put("id_resultado", prueba.resultado.idDb) else putNull("id_resultado")
                    put("comentario", prueba.comentario)
                    put("ingresado_manualmente", if (prueba.ingresadoManualmente) 1 else 0)
                })
                var orden = 1
                for ((codigo, valor) in prueba.valores) {
                    val campo = Parametros.porCodigo(codigo) ?: continue
                    val idParametro = campo.idParametro ?: continue
                    db.insertOrThrow("MEDICION", null, ContentValues().apply {
                        put("id_prueba", idPrueba)
                        put("id_parametro", idParametro)
                        put("elemento", prueba.elemento)
                        put("valor", valor)
                        put("unidad", campo.unidad)
                        put("orden", orden++)
                    })
                }
            }

            if (registrarEnTerreno) {
                db.update(
                    "PROTOCOLO", ContentValues().apply { put("id_estado", EstadoProtocolo.REGISTRADO_TERRENO.idDb) },
                    "id_protocolo = ?", arrayOf(idProtocolo)
                )
                insertarHistorial(
                    db, idProtocolo, estadoActual, EstadoProtocolo.REGISTRADO_TERRENO, usuarioId, ahora,
                    if (estadoActual == EstadoProtocolo.OBSERVADO) "Corregido por el técnico" else null
                )
            }

            db.setTransactionSuccessful()
            idProtocolo
        } finally {
            db.endTransaction()
        }
    }

    private fun ContentValues.putCabecera(datos: ProtocoloAGuardar) {
        if (datos.temperatura != null) put("temperatura_c", datos.temperatura) else putNull("temperatura_c")
        if (datos.humedad != null) put("humedad_pct", datos.humedad) else putNull("humedad_pct")
        put("descripcion_mantenimiento", datos.descripcionMantenimiento?.ifBlank { null })
        put("observaciones", datos.observaciones?.ifBlank { null })
    }

    private fun validarVisitaAsignada(db: SQLiteDatabase, idVisita: Long, idEquipo: Long, usuarioId: Long, rol: String) {
        val todas = if (rol == Rol.JEFE_TECNICO) "1" else "0"
        val ok = db.rawQuery(
            """
            SELECT COUNT(*)
            FROM VISITA v INNER JOIN EQUIPO e ON e.id_instalacion = v.id_instalacion
            WHERE v.id_visita = ? AND e.id_equipo = ? AND v.fecha_hora_cierre IS NULL
              AND (? = '1' OR v.id_ingeniero_cargo = ?
                   OR EXISTS (SELECT 1 FROM VISITA_PARTICIPANTE vp
                              WHERE vp.id_visita = v.id_visita AND vp.id_usuario = ?))
            """.trimIndent(),
            arrayOf(idVisita.toString(), idEquipo.toString(), todas, usuarioId.toString(), usuarioId.toString())
        ).use { c -> c.moveToFirst() && c.getInt(0) == 1 }
        check(ok) { "La visita no está asignada a tu usuario, está cerrada o el equipo no pertenece a ella." }
    }

    private fun generarNumeroUnico(db: SQLiteDatabase, uuid: String): String {
        val anio = Calendar.getInstance().get(Calendar.YEAR)
        var candidato = uuid.replace("-", "")
        repeat(5) {
            val numero = "PT-$anio-${candidato.take(8).uppercase(Locale.US)}"
            val existe = db.rawQuery("SELECT 1 FROM PROTOCOLO WHERE numero = ?", arrayOf(numero)).use { it.moveToFirst() }
            if (!existe) return numero
            candidato = UUID.randomUUID().toString().replace("-", "")
        }
        error("No se pudo generar un número de protocolo único.")
    }

    suspend fun cambiarEstado(
        idProtocolo: String,
        nuevo: EstadoProtocolo,
        usuarioId: Long,
        rol: String,
        motivo: String?
    ) = withContext(Dispatchers.IO) {
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            val actual = db.rawQuery("SELECT id_estado FROM PROTOCOLO WHERE id_protocolo = ?", arrayOf(idProtocolo))
                .use { c ->
                    check(c.moveToFirst()) { "El protocolo ya no existe." }
                    EstadoProtocolo.desdeId(c.getLong(0))
                }
            check(Rol.puedeFormalizar(rol)) { "Solo el Ingeniero o el Jefe técnico pueden hacer este cambio." }
            when (nuevo) {
                EstadoProtocolo.FORMALIZADO, EstadoProtocolo.OBSERVADO ->
                    check(actual == EstadoProtocolo.REGISTRADO_TERRENO) {
                        "Solo se puede revisar un protocolo Registrado en terreno."
                    }
                EstadoProtocolo.ANULADO ->
                    check(actual != EstadoProtocolo.FORMALIZADO && actual != EstadoProtocolo.ANULADO) {
                        "Un protocolo Formalizado no se puede anular."
                    }
                else -> error("Transición no permitida.")
            }
            if (nuevo == EstadoProtocolo.OBSERVADO || nuevo == EstadoProtocolo.ANULADO) {
                require(!motivo.isNullOrBlank()) { "El motivo es obligatorio." }
            }
            db.update(
                "PROTOCOLO", ContentValues().apply { put("id_estado", nuevo.idDb) },
                "id_protocolo = ?", arrayOf(idProtocolo)
            )
            insertarHistorial(db, idProtocolo, actual, nuevo, usuarioId, Fechas.ahora(), motivo?.ifBlank { null })
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun insertarHistorial(
        db: SQLiteDatabase,
        idProtocolo: String,
        anterior: EstadoProtocolo?,
        nuevo: EstadoProtocolo,
        usuarioId: Long,
        fechaHora: String,
        motivo: String?
    ) {
        db.insertOrThrow("HISTORIAL_ESTADO", null, ContentValues().apply {
            put("id_protocolo", idProtocolo)
            if (anterior != null) put("id_estado_anterior", anterior.idDb) else putNull("id_estado_anterior")
            put("id_estado_nuevo", nuevo.idDb)
            put("id_usuario", usuarioId)
            put("fecha_hora", fechaHora)
            put("motivo", motivo)
        })
    }

    private fun Cursor.realONulo(indice: Int): Double? = if (isNull(indice)) null else getDouble(indice)
}
