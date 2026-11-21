package com.example.appprotocolos_eq014.model.catalogos

object Rol{
    const val  TECNICO = "TECNICO"
    const val INGENIERO = "INGENIERO"
    const val JEFE_TECNICO = "JEFE_TECNICO"
    const val CLIENTE = "CLIENTE"

    fun puedeCrearProtocolos(codigo: String) = codigo == TECNICO || codigo == INGENIERO || codigo == JEFE_TECNICO
    fun puedeFormalizar(codigo: String) = codigo == TECNICO || codigo == INGENIERO
    fun esPersonalInduztek(codigo: String) = codigo.isNotBlank() && codigo != CLIENTE
}