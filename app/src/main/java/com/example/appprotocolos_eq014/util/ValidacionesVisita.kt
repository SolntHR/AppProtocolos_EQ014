package com.example.appprotocolos_eq014.util

import com.example.appprotocolos_eq014.model.uistate.FormularioVisitaUiState

object ValidacionesVisita {
    const val CAMPO_OT = "numeroOt"
    const val CAMPO_INSTALACION = "instalacionId"
    const val CAMPO_FECHA = "fecha"
    const val CAMPO_INGENIERO = "ingenieroCargoId"
    const val CAMPO_PARTICIPANTES = "participantes"

    private val regexOt = Regex("^OT-\\d{4}-\\d{4}$")

    fun normalizarOt(ot: String): String = ot.trim().uppercase()

    fun validar(e: FormularioVisitaUiState): Map<String, String> {
        val errores = mutableMapOf<String, String>()
        val ot = normalizarOt(e.numeroOt)
        when {
            ot.isEmpty() -> errores[CAMPO_OT] = "Ingresa el número de OT"
            !regexOt.matches(ot) -> errores[CAMPO_OT] = "Usa el formato OT-2026-0001"
        }
        if (e.instalacion == null) errores[CAMPO_INSTALACION] = "Selecciona la instalación"
        val fecha = Formato.fechaParaGuardar(e.fecha)
        when {
            fecha == null -> errores[CAMPO_FECHA] = "Fecha inválida. Formato: dd-mm-aaaa"
            fecha != e.fechaOriginal && (Formato.diasHasta(fecha) ?: 0) < 0 -> errores[CAMPO_FECHA] = "La fecha no puede ser anterior a hoy"

        }
        if (e.ingeniero == null) errores[CAMPO_INGENIERO] = "Selecciona el ingeniero a cargo"
        if (e.participantes.isEmpty()) errores[CAMPO_PARTICIPANTES] = "Asigna al menos un técnico"
        return errores
    }
}