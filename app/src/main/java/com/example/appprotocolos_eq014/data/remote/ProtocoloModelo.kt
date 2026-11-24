package com.example.appprotocolos_eq014.data.remote

import com.example.appprotocolos_eq014.model.catalogos.EstadoProtocolo
import com.example.appprotocolos_eq014.model.catalogos.ResultadoPrueba
import com.example.appprotocolos_eq014.model.entidades.HistorialEstado
import com.example.appprotocolos_eq014.model.entidades.Medicion
import com.example.appprotocolos_eq014.model.entidades.Protocolo
import com.example.appprotocolos_eq014.model.entidades.Prueba
import com.example.appprotocolos_eq014.model.entidades.RecepcionActa
import com.example.appprotocolos_eq014.model.entidades.Visita
import com.example.appprotocolos_eq014.model.reglas.TipoPrueba

data class VisitaRequest(
    val usuarioId: Long,
    val numeroOt: String,
    val instalacionId: Long,
    val fecha: String,
    val tipoVisita: String,
    val ingenieroCargoId: Long,
    val participantes: List<Long>
)

data class ParticipanteResponse(
    val id: Long,
    val nombre: String
)

data class VisitaResponse(
    val id: Long,
    val numeroOt: String,
    val instalacionId: Long,
    val instalacionNombre: String,
    val clienteId: Long,
    val clienteNombre: String,
    val fecha: String,
    val tipoVisita: String,
    val tipoVisitaEtiqueta: String,
    val ingenieroCargoId: Long,
    val ingenieroNombre: String,
    val participantes: List<ParticipanteResponse>,
    val cerrada: Boolean,
    val fechaHoraCierre: String?,
    val totalProtocolos: Long,
    val protocolosRegistrados: Long
)

data class RecepcionRequest(
    val receptorNombre: String,
    val receptorRut: String,
    val receptorCargo: String
)

data class RecepcionResponse(
    val nombre: String,
    val rut: String,
    val cargo: String,
    val fechaHora: String
)

data class MedicionResponse(
    val parametroId: Long,
    val parametroCodigo: String,
    val parametroNombre: String,
    val valor: Double?,
    val unidad: String
)

data class PruebaResponse(
    val id: Long,
    val tipoPruebaId: Long,
    val tipoPruebaCodigo: String,
    val tipoPruebaNombre: String,
    val elemento: String,
    val momento: String,
    val realizada: Boolean,
    val motivoNoRealizada: String?,
    val instrumentoId: Long?,
    val resultado: String?,
    val comentario: String?,
    val ingresadoManualmente: Boolean,
    val mediciones: List<MedicionResponse>
)

data class HistorialResponse(
    val estadoAnterior: String?,
    val estadoNuevo: String,
    val usuarioId: Long,
    val usuarioNombre: String,
    val fechaHora: String,
    val motivo: String?
)

data class EvidenciaResponse(
    val id: Long,
    val nombreOriginal: String,
    val tipoContenido: String,
    val tamano: Long,
    val descripcion: String?,
    val usuarioId: Long,
    val fechaHora: String,
    val url: String
)

data class ProtocoloDetalleResponse(
    val id: String,
    val numero: String,
    val visitaId: Long,
    val numeroOt: String,
    val tipoVisita: String,
    val equipoId: Long,
    val equipoTag: String,
    val tipoEquipoNombre: String,
    val instalacionNombre: String,
    val clienteNombre: String,
    val usuarioRegistraId: Long,
    val usuarioRegistraNombre: String,
    val fechaHoraPrueba: String,
    val temperaturaC: Double?,
    val humedadPct: Double?,
    val descripcionMantenimiento: String?,
    val observaciones: String?,
    val estado: String,
    val estadoEtiqueta: String,
    val protocoloReemplazadoId: String?,
    val pruebas: List<PruebaResponse>,
    val historial: List<HistorialResponse>,
    val evidencias: List<EvidenciaResponse>
)

data class ActaResponse(
    val visita: VisitaResponse,
    val recepcion: RecepcionResponse?,
    val protocolos: List<ProtocoloDetalleResponse>
)

fun fechaHoraLocal(valor: String?): String? = valor?.replace('T', ' ')?.take(16)

fun estadoDesde(codigo: String?): EstadoProtocolo =
    EstadoProtocolo.entries.firstOrNull { it.name == codigo } ?: EstadoProtocolo.BORRADOR

fun resultadoDesde(codigo: String?): ResultadoPrueba? =
    ResultadoPrueba.entries.firstOrNull { it.name == codigo }

fun VisitaResponse.aVisita() = Visita(
    id = id,
    numeroOt = numeroOt,
    instalacionId = instalacionId,
    fecha = fecha,
    tipoVisita = tipoVisita,
    ingenieroCargoId = ingenieroCargoId,
    fechaHoraCierre = fechaHoraLocal(fechaHoraCierre),
    instalacionNombre = instalacionNombre,
    clienteNombre = clienteNombre,
    ingenieroNombre = ingenieroNombre,
    tecnicos = participantes.joinToString(", ") { it.nombre }.ifEmpty { "—" },
    participantes = participantes.map { it.id },
    totalProtocolos = totalProtocolos.toInt(),
    protocolosRegistrados = protocolosRegistrados.toInt()
)

fun MedicionResponse.aMedicion(idPrueba: Long, orden: Int): Medicion? = valor?.let {
    Medicion(
        pruebaId = idPrueba,
        parametroId = parametroId,
        valor = it,
        unidad = unidad,
        orden = orden,
        parametroCodigo = parametroCodigo,
        parametroNombre = parametroNombre
    )
}

fun PruebaResponse.aPrueba(idProtocolo: String): Prueba? = TipoPrueba.desdeId(tipoPruebaId)?.let { tipo ->
    Prueba(
        id = id,
        protocoloId = idProtocolo,
        tipo = tipo,
        elemento = elemento,
        momento = momento,
        realizada = realizada,
        motivoNoRealizada = motivoNoRealizada,
        instrumentoId = instrumentoId,
        resultado = resultadoDesde(resultado),
        comentario = comentario,
        ingresadoManualmente = ingresadoManualmente,
        mediciones = mediciones.mapIndexedNotNull { orden, medicion -> medicion.aMedicion(id, orden) }
    )
}

fun HistorialResponse.aHistorial() = HistorialEstado(
    anterior = estadoAnterior?.let { estadoDesde(it) },
    nuevo = estadoDesde(estadoNuevo),
    usuario = usuarioNombre,
    fechaHora = fechaHoraLocal(fechaHora).orEmpty(),
    motivo = motivo
)

fun ProtocoloDetalleResponse.aProtocolo(): Protocolo {
    val listaPruebas = pruebas.mapNotNull { it.aPrueba(id) }
    return Protocolo(
        id = id,
        numero = numero,
        visitaId = visitaId,
        equipoId = equipoId,
        usuarioRegistraId = usuarioRegistraId,
        fechaHoraPrueba = fechaHoraLocal(fechaHoraPrueba).orEmpty(),
        temperatura = temperaturaC,
        humedad = humedadPct,
        descripcionMantenimiento = descripcionMantenimiento,
        observaciones = observaciones,
        estado = estadoDesde(estado),
        numeroOt = numeroOt,
        tagEquipo = equipoTag,
        tipoEquipoNombre = tipoEquipoNombre,
        equipoDescripcion = tipoEquipoNombre,
        instalacionNombre = instalacionNombre,
        clienteNombre = clienteNombre,
        tecnicoNombre = usuarioRegistraNombre,
        resultadoGlobal = listaPruebas.filter { it.realizada }.mapNotNull { it.resultado }.maxByOrNull { it.idDb },
        pruebas = listaPruebas,
        historialEstados = historial.map { it.aHistorial() }
    )
}

fun ActaResponse.aVisita() = visita.aVisita().copy(
    recepcion = recepcion?.let { RecepcionActa(it.nombre, it.rut, it.cargo, fechaHoraLocal(it.fechaHora).orEmpty()) },
    protocolos = protocolos.map { it.aProtocolo() }.sortedBy { it.tagEquipo }
)

data class ProtocoloResumenResponse(
    val id: String,
    val numero: String,
    val visitaId: Long,
    val numeroOt: String,
    val equipoId: Long,
    val equipoTag: String?,
    val tipoEquipoNombre: String?,
    val instalacionNombre: String?,
    val clienteNombre: String?,
    val usuarioRegistraId: Long,
    val usuarioRegistraNombre: String?,
    val fechaHoraPrueba: String,
    val estado: String,
    val estadoEtiqueta: String,
    val resultado: String?,
    val lectura1Min: Double?,
    val lectura10Min: Double?,
    val protocoloReemplazadoId: String?
)

data class IndicadoresResponse(
    val total: Long,
    val porEstado: Map<String, Long>,
    val promedioDiasFormalizacion: Double?
)

data class CambioEstadoRequest(
    val usuarioId: Long,
    val estado: String,
    val motivo: String?
)

data class MedicionAnteriorResponse(
    val tipoPruebaId: Long,
    val elemento: String,
    val protocoloNumero: String,
    val fechaHoraPrueba: String,
    val parametroCodigo: String,
    val valor: Double?,
    val unidad: String
)

fun ProtocoloResumenResponse.aProtocolo() = Protocolo(
    id = id,
    numero = numero,
    visitaId = visitaId,
    equipoId = equipoId,
    usuarioRegistraId = usuarioRegistraId,
    fechaHoraPrueba = fechaHoraLocal(fechaHoraPrueba).orEmpty(),
    estado = estadoDesde(estado),
    numeroOt = numeroOt,
    tagEquipo = equipoTag.orEmpty(),
    tipoEquipoNombre = tipoEquipoNombre.orEmpty(),
    instalacionNombre = instalacionNombre.orEmpty(),
    clienteNombre = clienteNombre.orEmpty(),
    tecnicoNombre = usuarioRegistraNombre.orEmpty(),
    resultadoGlobal = resultadoDesde(resultado),
    lectura1Min = lectura1Min,
    lectura10Min = lectura10Min,
    reemplazaANumero = protocoloReemplazadoId
)