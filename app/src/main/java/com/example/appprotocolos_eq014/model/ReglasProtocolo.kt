package com.example.appprotocolos_eq014.model

import kotlin.math.abs

// ---------------------------------------------------------------
// Catalogo de pruebas (RN-05) y reglas de resultado (RN-06 a RN-10).
// Kotlin puro: no depende de Android.
// Los idParametro coinciden con la tabla PARAMETRO del helper (version del equipo).
// idParametro = null -> valor calculado (IP, % error...): se muestra pero no se guarda.
// ---------------------------------------------------------------

data class CampoMedicion(
    val codigo: String,
    val etiqueta: String,
    val unidad: String,
    val idParametro: Long?
)

// idDb coincide con TIPO_PRUEBA.id_tipo_prueba
enum class TipoPrueba(val idDb: Long, val nombre: String) {
    AISLACION(1, "Resistencia de aislación"),
    TTR(2, "Relación de transformación (TTR)"),
    DEVANADOS(3, "Resistencia de devanados"),
    CONTACTOS(4, "Resistencia de contactos"),
    RELE(5, "Prueba de operación (inyección secundaria)");

    companion object {
        fun desdeId(id: Long): TipoPrueba? = entries.firstOrNull { it.idDb == id }
    }
}

object Parametros {
    // Guardados en MEDICION
    val L1 = CampoMedicion("LECTURA_1MIN", "Lectura 1 min", "MΩ", 1)
    val L10 = CampoMedicion("LECTURA_10MIN", "Lectura 10 min", "MΩ", 2)
    val KV = CampoMedicion("TENSION_PRUEBA", "Tensión de prueba", "kV", 3)
    val REL_TEO = CampoMedicion("RELACION_TEORICA", "Relación teórica", "-", 4)
    val REL_MED = CampoMedicion("RELACION_MEDIDA", "Relación medida", "-", 5)
    val TAP = CampoMedicion("POSICION_TAP", "Posición del cambiador", "-", 6)
    val R_FASE = CampoMedicion("RESISTENCIA_FASE", "Resistencia", "mΩ", 7)
    val R_POLO = CampoMedicion("RESISTENCIA_POLO", "Resistencia medida", "µΩ", 8)
    val I_AJUSTE = CampoMedicion("CORRIENTE_AJUSTE", "Corriente de ajuste", "A", 9)
    val I_MEDIDA = CampoMedicion("CORRIENTE_MEDIDA", "Corriente medida", "A", 10)
    val T_ESPERADO = CampoMedicion("TIEMPO_ESPERADO", "Tiempo esperado", "ms", 11)
    val T_MEDIDO = CampoMedicion("TIEMPO_MEDIDO", "Tiempo medido", "ms", 12)

    // Calculados (no se guardan)
    val IP = CampoMedicion("IP", "Índice de polarización", "-", null)
    val ERROR = CampoMedicion("ERROR_RELACION", "Error de relación", "%", null)
    val MAX_PLACA = CampoMedicion("MAX_PLACA", "Máximo de placa", "µΩ", null)
    val PCT_PLACA = CampoMedicion("PCT_PLACA", "% del máximo de placa", "%", null)
    val DIF_TIEMPO = CampoMedicion("DIF_TIEMPO", "Diferencia de tiempo", "%", null)

    val todos = listOf(
        L1, L10, KV, REL_TEO, REL_MED, TAP, R_FASE, R_POLO, I_AJUSTE, I_MEDIDA, T_ESPERADO, T_MEDIDO,
        IP, ERROR, MAX_PLACA, PCT_PLACA, DIF_TIEMPO
    )
    private val porCodigo = todos.associateBy { it.codigo }
    private val porId = todos.filter { it.idParametro != null }.associateBy { it.idParametro!! }

    fun porCodigo(codigo: String): CampoMedicion? = porCodigo[codigo]
    fun porId(id: Long): CampoMedicion? = porId[id]
}

// RN-10: el resultado global es el peor resultado de sus pruebas
fun peorResultado(resultados: List<ResultadoPrueba?>): ResultadoPrueba? =
    resultados.filterNotNull().maxByOrNull { it.idDb }

enum class EstadoFila { VACIA, INCOMPLETA, INVALIDA, COMPLETA }

data class Evaluacion(
    val estado: EstadoFila = EstadoFila.VACIA,
    val resultado: ResultadoPrueba? = null,
    val calculados: Map<String, Double> = emptyMap(), // incluye guardables y calculados
    val detalle: String? = null,                       // ej: "IP = 1,80"
    val error: String? = null,
    val comparaciones: List<Comparacion> = emptyList()
)

// Ultima medicion del mismo tipo y elemento en el mismo equipo (RN-09)
data class MedicionAnterior(
    val numeroProtocolo: String,
    val valores: Map<String, Double>
)

object ReglasProtocolo {

    const val TIPO_TRANSFORMADOR = 1L
    const val TIPO_INTERRUPTOR = 2L
    const val TIPO_CELDA = 3L
    const val TIPO_CABLE = 4L
    const val TIPO_RELE = 5L

    // RN-05: pruebas segun tipo de equipo (igual a TIPO_EQUIPO_PRUEBA)
    fun pruebasPara(tipoEquipoId: Long): List<TipoPrueba> = when (tipoEquipoId) {
        TIPO_TRANSFORMADOR -> listOf(TipoPrueba.AISLACION, TipoPrueba.TTR, TipoPrueba.DEVANADOS)
        TIPO_INTERRUPTOR, TIPO_CELDA -> listOf(TipoPrueba.AISLACION, TipoPrueba.CONTACTOS)
        TIPO_CABLE -> listOf(TipoPrueba.AISLACION)
        TIPO_RELE -> listOf(TipoPrueba.RELE)
        else -> emptyList()
    }

    // Elementos con los mismos nombres que usan los datos de ejemplo del helper
    fun elementosPara(tipo: TipoPrueba, tipoEquipoId: Long): List<String> = when (tipo) {
        TipoPrueba.AISLACION -> when (tipoEquipoId) {
            TIPO_TRANSFORMADOR -> listOf("AT-BT", "AT-T", "BT-T")
            TIPO_CABLE -> listOf("Fase R", "Fase S", "Fase T")
            else -> listOf("Polo 1", "Polo 2", "Polo 3")
        }
        TipoPrueba.TTR -> listOf("Fase H1-X1", "Fase H2-X2", "Fase H3-X3")
        TipoPrueba.DEVANADOS -> listOf("AT fase U", "AT fase V", "AT fase W", "BT fase U", "BT fase V", "BT fase W")
        TipoPrueba.CONTACTOS -> listOf("Polo 1", "Polo 2", "Polo 3")
        TipoPrueba.RELE -> listOf("Función 50/51")
    }

    // RN-06: la lectura a 10 min solo aplica a transformadores y cables
    fun usaLectura10(tipoEquipoId: Long) = tipoEquipoId == TIPO_TRANSFORMADOR || tipoEquipoId == TIPO_CABLE

    // Campos que el tecnico escribe en cada fila
    fun camposFila(tipo: TipoPrueba, tipoEquipoId: Long): List<CampoMedicion> = when (tipo) {
        TipoPrueba.AISLACION ->
            if (usaLectura10(tipoEquipoId)) listOf(Parametros.L1, Parametros.L10) else listOf(Parametros.L1)
        TipoPrueba.TTR -> listOf(Parametros.REL_TEO, Parametros.REL_MED)
        TipoPrueba.DEVANADOS -> listOf(Parametros.R_FASE)
        TipoPrueba.CONTACTOS -> listOf(Parametros.R_POLO)
        TipoPrueba.RELE -> listOf(Parametros.I_AJUSTE, Parametros.I_MEDIDA, Parametros.T_ESPERADO, Parametros.T_MEDIDO)
    }

    // Campos que realmente mide el tecnico (los demas pueden venir pre-cargados)
    fun camposMedidos(tipo: TipoPrueba, tipoEquipoId: Long): List<CampoMedicion> = when (tipo) {
        TipoPrueba.TTR -> listOf(Parametros.REL_MED)
        TipoPrueba.RELE -> listOf(Parametros.I_MEDIDA, Parametros.T_MEDIDO)
        else -> camposFila(tipo, tipoEquipoId)
    }

    // Valores principales que se comparan con la prueba anterior (RN-09)
    fun valoresPrincipales(tipo: TipoPrueba): List<CampoMedicion> = when (tipo) {
        TipoPrueba.AISLACION -> listOf(Parametros.L1, Parametros.L10)
        TipoPrueba.TTR -> listOf(Parametros.REL_MED)
        TipoPrueba.DEVANADOS -> listOf(Parametros.R_FASE)
        TipoPrueba.CONTACTOS -> listOf(Parametros.R_POLO)
        TipoPrueba.RELE -> listOf(Parametros.T_MEDIDO)
    }

    // Instrumento sugerido (filtra por nombre_tipo del INSTRUMENTO)
    fun palabraInstrumento(tipo: TipoPrueba): String = when (tipo) {
        TipoPrueba.AISLACION -> "megóhmetro"
        TipoPrueba.TTR -> "ttr"
        TipoPrueba.DEVANADOS, TipoPrueba.CONTACTOS -> "micro-ohm"
        TipoPrueba.RELE -> "inyección"
    }

    // "12 / 0,4 kV" -> 30.0 (si la placa no trae ese formato, el tecnico la escribe)
    fun relacionTeorica(tensiones: String?): Double? {
        if (tensiones.isNullOrBlank()) return null
        val partes = tensiones.lowercase().replace("kv", "").split("/")
        if (partes.size < 2) return null
        val at = numero(partes[0]) ?: return null
        val bt = numero(partes[1]) ?: return null
        return if (bt > 0) at / bt else null
    }

    fun numero(texto: String?): Double? {
        val limpio = texto?.trim()?.replace(" ", "")?.replace(",", ".") ?: return null
        if (limpio.isEmpty()) return null
        val valor = limpio.toDoubleOrNull() ?: return null
        return if (valor.isFinite()) valor else null
    }

    /**
     * Evalua una fila: valida rangos (RNF-05 / HU-03), calcula IP, % de error, etc.
     * y entrega el resultado segun RN-06, RN-07, RN-08, RN-08b y RN-08c.
     */
    fun evaluar(
        tipo: TipoPrueba,
        tipoEquipoId: Long,
        valores: Map<String, String>,
        comunes: Map<String, String>,
        unidadAislacion: String,
        opera: Boolean,
        resultadoManual: ResultadoPrueba?,
        anterior: MedicionAnterior?
    ): Evaluacion {
        val todos = camposFila(tipo, tipoEquipoId)
        val medidos = camposMedidos(tipo, tipoEquipoId)
        val sinMediciones = medidos.all { valores[it.codigo].isNullOrBlank() }
        if (sinMediciones && !(tipo == TipoPrueba.RELE && !opera)) return Evaluacion()

        val requeridos = if (tipo == TipoPrueba.RELE && !opera)
            listOf(Parametros.I_AJUSTE, Parametros.T_ESPERADO)
        else todos
        if (requeridos.any { valores[it.codigo].isNullOrBlank() }) {
            return Evaluacion(EstadoFila.INCOMPLETA, error = "Completa todos los valores de la fila")
        }

        val v = mutableMapOf<String, Double>()
        for (campo in todos) {
            val texto = valores[campo.codigo]
            if (texto.isNullOrBlank()) continue
            val n = numero(texto)
                ?: return Evaluacion(EstadoFila.INVALIDA, error = "${campo.etiqueta}: no es un número válido")
            if (n < 0) return Evaluacion(EstadoFila.INVALIDA, error = "${campo.etiqueta}: no se aceptan valores negativos")
            v[campo.codigo] = n
        }

        val base = when (tipo) {
            TipoPrueba.AISLACION -> evaluarAislacion(tipoEquipoId, v, comunes, unidadAislacion)
            TipoPrueba.TTR -> evaluarTtr(v, comunes)
            TipoPrueba.DEVANADOS -> evaluarDevanados(v, resultadoManual)
            TipoPrueba.CONTACTOS -> evaluarContactos(v, comunes)
            TipoPrueba.RELE -> evaluarRele(v, opera)
        }
        if (base.estado != EstadoFila.COMPLETA || anterior == null) return base
        return base.copy(comparaciones = comparar(tipo, base.calculados, anterior))
    }

    fun comparar(tipo: TipoPrueba, actuales: Map<String, Double>, anterior: MedicionAnterior): List<Comparacion> =
        valoresPrincipales(tipo).mapNotNull { campo ->
            val actual = actuales[campo.codigo] ?: return@mapNotNull null
            val previo = anterior.valores[campo.codigo] ?: return@mapNotNull null
            Comparacion(campo.codigo, campo.etiqueta, previo, actual, campo.unidad, anterior.numeroProtocolo)
        }

    // Texto con el valor calculado para mostrar en el detalle (los calculados no se guardan)
    fun resumenCalculo(tipo: TipoPrueba, valores: Map<String, Double>, maxPlaca: Double?): String? = when (tipo) {
        TipoPrueba.AISLACION -> {
            val l1 = valores[Parametros.L1.codigo]
            val l10 = valores[Parametros.L10.codigo]
            if (l1 != null && l10 != null && l1 > 0) "IP = ${(l10 / l1).formatoCl(2)}" else null
        }
        TipoPrueba.TTR -> {
            val teo = valores[Parametros.REL_TEO.codigo]
            val med = valores[Parametros.REL_MED.codigo]
            if (teo != null && med != null && teo > 0) "Error = ${(abs(med - teo) / teo * 100).formatoCl(2)} %" else null
        }
        TipoPrueba.CONTACTOS -> {
            val r = valores[Parametros.R_POLO.codigo]
            if (r != null && maxPlaca != null && maxPlaca > 0) "${(r / maxPlaca * 100).formatoCl(1)} % del máximo de placa" else null
        }
        TipoPrueba.RELE -> {
            val te = valores[Parametros.T_ESPERADO.codigo]
            val tm = valores[Parametros.T_MEDIDO.codigo]
            if (te != null && tm != null && te > 0) "Diferencia = ${(abs(tm - te) / te * 100).formatoCl(1)} %"
            else if (te != null && tm == null) "El relé no operó" else null
        }
        TipoPrueba.DEVANADOS -> null
    }

    private fun invalida(mensaje: String) = Evaluacion(EstadoFila.INVALIDA, error = mensaje)

    // RN-06: IP = L10 / L1. L1 < 100 MΩ -> Rechazado
    private fun evaluarAislacion(
        tipoEquipoId: Long,
        v: Map<String, Double>,
        comunes: Map<String, String>,
        unidad: String
    ): Evaluacion {
        val kv = numero(comunes[Parametros.KV.codigo])
            ?: return Evaluacion(EstadoFila.INCOMPLETA, error = "Indica la tensión de prueba (kV)")
        if (kv < 0.25 || kv > 15) return invalida("Tensión de prueba fuera de rango (0,25 a 15 kV)")

        val factor = if (unidad == "GΩ") 1000.0 else 1.0
        val l1 = v.getValue(Parametros.L1.codigo) * factor
        if (l1 > 10_000_000) return invalida("Lectura 1 min fuera de rango, revisa la unidad")
        val calculados = mutableMapOf(Parametros.L1.codigo to l1, Parametros.KV.codigo to kv)

        if (!usaLectura10(tipoEquipoId)) {
            val r = if (l1 < 100) ResultadoPrueba.RECHAZADO else ResultadoPrueba.APROBADO
            return Evaluacion(
                EstadoFila.COMPLETA, r, calculados,
                detalle = if (l1 < 100) "Lectura 1 min < 100 MΩ" else "Lectura 1 min ≥ 100 MΩ"
            )
        }

        val l10 = v.getValue(Parametros.L10.codigo) * factor
        if (l10 > 10_000_000) return invalida("Lectura 10 min fuera de rango, revisa la unidad")
        calculados[Parametros.L10.codigo] = l10
        if (l1 <= 0.0) {
            return Evaluacion(EstadoFila.COMPLETA, ResultadoPrueba.RECHAZADO, calculados, "Lectura 1 min < 100 MΩ")
        }
        val ip = l10 / l1
        calculados[Parametros.IP.codigo] = ip
        val resultado = when {
            l1 < 100 -> ResultadoPrueba.RECHAZADO
            ip < 1.0 -> ResultadoPrueba.RECHAZADO
            ip < 2.0 -> ResultadoPrueba.OBSERVADO
            else -> ResultadoPrueba.APROBADO
        }
        val detalle = "IP = ${ip.formatoCl(2)}" + if (l1 < 100) " · Lectura 1 min < 100 MΩ" else ""
        return Evaluacion(EstadoFila.COMPLETA, resultado, calculados, detalle)
    }

    // RN-07: error = |medida - teorica| / teorica x 100
    private fun evaluarTtr(v: Map<String, Double>, comunes: Map<String, String>): Evaluacion {
        val teo = v.getValue(Parametros.REL_TEO.codigo)
        val med = v.getValue(Parametros.REL_MED.codigo)
        if (teo <= 0.0) return invalida("La relación teórica debe ser mayor a 0")
        if (med <= 0.0) return invalida("La relación medida debe ser mayor a 0")
        val tapTexto = comunes[Parametros.TAP.codigo].orEmpty()
        val tap = numero(tapTexto)
        if (tapTexto.isNotBlank() && tap == null) return invalida("Posición del cambiador: debe ser un número")

        val error = abs(med - teo) / teo * 100.0
        val resultado = when {
            error <= 0.5 -> ResultadoPrueba.APROBADO
            error <= 1.0 -> ResultadoPrueba.OBSERVADO
            else -> ResultadoPrueba.RECHAZADO
        }
        val calculados = mutableMapOf(
            Parametros.REL_TEO.codigo to teo,
            Parametros.REL_MED.codigo to med,
            Parametros.ERROR.codigo to error
        )
        if (tap != null) calculados[Parametros.TAP.codigo] = tap
        return Evaluacion(EstadoFila.COMPLETA, resultado, calculados, "Error = ${error.formatoCl(2)} %")
    }

    // RN-08c: sin criterio automatico, el ingeniero indica el resultado
    private fun evaluarDevanados(v: Map<String, Double>, manual: ResultadoPrueba?): Evaluacion {
        if (v.values.any { it > 100_000 }) return invalida("Valor fuera de rango, revisa la unidad (mΩ)")
        return Evaluacion(
            EstadoFila.COMPLETA, manual, v,
            detalle = if (manual == null) "Resultado pendiente: lo indica el ingeniero" else "Resultado indicado manualmente"
        )
    }

    // RN-08: se compara con el valor maximo de placa
    private fun evaluarContactos(v: Map<String, Double>, comunes: Map<String, String>): Evaluacion {
        val max = numero(comunes[Parametros.MAX_PLACA.codigo])
            ?: return Evaluacion(EstadoFila.INCOMPLETA, error = "Indica el máximo de placa (µΩ)")
        if (max <= 0.0) return invalida("El máximo de placa debe ser mayor a 0")
        val r = v.getValue(Parametros.R_POLO.codigo)
        if (r > 100_000) return invalida("Valor fuera de rango, revisa la unidad (µΩ)")
        val pct = r / max * 100.0
        val resultado = when {
            pct <= 100.0 -> ResultadoPrueba.APROBADO
            pct <= 120.0 -> ResultadoPrueba.OBSERVADO
            else -> ResultadoPrueba.RECHAZADO
        }
        return Evaluacion(
            EstadoFila.COMPLETA, resultado,
            mapOf(Parametros.R_POLO.codigo to r, Parametros.MAX_PLACA.codigo to max, Parametros.PCT_PLACA.codigo to pct),
            "${pct.formatoCl(1)} % del máximo de placa"
        )
    }

    // RN-08b: diferencia de tiempo; si no opera -> Rechazado
    private fun evaluarRele(v: Map<String, Double>, opera: Boolean): Evaluacion {
        val ajuste = v.getValue(Parametros.I_AJUSTE.codigo)
        val esperado = v.getValue(Parametros.T_ESPERADO.codigo)
        if (ajuste <= 0.0) return invalida("La corriente de ajuste debe ser mayor a 0")
        if (esperado <= 0.0) return invalida("El tiempo esperado debe ser mayor a 0")
        if (!opera) {
            // No se guarda tiempo medido: asi queda registrado que no opero
            val sinTiempo = v.filterKeys { it != Parametros.T_MEDIDO.codigo }
            return Evaluacion(EstadoFila.COMPLETA, ResultadoPrueba.RECHAZADO, sinTiempo, "El relé no operó")
        }
        val medido = v.getValue(Parametros.T_MEDIDO.codigo)
        if (medido <= 0.0) return invalida("El tiempo medido debe ser mayor a 0")
        val dif = abs(medido - esperado) / esperado * 100.0
        val calculados = v.toMutableMap()
        calculados[Parametros.DIF_TIEMPO.codigo] = dif
        val resultado = when {
            dif <= 5.0 -> ResultadoPrueba.APROBADO
            dif <= 10.0 -> ResultadoPrueba.OBSERVADO
            else -> ResultadoPrueba.RECHAZADO
        }
        return Evaluacion(EstadoFila.COMPLETA, resultado, calculados, "Diferencia = ${dif.formatoCl(1)} %")
    }
}
