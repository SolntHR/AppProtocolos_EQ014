package com.example.appprotocolos_eq014.model.entidades

data class Visita(
    val id: Long = 0,
    val numeroOt: String,
    val instalacionId: Long,
    val fecha: String,
    val tipoVisita: String,
    val ingenieroCargoId: Long = 0,
    val fechaHoraCierre: String? = null,

    val instalacionNombre: String = "",
    val clienteNombre: String = "",
    val ingenieroNombre: String = "",
    val tecnicos: String = "",
    val participantes: List<Long> = emptyList(),


    val totalProtocolos: Int = 0,
    val protocolosRegistrados: Int = 0,
    val protocolosFormalizados: Int = 0,

    val protocolos: List<Protocolo> = emptyList(),
    val recepcion: RecepcionActa? = null
){
    val cerrada: Boolean get() = fechaHoraCierre != null

    val incluyeMantenimiento: Boolean get() = tipoVisita == PRUEBAS_Y_MANTENIMIENTO

    val tipoVisitaTexto: String
        get() = if (incluyeMantenimiento) "Pruebas y mantenimiento" else "Solo pruebas"

    companion object {
        const val SOLO_PRUEBAS = "SOLO_PRUEBAS"
        const val PRUEBAS_Y_MANTENIMIENTO = "PRUEBAS_Y_MANT"
    }
}
