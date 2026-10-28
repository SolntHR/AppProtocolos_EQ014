package com.example.appprotocolos_eq014.model

enum class EstadoProtocolo(val idDb: Long, val etiqueta: String) {
    BORRADOR(1, "Borrador"),
    REGISTRADO_TERRENO(2, "Registrado en terreno"),
    OBSERVADO(3, "Observado"),
    FORMALIZADO(4, "Formalizado"),
    ANULADO(5, "Anulado");

    val etiquetaCliente: String
        get() = if (this == FORMALIZADO) etiqueta else "Pendiente de formalización"

    val visibleParaCliente: Boolean
        get() = this != BORRADOR && this != ANULADO

    companion object {
        fun desdeId(id: Long): EstadoProtocolo = entries.firstOrNull { it.idDb == id } ?: BORRADOR
    }
}
