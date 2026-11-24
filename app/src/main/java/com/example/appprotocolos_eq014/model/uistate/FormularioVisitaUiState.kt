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
    val busquedaTecnicos: String = "",
    val errores: Map<String, String> = emptyMap(),
    val error: String? = null,
    val visitaCreadaId: Long? = null
) {
    val editando: Boolean get() = idVisita != null

    val personalVisible: List<OpcionCatalogo>
        get() {
            val texto = busquedaTecnicos.trim()
            if (texto.isEmpty()) return personal
            return personal.filter { it.id in participantes || it.nombre.contains(texto, ignoreCase = true) }
        }
}