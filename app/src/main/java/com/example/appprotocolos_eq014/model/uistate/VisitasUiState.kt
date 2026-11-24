package com.example.appprotocolos_eq014.model.uistate

import com.example.appprotocolos_eq014.model.catalogos.EstadoProtocolo
import com.example.appprotocolos_eq014.model.catalogos.Rol
import com.example.appprotocolos_eq014.model.entidades.Equipo
import com.example.appprotocolos_eq014.model.entidades.Visita
import com.example.appprotocolos_eq014.util.Fechas

enum class FiltroVisita(val etiqueta: String) {
    TODAS("Todas"), EN_CURSO("En curso"), PROGRAMADAS("Programadas"), CERRADAS("Cerradas")
}

fun Visita.esProgramada(hoy: String): Boolean = !cerrada && fecha.take(10) > hoy

data class VisitasUiState(
    val cargando: Boolean = true,
    val visitas: List<Visita> = emptyList(),
    val rol: String = "",
    val busqueda: String = "",
    val filtro: FiltroVisita = FiltroVisita.TODAS,
    val hoy: String = Fechas.hoy(),
    val error: String? = null
) {
    val filtradas: List<Visita>
        get() = visitas.filter { visita ->
            val coincideEstado = when (filtro) {
                FiltroVisita.TODAS -> true
                FiltroVisita.EN_CURSO -> !visita.cerrada && !visita.esProgramada(hoy)
                FiltroVisita.PROGRAMADAS -> visita.esProgramada(hoy)
                FiltroVisita.CERRADAS -> visita.cerrada
            }
            val consulta = busqueda.trim()
            coincideEstado && (consulta.isEmpty() || listOf(
                visita.numeroOt, visita.clienteNombre, visita.instalacionNombre,
                visita.fecha, Fechas.fecha(visita.fecha)
            ).any { it.contains(consulta, ignoreCase = true) })
        }
}

data class DetalleVisitaUiState(
    val cargando: Boolean = true,
    val visita: Visita? = null,
    val equipos: List<Equipo> = emptyList(),
    val rol: String = "",
    val usuarioId: Long = -1,
    val error: String? = null,
    val hoy: String = Fechas.hoy(),
    val eliminando: Boolean = false,
    val eliminada: Boolean = false,
    val errorAccion: String? = null
) {
    val puedeEditarVisita: Boolean get() = Rol.puedeAsignarVisitas(rol) && visita?.cerrada == false
    val puedeEliminarVisita: Boolean
        get() = Rol.puedeAsignarVisitas(rol) && visita?.let { it.esProgramada(hoy) && it.protocolos.isEmpty() } == true

    val puedeRegistrar: Boolean get() = Rol.puedeCrearProtocolos(rol) && visita?.cerrada == false
    val puedeCerrar: Boolean
        get() {
            val actual = visita ?: return false
            val permitidos = setOf(EstadoProtocolo.REGISTRADO_TERRENO, EstadoProtocolo.FORMALIZADO, EstadoProtocolo.ANULADO)
            return rol in setOf(Rol.TECNICO, Rol.INGENIERO) && !actual.cerrada &&
                actual.protocolos.isNotEmpty() && actual.protocolos.all { it.estado in permitidos }
        }
}
