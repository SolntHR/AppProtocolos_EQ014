package com.example.appprotocolos_eq014.repository

import com.example.appprotocolos_eq014.data.remote.EquipoRequest
import com.example.appprotocolos_eq014.data.remote.InstalacionApi
import com.example.appprotocolos_eq014.data.remote.InstalacionRequest
import com.example.appprotocolos_eq014.data.remote.ProtocoloApi
import com.example.appprotocolos_eq014.data.remote.RetrofitClient
import com.example.appprotocolos_eq014.data.remote.UsuarioApi
import com.example.appprotocolos_eq014.data.remote.aEquipo
import com.example.appprotocolos_eq014.data.remote.aVisita
import com.example.appprotocolos_eq014.data.remote.listaApi
import com.example.appprotocolos_eq014.data.remote.llamarApi
import com.example.appprotocolos_eq014.model.entidades.Equipo
import com.example.appprotocolos_eq014.model.entidades.Instalacion
import com.example.appprotocolos_eq014.model.reglas.ReglasProtocolo
import com.example.appprotocolos_eq014.model.uistate.OpcionCatalogo
import com.example.appprotocolos_eq014.model.entidades.Visita

class RegistroCatalogoRepository(
    private val instalacionApi: InstalacionApi = RetrofitClient.instalacionApi,
    private val usuarioApi: UsuarioApi = RetrofitClient.usuarioApi,
    private val protocoloApi: ProtocoloApi = RetrofitClient.protocoloApi
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

    suspend fun visitasCerradasDeCliente(idCliente: Long): List<Visita> =
        listaApi { protocoloApi.listarVisitas(idCliente, false) }
            .map { it.aVisita() }
            .sortedByDescending { it.fecha }

    suspend fun visitaEsDeCliente(idVisita: Long, idCliente: Long): Boolean =
        visitasCerradasDeCliente(idCliente).any { it.id == idVisita }
}