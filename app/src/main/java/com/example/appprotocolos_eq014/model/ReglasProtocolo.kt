package com.example.appprotocolos_eq014.model

import kotlin.math.abs

data class CampoMedicion(
    val codigo: String,
    val etiqueta: String,
    val unidad: String,
    val idParametro: Long?
)

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

fun peorResultado(resultados: List<ResultadoPrueba?>): ResultadoPrueba? =
    resultados.filterNotNull().maxByOrNull { it.idDb }

enum class EstadoFila { VACIA, INCOMPLETA, INVALIDA, COMPLETA }

data class Evaluacion(
    val estado: EstadoFila = EstadoFila.VACIA,
    val resultado: ResultadoPrueba? = null,
    val calculados: Map<String, Double> = emptyMap(),
    val detalle: String? = null,
    val error: String? = null,
    val comparaciones: List<Comparacion> = emptyList(),
    val camposConError: Set<String> = emptySet()
)

data class MedicionAnterior(
    val numeroProtocolo: String,
    val valores: Map<String, Double>
)

sealed class ReglaPrueba(val tipo: TipoPrueba) {
    abstract fun elementos(tipoEquipoId: Long): List<String>

    abstract fun camposFila(tipoEquipoId: Long): List<CampoMedicion>

    open fun camposMedidos(tipoEquipoId: Long): List<CampoMedicion> = camposFila(tipoEquipoId)

    open fun camposRequeridos(tipoEquipoId: Long, opera: Boolean): List<CampoMedicion> = camposFila(tipoEquipoId)

    open fun estaVacia(valores: Map<String, String>, tipoEquipoId: Long, opera: Boolean): Boolean =
        camposMedidos(tipoEquipoId).all { valores[it.codigo].isNullOrBlank() }

    abstract val valoresPrincipales: List<CampoMedicion>

    abstract val palabraInstrumento: String

    protected abstract fun calcular(
        tipoEquipoId: Long,
        v: Map<String, Double>,
        comunes: Map<String, String>,
        unidadAislacion: String,
        opera: Boolean,
        resultadoManual: ResultadoPrueba?
    ): Evaluacion

    open fun resumenCalculo(valores: Map<String, Double>, maxPlaca: Double?): String? = null

    fun evaluar(
        tipoEquipoId: Long,
        valores: Map<String, String>,
        comunes: Map<String, String>,
        unidadAislacion: String,
        opera: Boolean,
        resultadoManual: ResultadoPrueba?,
        anterior: MedicionAnterior?
    ): Evaluacion {
        if (estaVacia(valores, tipoEquipoId, opera)) return Evaluacion()

        val faltantes = camposRequeridos(tipoEquipoId, opera).filter { valores[it.codigo].isNullOrBlank() }
        if (faltantes.isNotEmpty()) {
            return Evaluacion(
                EstadoFila.INCOMPLETA,
                error = "Falta: ${faltantes.joinToString(", ") { it.etiqueta }}",
                camposConError = faltantes.map { it.codigo }.toSet()
            )
        }

        val v = mutableMapOf<String, Double>()
        for (campo in camposFila(tipoEquipoId)) {
            val texto = valores[campo.codigo]
            if (texto.isNullOrBlank()) continue
            val n = ReglasProtocolo.numero(texto)
                ?: return invalida("${campo.etiqueta}: no es un número válido", campo)
            if (n < 0) return invalida("${campo.etiqueta}: no se aceptan valores negativos", campo)
            v[campo.codigo] = n
        }

        val base = calcular(tipoEquipoId, v, comunes, unidadAislacion, opera, resultadoManual)
        if (base.estado != EstadoFila.COMPLETA || anterior == null) return base
        return base.copy(comparaciones = comparar(base.calculados, anterior))
    }

    fun comparar(actuales: Map<String, Double>, anterior: MedicionAnterior): List<Comparacion> =
        valoresPrincipales.mapNotNull { campo ->
            val actual = actuales[campo.codigo] ?: return@mapNotNull null
            val previo = anterior.valores[campo.codigo] ?: return@mapNotNull null
            Comparacion(campo.codigo, campo.etiqueta, previo, actual, campo.unidad, anterior.numeroProtocolo)
        }

    protected fun invalida(mensaje: String, campo: CampoMedicion? = null) =
        Evaluacion(EstadoFila.INVALIDA, error = mensaje, camposConError = setOfNotNull(campo?.codigo))

    object Aislacion : ReglaPrueba(TipoPrueba.AISLACION) {
        override fun elementos(tipoEquipoId: Long) = when (tipoEquipoId) {
            ReglasProtocolo.TIPO_TRANSFORMADOR -> listOf("AT-BT", "AT-T", "BT-T")
            ReglasProtocolo.TIPO_CABLE -> listOf("Fase R", "Fase S", "Fase T")
            else -> listOf("Polo 1", "Polo 2", "Polo 3")
        }

        override fun camposFila(tipoEquipoId: Long) =
            if (ReglasProtocolo.usaLectura10(tipoEquipoId)) listOf(Parametros.L1, Parametros.L10) else listOf(Parametros.L1)

        override val valoresPrincipales = listOf(Parametros.L1, Parametros.L10)
        override val palabraInstrumento = "megóhmetro"

        override fun calcular(
            tipoEquipoId: Long, v: Map<String, Double>, comunes: Map<String, String>,
            unidadAislacion: String, opera: Boolean, resultadoManual: ResultadoPrueba?
        ): Evaluacion {
            val kv = ReglasProtocolo.numero(comunes[Parametros.KV.codigo])
                ?: return Evaluacion(EstadoFila.INCOMPLETA, error = "Indica la tensión de prueba (kV)")
            if (kv < 0.25 || kv > 15) return invalida("Tensión de prueba fuera de rango (0,25 a 15 kV)")

            val factor = if (unidadAislacion == "GΩ") 1000.0 else 1.0
            val l1 = v.getValue(Parametros.L1.codigo) * factor
            if (l1 > 10_000_000) return invalida("Lectura 1 min fuera de rango, revisa la unidad", Parametros.L1)
            val calculados = mutableMapOf(Parametros.L1.codigo to l1, Parametros.KV.codigo to kv)

            if (!ReglasProtocolo.usaLectura10(tipoEquipoId)) {
                val r = if (l1 < 100) ResultadoPrueba.RECHAZADO else ResultadoPrueba.APROBADO
                return Evaluacion(
                    EstadoFila.COMPLETA, r, calculados,
                    detalle = if (l1 < 100) "Lectura 1 min < 100 MΩ" else "Lectura 1 min ≥ 100 MΩ"
                )
            }

            val l10 = v.getValue(Parametros.L10.codigo) * factor
            if (l10 > 10_000_000) return invalida("Lectura 10 min fuera de rango, revisa la unidad", Parametros.L10)
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

        override fun resumenCalculo(valores: Map<String, Double>, maxPlaca: Double?): String? {
            val l1 = valores[Parametros.L1.codigo]
            val l10 = valores[Parametros.L10.codigo]
            return if (l1 != null && l10 != null && l1 > 0) "IP = ${(l10 / l1).formatoCl(2)}" else null
        }
    }

    object Ttr : ReglaPrueba(TipoPrueba.TTR) {
        override fun elementos(tipoEquipoId: Long) = listOf("Fase H1-X1", "Fase H2-X2", "Fase H3-X3")
        override fun camposFila(tipoEquipoId: Long) = listOf(Parametros.REL_TEO, Parametros.REL_MED)
        override fun camposMedidos(tipoEquipoId: Long) = listOf(Parametros.REL_MED)
        override val valoresPrincipales = listOf(Parametros.REL_MED)
        override val palabraInstrumento = "ttr"

        override fun calcular(
            tipoEquipoId: Long, v: Map<String, Double>, comunes: Map<String, String>,
            unidadAislacion: String, opera: Boolean, resultadoManual: ResultadoPrueba?
        ): Evaluacion {
            val teo = v.getValue(Parametros.REL_TEO.codigo)
            val med = v.getValue(Parametros.REL_MED.codigo)
            if (teo <= 0.0) return invalida("La relación teórica debe ser mayor a 0", Parametros.REL_TEO)
            if (med <= 0.0) return invalida("La relación medida debe ser mayor a 0", Parametros.REL_MED)
            val tapTexto = comunes[Parametros.TAP.codigo].orEmpty()
            val tap = ReglasProtocolo.numero(tapTexto)
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

        override fun resumenCalculo(valores: Map<String, Double>, maxPlaca: Double?): String? {
            val teo = valores[Parametros.REL_TEO.codigo]
            val med = valores[Parametros.REL_MED.codigo]
            return if (teo != null && med != null && teo > 0) "Error = ${(abs(med - teo) / teo * 100).formatoCl(2)} %" else null
        }
    }

    object Devanados : ReglaPrueba(TipoPrueba.DEVANADOS) {
        override fun elementos(tipoEquipoId: Long) =
            listOf("AT fase U", "AT fase V", "AT fase W", "BT fase U", "BT fase V", "BT fase W")

        override fun camposFila(tipoEquipoId: Long) = listOf(Parametros.R_FASE)
        override val valoresPrincipales = listOf(Parametros.R_FASE)
        override val palabraInstrumento = "micro-ohm"

        override fun calcular(
            tipoEquipoId: Long, v: Map<String, Double>, comunes: Map<String, String>,
            unidadAislacion: String, opera: Boolean, resultadoManual: ResultadoPrueba?
        ): Evaluacion {
            if (v.values.any { it > 100_000 }) return invalida("Valor fuera de rango, revisa la unidad (mΩ)", Parametros.R_FASE)
            return Evaluacion(
                EstadoFila.COMPLETA, resultadoManual, v,
                detalle = if (resultadoManual == null) "Resultado pendiente: lo indica el ingeniero" else "Resultado indicado manualmente"
            )
        }
    }

    object Contactos : ReglaPrueba(TipoPrueba.CONTACTOS) {
        override fun elementos(tipoEquipoId: Long) = listOf("Polo 1", "Polo 2", "Polo 3")
        override fun camposFila(tipoEquipoId: Long) = listOf(Parametros.R_POLO)
        override val valoresPrincipales = listOf(Parametros.R_POLO)
        override val palabraInstrumento = "micro-ohm"

        override fun calcular(
            tipoEquipoId: Long, v: Map<String, Double>, comunes: Map<String, String>,
            unidadAislacion: String, opera: Boolean, resultadoManual: ResultadoPrueba?
        ): Evaluacion {
            val max = ReglasProtocolo.numero(comunes[Parametros.MAX_PLACA.codigo])
                ?: return Evaluacion(EstadoFila.INCOMPLETA, error = "Indica el máximo de placa (µΩ)")
            if (max <= 0.0) return invalida("El máximo de placa debe ser mayor a 0")
            val r = v.getValue(Parametros.R_POLO.codigo)
            if (r > 100_000) return invalida("Valor fuera de rango, revisa la unidad (µΩ)", Parametros.R_POLO)
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

        override fun resumenCalculo(valores: Map<String, Double>, maxPlaca: Double?): String? {
            val r = valores[Parametros.R_POLO.codigo]
            return if (r != null && maxPlaca != null && maxPlaca > 0) "${(r / maxPlaca * 100).formatoCl(1)} % del máximo de placa" else null
        }
    }

    object Rele : ReglaPrueba(TipoPrueba.RELE) {
        override fun elementos(tipoEquipoId: Long) = listOf("Función 50/51")
        override fun camposFila(tipoEquipoId: Long) =
            listOf(Parametros.I_AJUSTE, Parametros.I_MEDIDA, Parametros.T_ESPERADO, Parametros.T_MEDIDO)

        override fun camposMedidos(tipoEquipoId: Long) = listOf(Parametros.I_MEDIDA, Parametros.T_MEDIDO)

        override fun camposRequeridos(tipoEquipoId: Long, opera: Boolean) =
            if (opera) camposFila(tipoEquipoId) else listOf(Parametros.I_AJUSTE, Parametros.T_ESPERADO)

        override fun estaVacia(valores: Map<String, String>, tipoEquipoId: Long, opera: Boolean) =
            opera && super.estaVacia(valores, tipoEquipoId, opera)

        override val valoresPrincipales = listOf(Parametros.T_MEDIDO)
        override val palabraInstrumento = "inyección"

        override fun calcular(
            tipoEquipoId: Long, v: Map<String, Double>, comunes: Map<String, String>,
            unidadAislacion: String, opera: Boolean, resultadoManual: ResultadoPrueba?
        ): Evaluacion {
            val ajuste = v.getValue(Parametros.I_AJUSTE.codigo)
            val esperado = v.getValue(Parametros.T_ESPERADO.codigo)
            if (ajuste <= 0.0) return invalida("La corriente de ajuste debe ser mayor a 0", Parametros.I_AJUSTE)
            if (esperado <= 0.0) return invalida("El tiempo esperado debe ser mayor a 0", Parametros.T_ESPERADO)
            if (!opera) {
                val sinTiempo = v.filterKeys { it != Parametros.T_MEDIDO.codigo }
                return Evaluacion(EstadoFila.COMPLETA, ResultadoPrueba.RECHAZADO, sinTiempo, "El relé no operó")
            }
            val medido = v.getValue(Parametros.T_MEDIDO.codigo)
            if (medido <= 0.0) return invalida("El tiempo medido debe ser mayor a 0", Parametros.T_MEDIDO)
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

        override fun resumenCalculo(valores: Map<String, Double>, maxPlaca: Double?): String? {
            val te = valores[Parametros.T_ESPERADO.codigo]
            val tm = valores[Parametros.T_MEDIDO.codigo]
            return when {
                te != null && tm != null && te > 0 -> "Diferencia = ${(abs(tm - te) / te * 100).formatoCl(1)} %"
                te != null && tm == null -> "El relé no operó"
                else -> null
            }
        }
    }

    companion object {
        fun para(tipo: TipoPrueba): ReglaPrueba = when (tipo) {
            TipoPrueba.AISLACION -> Aislacion
            TipoPrueba.TTR -> Ttr
            TipoPrueba.DEVANADOS -> Devanados
            TipoPrueba.CONTACTOS -> Contactos
            TipoPrueba.RELE -> Rele
        }
    }
}

object ReglasProtocolo {
    const val TIPO_TRANSFORMADOR = 1L
    const val TIPO_INTERRUPTOR = 2L
    const val TIPO_CELDA = 3L
    const val TIPO_CABLE = 4L
    const val TIPO_RELE = 5L

    fun pruebasPara(tipoEquipoId: Long): List<TipoPrueba> = when (tipoEquipoId) {
        TIPO_TRANSFORMADOR -> listOf(TipoPrueba.AISLACION, TipoPrueba.TTR, TipoPrueba.DEVANADOS)
        TIPO_INTERRUPTOR, TIPO_CELDA -> listOf(TipoPrueba.AISLACION, TipoPrueba.CONTACTOS)
        TIPO_CABLE -> listOf(TipoPrueba.AISLACION)
        TIPO_RELE -> listOf(TipoPrueba.RELE)
        else -> emptyList()
    }

    fun usaLectura10(tipoEquipoId: Long) = tipoEquipoId == TIPO_TRANSFORMADOR || tipoEquipoId == TIPO_CABLE

    fun elementosPara(tipo: TipoPrueba, tipoEquipoId: Long) = ReglaPrueba.para(tipo).elementos(tipoEquipoId)
    fun camposFila(tipo: TipoPrueba, tipoEquipoId: Long) = ReglaPrueba.para(tipo).camposFila(tipoEquipoId)
    fun camposMedidos(tipo: TipoPrueba, tipoEquipoId: Long) = ReglaPrueba.para(tipo).camposMedidos(tipoEquipoId)
    fun valoresPrincipales(tipo: TipoPrueba) = ReglaPrueba.para(tipo).valoresPrincipales
    fun palabraInstrumento(tipo: TipoPrueba) = ReglaPrueba.para(tipo).palabraInstrumento

    fun evaluar(
        tipo: TipoPrueba,
        tipoEquipoId: Long,
        valores: Map<String, String>,
        comunes: Map<String, String>,
        unidadAislacion: String,
        opera: Boolean,
        resultadoManual: ResultadoPrueba?,
        anterior: MedicionAnterior?
    ): Evaluacion = ReglaPrueba.para(tipo)
        .evaluar(tipoEquipoId, valores, comunes, unidadAislacion, opera, resultadoManual, anterior)

    fun comparar(tipo: TipoPrueba, actuales: Map<String, Double>, anterior: MedicionAnterior): List<Comparacion> =
        ReglaPrueba.para(tipo).comparar(actuales, anterior)

    fun resumenCalculo(tipo: TipoPrueba, valores: Map<String, Double>, maxPlaca: Double?): String? =
        ReglaPrueba.para(tipo).resumenCalculo(valores, maxPlaca)

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
}
