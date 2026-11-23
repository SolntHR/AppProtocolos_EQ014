package com.example.appprotocolos_eq014.model.uistate

import com.example.appprotocolos_eq014.model.entidades.Equipo
import com.example.appprotocolos_eq014.model.entidades.Protocolo

data class EquiposUiState(
    val cargando: Boolean = true,
    val equipos: List<Equipo> = emptyList(),
    val rol: String = "",
    val busqueda: String = "",
    val tipoId: Long? = null,
    val instalacionId: Long? = null,
    val error: String? = null
) {
    val filtrados: List<Equipo>
        get() {
            val consulta = busqueda.trim()
            return equipos.filter { equipo ->
                (tipoId == null || equipo.tipoEquipoId == tipoId) &&
                    (instalacionId == null || equipo.instalacionId == instalacionId) &&
                    (consulta.isEmpty() || listOfNotNull(
                        equipo.tag, equipo.tipoEquipoNombre, equipo.marca, equipo.modelo,
                        equipo.numeroSerie, equipo.instalacionNombre, equipo.clienteNombre
                    ).any { it.contains(consulta, ignoreCase = true) })
            }
        }
}

data class DetalleEquipoUiState(
    val cargando: Boolean = true,
    val equipo: Equipo? = null,
    val protocolos: List<Protocolo> = emptyList(),
    val error: String? = null
)
