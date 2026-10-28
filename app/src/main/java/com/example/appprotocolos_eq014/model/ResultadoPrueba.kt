package com.example.appprotocolos_eq014.model

enum class ResultadoPrueba(val idDb: Long, val etiqueta: String) {
    APROBADO(1, "Aprobado"),
    OBSERVADO(2, "Observado"),
    RECHAZADO(3, "Rechazado");

    companion object {
        fun desdeId(id: Long?): ResultadoPrueba? = entries.firstOrNull { it.idDb == id }

        fun desdeIndicePolarizacion(ip: Double): ResultadoPrueba = when {
            ip >= 2.0 -> APROBADO
            ip >= 1.0 -> OBSERVADO
            else -> RECHAZADO
        }
    }
}
