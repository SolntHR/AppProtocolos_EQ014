package com.example.appprotocolos_eq014.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formato {

    private val localeChile = Locale.forLanguageTag("es-CL")
    private val simbolos = DecimalFormatSymbols(localeChile).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    private fun formateador(patron: String) = SimpleDateFormat(patron, localeChile)

    fun numero(valor: Double, decimales: Int = 0): String {
        val patron = if (decimales == 0) "#,##0" else "#,##0." + "0".repeat(decimales)
        return DecimalFormat(patron, simbolos).format(valor)
    }

    fun numeroAutomatico(valor: Double): String =
        if (valor % 1.0 == 0.0) numero(valor) else numero(valor, 2)

    fun fecha(fechaBaseDatos: String): String = convertir(fechaBaseDatos.take(10), "yyyy-MM-dd", "dd-MM-yyyy")

    fun fechaHora(fechaBaseDatos: String): String =
        convertir(fechaBaseDatos.take(16), "yyyy-MM-dd HH:mm", "dd-MM-yyyy HH:mm")

    fun ahoraParaGuardar(): String = formateador("yyyy-MM-dd HH:mm").format(Date())

    fun ahoraParaMostrar(): String = formateador("dd-MM-yyyy HH:mm").format(Date())

    fun encabezadoHoy(): String = formateador("EEEE, d MMM yyyy").format(Date()).uppercase(localeChile)

    private fun convertir(valor: String, entrada: String, salida: String): String =
        runCatching { formateador(salida).format(formateador(entrada).parse(valor)!!) }.getOrDefault(valor)
}
