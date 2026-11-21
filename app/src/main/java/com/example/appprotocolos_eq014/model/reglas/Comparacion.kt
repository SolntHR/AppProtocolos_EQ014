package com.example.appprotocolos_eq014.model.reglas

data class Comparacion(
    val codigo: String,
    val nombre: String,
    val valorAnterior: Double,
    val valorActual: Double,
    val unidad: String,
    val numeroAnterior: String?
){
    val variacionPct: Double
        get() = if (valorAnterior == 0.0) 0.0 else (valorActual - valorAnterior) / valorAnterior * 100

    val significativa: Boolean get() = kotlin.math.abs(variacionPct) > 25.0
}
