package com.example.appprotocolos_eq014.model.catalogos

enum class ResultadoPrueba(val idDb: Long, val etiqueta: String) {
    APROBADO(1, "Aprobado"),
    OBSERVADO(2, "Observado"),
    RECHAZADO(3, "Rechazado");

    companion object {
        fun desdeId(id: Long?): ResultadoPrueba? = entries.firstOrNull { it.idDb == id }

    }
}
