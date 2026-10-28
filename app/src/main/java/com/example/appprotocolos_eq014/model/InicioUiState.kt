package com.example.appprotocolos_eq014.model

data class ProtocoloCliente(
    val numero: String,
    val fecha: String,
    val estado: String,
    val formalizado: Boolean
)

data class EquipoCliente(
    val id: Long,
    val tag: String,
    val descripcion: String,
    val protocolos: List<ProtocoloCliente>
)

data class InicioUiState(
    val isLoading: Boolean = true,
    val rolUsuario: String = "",
    val rolNombre: String = "",
    val primerNombre: String = "",
    val nombreCompleto: String = "",
    val empresa: String = "",
    val fechaHoy: String = "",
    val equiposCliente: List<EquipoCliente> = emptyList(),
    val busqueda: String = "",
    val visitas: List<VisitaResumen> = emptyList(),
    val actasCliente: List<VisitaResumen> = emptyList() // RF-15: actas de visitas cerradas
) {
    val equiposFiltrados: List<EquipoCliente>
        get() = equiposCliente.filter { it.tag.contains(busqueda.trim(), ignoreCase = true) }
}
