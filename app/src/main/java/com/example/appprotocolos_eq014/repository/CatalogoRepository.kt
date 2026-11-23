package com.example.appprotocolos_eq014.repository

import com.example.appprotocolos_eq014.data.remote.InstalacionApi
import com.example.appprotocolos_eq014.data.remote.RetrofitClient
import com.example.appprotocolos_eq014.data.remote.aEquipo
import com.example.appprotocolos_eq014.data.remote.listaApi
import com.example.appprotocolos_eq014.model.entidades.Equipo


class CatalogoRepository(
    private val instalacionApi: InstalacionApi = RetrofitClient.instalacionApi
    ) {

        suspend fun listarEquipos(idCliente: Long? = null): List<Equipo> {
            val clientes = listaApi { instalacionApi.listarInstalaciones(idCliente) }
                .associate { it.id to it.clienteNombre }
            return listaApi { instalacionApi.listarEquipos(null, idCliente, null) }
                .map { it.aEquipo(clientes[it.instalacionId].orEmpty()) }
                .sortedWith(compareBy({ it.clienteNombre.lowercase() }, { it.instalacionNombre.lowercase() }, { it.tag }))
        }

        suspend fun obtenerEquiposPorCliente(idCliente: Long): List<Equipo> =
            listarEquipos(idCliente).sortedBy { it.tag }
}
