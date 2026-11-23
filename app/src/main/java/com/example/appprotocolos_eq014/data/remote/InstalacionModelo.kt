package com.example.appprotocolos_eq014.data.remote

import com.example.appprotocolos_eq014.model.entidades.Equipo

data class RegionResponse(
    val id: Long,
    val nombre: String
)

data class ComunaResponse(
    val id: Long,
    val nombre: String,
    val regionId: Long,
    val regionNombre: String
)

data class TipoEquipoResponse(
    val id: Long,
    val nombre: String,
    val clasificacion: String
)

data class InstalacionRequest(
    val clienteId: Long,
    val nombre: String,
    val ubicacion: String?,
    val comunaId: Long,
    val observacionesAcceso: String?
)

data class InstalacionResponse(
    val id: Long,
    val clienteId: Long,
    val clienteNombre: String,
    val nombre: String,
    val ubicacion: String?,
    val comunaId: Long,
    val comunaNombre: String,
    val regionNombre: String,
    val observacionesAcceso: String?
)

data class EquipoRequest(
    val instalacionId: Long,
    val tag: String,
    val tipoEquipoId: Long,
    val marca: String?,
    val modelo: String?,
    val numeroSerie: String?,
    val anio: Int?,
    val observaciones: String?,
    val potenciaKva: Double?,
    val tensionKv: Double?,
    val corrienteA: Double?,
    val maxContactosUohm: Double?
)

data class EquipoResponse(
    val id: Long,
    val instalacionId: Long,
    val instalacionNombre: String,
    val clienteId: Long,
    val tag: String,
    val tipoEquipoId: Long,
    val tipoEquipoNombre: String,
    val clasificacion: String,
    val marca: String?,
    val modelo: String?,
    val numeroSerie: String?,
    val anio: Int?,
    val observaciones: String?,
    val potenciaKva: Double?,
    val tensionKv: Double?,
    val corrienteA: Double?,
    val maxContactosUohm: Double?
)

fun EquipoResponse.aEquipo(clienteNombre: String = "") = Equipo(
    id = id,
    instalacionId = instalacionId,
    tipoEquipoId = tipoEquipoId,
    tag = tag,
    marca = marca,
    modelo = modelo,
    numeroSerie = numeroSerie,
    anio = anio,
    potenciaKva = potenciaKva,
    tensiones = tensionKv?.let { "${it.toString().removeSuffix(".0").replace('.', ',')} kV" },
    corriente = corrienteA,
    maxContactosUohm = maxContactosUohm,
    observaciones = observaciones,
    tipoEquipoNombre = tipoEquipoNombre,
    instalacionNombre = instalacionNombre,
    clienteNombre = clienteNombre
)