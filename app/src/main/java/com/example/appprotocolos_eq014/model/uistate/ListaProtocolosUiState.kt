package com.example.appprotocolos_eq014.model.uistate

import com.example.appprotocolos_eq014.model.entidades.Protocolo
import com.example.appprotocolos_eq014.model.catalogos.Rol
import com.example.appprotocolos_eq014.model.catalogos.EstadoProtocolo

data class ListaProtocolosUiState(
    val cargando: Boolean = true,
    val idUsuario: Long = -1,
    val rolUsuario: String = "",
    val protocolos: List<Protocolo> = emptyList(),
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

    val filtrados: List<Protocolo>
        get() {
            val q = busqueda.trim()
            return protocolos.filter { p ->
                (filtroEstado == null || p.estado == filtroEstado) &&
                        (!soloMios || p.usuarioRegistraId == idUsuario) &&
                        (!soloAtrasados || p.atrasado) &&
                        (q.isEmpty() ||
                                p.numero.contains(q, true) ||
                                p.tagEquipo.contains(q, true) ||
                                p.clienteNombre.contains(q, true) ||
                                p.instalacionNombre.contains(q, true) ||
                                p.tecnicoNombre.contains(q, true))
            }
        }
}

data class DetalleProtocoloUiState(
    val cargando: Boolean = true,
    val procesando: Boolean = false,
    val idUsuario: Long = -1,
    val rolUsuario: String = "",
    val detalle: Protocolo? = null,
    val historialEquipo: List<Protocolo> = emptyList(),
    val error: String? = null,
    val mensaje: String? = null
) {
    val puedeEditar: Boolean
        get() {
            val d = detalle ?: return false
            return Rol.puedeCrearProtocolos(rolUsuario) &&
                    d.usuarioRegistraId == idUsuario &&
                    (d.estado == EstadoProtocolo.BORRADOR || d.estado == EstadoProtocolo.OBSERVADO)
        }

    val puedeRevisar: Boolean
        get() = Rol.puedeFormalizar(rolUsuario) && detalle?.estado == EstadoProtocolo.REGISTRADO_TERRENO

    val puedeAnular: Boolean
        get() {
            val estado = detalle?.estado ?: return false
            return Rol.puedeFormalizar(rolUsuario) &&
                    estado != EstadoProtocolo.FORMALIZADO && estado != EstadoProtocolo.ANULADO
        }
}
