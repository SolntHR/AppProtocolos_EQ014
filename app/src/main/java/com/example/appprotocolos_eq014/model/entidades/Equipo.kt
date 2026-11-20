package com.example.appprotocolos_eq014.model.entidades

data class Equipo(
    val id: Long = 0,
    val instalacionId: Long = 0,
    val tipoEquipoId: Long = 0,
    val tag: String,
    val marca: String? = null,
    val modelo: String? = null,
    val numeroSerie: String? = null,
    val anio: Int? = null,
    val potenciaKva: Double? = null,
    val tensiones: String? = null,
    val corriente: Double? = null,
    val maxContactosUohm: Double? = null,
    val observaciones: String? = null,
    val tipoEquipoNombre: String = "",
    val instalacionNombre: String = ""
){
    val descripcion: String
        get() = listOfNotNull(
            tipoEquipoNombre.ifBlank { null },
            marca,
            numeroSerie?.let { "Serie $it" }
        ).joinToString(" · ")
}