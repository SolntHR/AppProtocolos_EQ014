package com.example.appprotocolos_eq014.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Utilidades de fecha del modulo de protocolos (minSdk 24: SimpleDateFormat en vez de java.time)
object Fechas {
    private val localeCl = Locale.forLanguageTag("es-CL")

    fun ahora(): String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
    fun hoy(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    // Acepta "yyyy-MM-dd", "yyyy-MM-dd HH:mm" y "yyyy-MM-dd HH:mm:ss" (el helper usa los tres)
    private fun parsear(iso: String): Date? {
        val patrones = when {
            iso.length >= 19 -> listOf("yyyy-MM-dd HH:mm:ss")
            iso.length >= 16 -> listOf("yyyy-MM-dd HH:mm")
            else -> listOf("yyyy-MM-dd")
        }
        for (p in patrones) {
            try {
                return SimpleDateFormat(p, Locale.US).parse(iso)
            } catch (e: Exception) {
                // se prueba el siguiente
            }
        }
        return null
    }

    // yyyy-MM-dd -> dd-MM-yyyy
    fun fecha(iso: String?): String {
        if (iso.isNullOrBlank()) return "-"
        val d = parsear(iso) ?: return iso
        return SimpleDateFormat("dd-MM-yyyy", Locale.US).format(d)
    }

    // yyyy-MM-dd HH:mm(:ss) -> dd-MM-yyyy HH:mm
    fun fechaHora(iso: String?): String {
        if (iso.isNullOrBlank()) return "-"
        val d = parsear(iso) ?: return iso
        return SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US).format(d)
    }

    // RN-16: dias habiles (lunes a viernes) entre la fecha dada y hoy
    fun diasHabilesDesde(iso: String): Int {
        val inicio = parsear(iso) ?: return 0
        val cal = Calendar.getInstance().apply { time = inicio }
        val hoy = Calendar.getInstance()
        var dias = 0
        while (cal.before(hoy)) {
            cal.add(Calendar.DAY_OF_MONTH, 1)
            if (cal.after(hoy)) break
            val dia = cal.get(Calendar.DAY_OF_WEEK)
            if (dia != Calendar.SATURDAY && dia != Calendar.SUNDAY) dias++
        }
        return dias
    }

    internal val locale: Locale get() = localeCl
}

// 12500.0 -> "12.500", 1.8 -> "1,8"
fun Double.formatoCl(decimales: Int = 2): String {
    val texto = String.format(Fechas.locale, "%,.${decimales}f", this)
    return if (texto.contains(',')) texto.trimEnd('0').trimEnd(',') else texto
}
