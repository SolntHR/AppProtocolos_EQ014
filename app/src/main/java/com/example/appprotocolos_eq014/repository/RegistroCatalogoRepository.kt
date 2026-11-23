package com.example.appprotocolos_eq014.repository

import com.example.appprotocolos_eq014.data.InduztekDbHelper
import com.example.appprotocolos_eq014.data.remote.EquipoRequest
import com.example.appprotocolos_eq014.data.remote.InstalacionApi
import com.example.appprotocolos_eq014.data.remote.InstalacionRequest
import com.example.appprotocolos_eq014.data.remote.RetrofitClient
import com.example.appprotocolos_eq014.data.remote.UsuarioApi
import com.example.appprotocolos_eq014.data.remote.aEquipo
import com.example.appprotocolos_eq014.data.remote.listaApi
import com.example.appprotocolos_eq014.data.remote.llamarApi
import com.example.appprotocolos_eq014.model.entidades.Equipo
import com.example.appprotocolos_eq014.model.entidades.Instalacion
import com.example.appprotocolos_eq014.model.reglas.ReglasProtocolo
import com.example.appprotocolos_eq014.model.uistate.OpcionCatalogo
import com.example.appprotocolos_eq014.model.entidades.Visita
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegistroCatalogoRepository(
    private val helper: InduztekDbHelper,
    private val instalacionApi: InstalacionApi = RetrofitClient.instalacionApi,
    private val usuarioApi: UsuarioApi = RetrofitClient.usuarioApi
) {

    suspend fun instalaciones(): List<OpcionCatalogo> =
        listaApi { instalacionApi.listarInstalaciones(null) }
            .sortedWith(compareBy({ it.clienteNombre.lowercase() }, { it.nombre.lowercase() }))
            .map { OpcionCatalogo(it.id, "${it.nombre} · ${it.clienteNombre}") }

    suspend fun tiposEquipo(): List<OpcionCatalogo> =
        listaApi { instalacionApi.tiposEquipo() }
            .sortedBy { it.id }
            .map { OpcionCatalogo(it.id, it.nombre) }

    suspend fun clientes(): List<OpcionCatalogo> =
        listaApi { usuarioApi.listar("CLIENTE", true, null) }
            .sortedBy { it.nombre.lowercase() }
            .map { OpcionCatalogo(it.id, it.nombre) }

    suspend fun comunas(): List<OpcionCatalogo> =
        listaApi { instalacionApi.comunas(null) }
            .sortedBy { it.nombre.lowercase() }
            .map { OpcionCatalogo(it.id, "${it.nombre} · ${it.regionNombre}") }

    suspend fun buscarEquipo(idInstalacion: Long, tag: String): Equipo? =
        listaApi { instalacionApi.listarEquipos(idInstalacion, null, null) }
            .firstOrNull { it.tag.equals(tag, ignoreCase = true) }
            ?.aEquipo()

    suspend fun crearEquipo(e: Equipo): Long {
        val datos = EquipoRequest(
            instalacionId = e.instalacionId,
            tag = e.tag,
            tipoEquipoId = e.tipoEquipoId,
            marca = e.marca,
            modelo = e.modelo,
            numeroSerie = e.numeroSerie,
            anio = e.anio,
            observaciones = e.observaciones,
            potenciaKva = e.potenciaKva,
            tensionKv = ReglasProtocolo.numero(e.tensiones),
            corrienteA = e.corriente,
            maxContactosUohm = e.maxContactosUohm
        )
        return llamarApi { instalacionApi.crearEquipo(datos) }.id
    }

    suspend fun existeInstalacion(idCliente: Long, nombre: String): Boolean =
        listaApi { instalacionApi.listarInstalaciones(idCliente) }
            .any { it.nombre.trim().equals(nombre.trim(), ignoreCase = true) }

    suspend fun crearInstalacion(instalacion: Instalacion): Long {
        val datos = InstalacionRequest(
            clienteId = instalacion.clienteId,
            nombre = instalacion.nombre,
            ubicacion = instalacion.ubicacion,
            comunaId = instalacion.comunaId,
            observacionesAcceso = instalacion.observacionesAcceso
        )
        return llamarApi { instalacionApi.crearInstalacion(datos) }.id
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