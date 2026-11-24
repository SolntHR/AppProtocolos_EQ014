package com.example.appprotocolos_eq014.repository

import com.example.appprotocolos_eq014.data.remote.CambioEstadoRequest
import com.example.appprotocolos_eq014.data.remote.InstalacionApi
import com.example.appprotocolos_eq014.data.remote.InstrumentoApi
import com.example.appprotocolos_eq014.data.remote.ProtocoloApi
import com.example.appprotocolos_eq014.data.remote.ProtocoloRequest
import com.example.appprotocolos_eq014.data.remote.RetrofitClient
import com.example.appprotocolos_eq014.data.remote.UsuarioApi
import com.example.appprotocolos_eq014.data.remote.aEquipo
import com.example.appprotocolos_eq014.data.remote.aInstrumento
import com.example.appprotocolos_eq014.data.remote.aProtocolo
import com.example.appprotocolos_eq014.data.remote.aRequest
import com.example.appprotocolos_eq014.data.remote.aVisita
import com.example.appprotocolos_eq014.data.remote.listaApi
import com.example.appprotocolos_eq014.data.remote.llamarApi
import com.example.appprotocolos_eq014.data.remote.sinContenidoApi
import com.example.appprotocolos_eq014.model.catalogos.EstadoProtocolo
import com.example.appprotocolos_eq014.model.entidades.Equipo
import com.example.appprotocolos_eq014.model.entidades.Instrumento
import com.example.appprotocolos_eq014.model.entidades.Protocolo
import com.example.appprotocolos_eq014.model.entidades.Visita
import com.example.appprotocolos_eq014.model.reglas.MedicionAnterior
import com.example.appprotocolos_eq014.model.reglas.ReglasProtocolo
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ProtocoloRepository(
    private val protocoloApi: ProtocoloApi = RetrofitClient.protocoloApi,
    private val instalacionApi: InstalacionApi = RetrofitClient.instalacionApi,
    private val instrumentoApi: InstrumentoApi = RetrofitClient.instrumentoApi,
    private val usuarioApi: UsuarioApi = RetrofitClient.usuarioApi
) {

    suspend fun listar(): List<Protocolo> =
        conReemplazos(listaApi { protocoloApi.listarProtocolos(null, null, null, null) }.map { it.aProtocolo() })
            .sortedByDescending { it.fechaHoraPrueba }

    suspend fun promedioDiasFormalizacion(): Double? =
        llamarApi { protocoloApi.indicadores() }.promedioDiasFormalizacion

    suspend fun historialEquipo(idEquipo: Long): List<Protocolo> =
        conReemplazos(listaApi { protocoloApi.historialEquipo(idEquipo) }.map { it.aProtocolo() })

    suspend fun detalle(idProtocolo: String): Protocolo {
        val protocolo = paraEditar(idProtocolo)
        val equipo = llamarApi { instalacionApi.obtenerEquipo(protocolo.equipoId) }
        val instrumentos = instrumentos().associate { it.id to it.descripcion }
        val anteriores = medicionesAnteriores(protocolo.equipoId, protocolo.id, protocolo.fechaHoraPrueba)
        return protocolo.copy(
            pruebas = protocolo.pruebas.map { prueba ->
                val anterior = anteriores["${prueba.tipo.idDb}|${prueba.elemento}"]
                prueba.copy(
                    instrumentoNombre = prueba.instrumentoId?.let { instrumentos[it] },
                    calculo = ReglasProtocolo.resumenCalculo(prueba.tipo, prueba.valores, equipo.maxContactosUohm),
                    comparaciones = if (anterior == null || !prueba.realizada) emptyList()
                    else ReglasProtocolo.comparar(prueba.tipo, prueba.valores, anterior)
                )
            }
        )
    }

    suspend fun cambiarEstado(idProtocolo: String, nuevo: EstadoProtocolo, usuarioId: Long, motivo: String?) {
        val datos = CambioEstadoRequest(usuarioId, nuevo.name, motivo?.trim()?.ifEmpty { null })
        llamarApi { protocoloApi.cambiarEstado(idProtocolo, datos) }
    }

    suspend fun paraEditar(idProtocolo: String): Protocolo =
        llamarApi { protocoloApi.obtenerProtocolo(idProtocolo) }.aProtocolo()

    suspend fun nombreUsuario(idUsuario: Long): String =
        llamarApi { usuarioApi.obtener(idUsuario) }.nombre

    suspend fun instrumentos(): List<Instrumento> =
        listaApi { instrumentoApi.listar(null) }.map { it.aInstrumento() }

    suspend fun visitasAbiertas(idUsuario: Long): List<Visita> =
        listaApi { protocoloApi.listarVisitas(idUsuario, true) }.map { it.aVisita() }

    suspend fun visita(idVisita: Long): Visita =
        llamarApi { protocoloApi.obtenerVisita(idVisita) }.aVisita()

    suspend fun equiposDisponibles(idVisita: Long, idInstalacion: Long, idProtocoloActual: String?): List<Equipo> {
        val ocupados = listaApi { protocoloApi.listarProtocolos(null, null, idVisita, null) }
            .filter { it.id != idProtocoloActual }
            .map { it.equipoId }
            .toSet()
        return listaApi { instalacionApi.listarEquipos(idInstalacion, null, null) }
            .filter { it.id !in ocupados }
            .map { it.aEquipo() }
            .sortedBy { it.tag }
    }

    suspend fun medicionesAnteriores(idEquipo: Long, idProtocoloActual: String?, antesDe: String): Map<String, MedicionAnterior> =
        listaApi { protocoloApi.medicionesAnteriores(idEquipo, antesDe.replace(' ', 'T'), idProtocoloActual) }
            .groupBy { "${it.tipoPruebaId}|${it.elemento}" }
            .mapValues { (_, filas) ->
                MedicionAnterior(
                    filas.first().protocoloNumero,
                    filas.mapNotNull { fila -> fila.valor?.let { fila.parametroCodigo to it } }.toMap()
                )
            }

    suspend fun guardar(datos: Protocolo, usuarioId: Long, registrar: Boolean): Protocolo {
        val solicitud = ProtocoloRequest(
            usuarioId = usuarioId,
            visitaId = datos.visitaId,
            equipoId = datos.equipoId,
            temperaturaC = datos.temperatura,
            humedadPct = datos.humedad,
            descripcionMantenimiento = datos.descripcionMantenimiento?.trim()?.ifEmpty { null },
            observaciones = datos.observaciones?.trim()?.ifEmpty { null },
            registrar = registrar,
            pruebas = datos.pruebas.map { it.aRequest() }
        )
        val respuesta = if (datos.esNuevo) llamarApi { protocoloApi.crearProtocolo(solicitud) }
        else llamarApi { protocoloApi.actualizarProtocolo(datos.id, solicitud) }
        return respuesta.aProtocolo()
    }

    suspend fun subirEvidencia(idProtocolo: String, usuarioId: Long, foto: ByteArray) {
        val archivo = MultipartBody.Part.createFormData(
            "archivo",
            "evidencia_${System.currentTimeMillis()}.jpg",
            foto.toRequestBody("image/jpeg".toMediaType())
        )
        llamarApi { protocoloApi.subirEvidencia(idProtocolo, usuarioId, archivo) }
    }

    suspend fun eliminarEvidencia(idEvidencia: Long, usuarioId: Long) {
        sinContenidoApi { protocoloApi.eliminarEvidencia(idEvidencia, usuarioId) }
    }

    private fun conReemplazos(protocolos: List<Protocolo>): List<Protocolo> {
        val numeros = protocolos.associate { it.id to it.numero }
        val reemplazadoPor = protocolos.filter { it.reemplazaANumero != null }
            .associate { it.reemplazaANumero to it.numero }
        return protocolos.map {
            it.copy(
                reemplazaANumero = it.reemplazaANumero?.let { id -> numeros[id] ?: id },
                reemplazadoPorNumero = reemplazadoPor[it.id]
            )
        }
    }
}