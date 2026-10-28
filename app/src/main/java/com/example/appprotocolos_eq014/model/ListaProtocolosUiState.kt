package com.example.appprotocolos_eq014.model

data class ListaProtocolosUiState(
    val cargando: Boolean = true,
    val idUsuario: Long = -1,
    val rolUsuario: String = "",
    val protocolos: List<ProtocoloListado> = emptyList(),
    val filtroEstado: EstadoProtocolo? = null,
    val busqueda: String = "",
    val soloMios: Boolean = false,
    val soloAtrasados: Boolean = false,
    val promedioDiasFormalizacion: Double? = null,
    val error: String? = null
) {
    val pendientesFormalizar: Int
        get() = protocolos.count { it.estado == EstadoProtocolo.REGISTRADO_TERRENO || it.estado == EstadoProtocolo.OBSERVADO }
    val atrasados: Int get() = protocolos.count { it.atrasado }

    val filtrados: List<ProtocoloListado>
        get() {
            val q = busqueda.trim()
            return protocolos.filter { p ->
                (filtroEstado == null || p.estado == filtroEstado) &&
                        (!soloMios || p.tecnicoId == idUsuario) &&
                        (!soloAtrasados || p.atrasado) &&
                        (q.isEmpty() ||
                                p.numero.contains(q, true) ||
                                p.tagEquipo.contains(q, true) ||
                                p.cliente.contains(q, true) ||
                                p.instalacion.contains(q, true) ||
                                p.tecnico.contains(q, true))
            }
        }
}

data class DetalleProtocoloUiState(
    val cargando: Boolean = true,
    val procesando: Boolean = false,
    val idUsuario: Long = -1,
    val rolUsuario: String = "",
    val detalle: ProtocoloDetalle? = null,
    val error: String? = null,
    val mensaje: String? = null
) {
    val puedeEditar: Boolean
        get() {
            val d = detalle ?: return false
            return Rol.puedeCrearProtocolos(rolUsuario) &&
                    d.resumen.tecnicoId == idUsuario &&
                    (d.resumen.estado == EstadoProtocolo.BORRADOR || d.resumen.estado == EstadoProtocolo.OBSERVADO)
        }

    val puedeRevisar: Boolean
        get() = Rol.puedeFormalizar(rolUsuario) && detalle?.resumen?.estado == EstadoProtocolo.REGISTRADO_TERRENO

    val puedeAnular: Boolean
        get() {
            val estado = detalle?.resumen?.estado ?: return false
            return Rol.puedeFormalizar(rolUsuario) &&
                    estado != EstadoProtocolo.FORMALIZADO && estado != EstadoProtocolo.ANULADO
        }
}
