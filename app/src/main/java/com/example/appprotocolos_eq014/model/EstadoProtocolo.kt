package com.example.appprotocolos_eq014.model

enum class EstadoProtocolo(val idDb: Long) {
    BORRADOR(1),
    REGISTRADO_TERRENO(2),
    OBSERVADO(3),
    FORMALIZADO(4),
    ANULADO(5)
}