package com.example.appprotocolos_eq014.model.uistate

import com.example.appprotocolos_eq014.model.entidades.Visita

data class FormularioVisitaUiState(
    val idVisita: Long? = null,
    val fechaOriginal: String? = null,
    val instalacionBloqueada: Boolean = false,
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val instalaciones: List<OpcionCatalogo> = emptyList(),
    val ingenieros: List<OpcionCatalogo> = emptyList(),
    val personal: List<OpcionCatalogo> = emptyList(),
    val numeroOt: String = "",
    val instalacion: OpcionCatalogo? = null,
    val fecha: String = "",
    val tipoVisita: String = Visita.SOLO_PRUEBAS,
    val ingeniero: OpcionCatalogo? = null,
    val participantes: Set<Long> = emptySet(),
    val errores: Map<String, String> = emptyMap(),
    val error: String? = null,
    val visitaCreadaId: Long? = null
) {
    val editando: Boolean get() = idVisita != null
}