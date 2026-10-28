package com.example.appprotocolos_eq014.util

import com.example.appprotocolos_eq014.model.FormularioEquipoUiState
import com.example.appprotocolos_eq014.model.FormularioInstalacionUiState
import com.example.appprotocolos_eq014.model.ReglasProtocolo
import java.util.Calendar

// Validaciones centralizadas de los formularios de instalacion y equipo.
// Devuelven un mapa campo -> mensaje; vacio significa que el formulario es valido.
object ValidacionesCatalogo {

    const val CAMPO_INSTALACION = "instalacion"
    const val CAMPO_TIPO = "tipo"
    const val CAMPO_TAG = "tag"
    const val CAMPO_ANIO = "anio"
    const val CAMPO_POTENCIA = "potencia"
    const val CAMPO_TENSIONES = "tensiones"
    const val CAMPO_CORRIENTE = "corriente"
    const val CAMPO_CONTACTOS = "contactos"
    const val CAMPO_CLIENTE = "cliente"
    const val CAMPO_COMUNA = "comuna"
    const val CAMPO_NOMBRE = "nombre"

    // TAG del cliente: letras, numeros, guion y barra (ej.: TR-01, REL-50/51-A)
    private val regexTag = Regex("^[A-Z0-9][A-Z0-9/\\-]{1,19}$")

    fun normalizarTag(tag: String): String = tag.trim().uppercase().replace(" ", "")

    fun validarEquipo(e: FormularioEquipoUiState): Map<String, String> {
        val errores = mutableMapOf<String, String>()
        if (e.instalacion == null) errores[CAMPO_INSTALACION] = "Selecciona la instalación"
        if (e.tipo == null) errores[CAMPO_TIPO] = "Selecciona el tipo de equipo"

        val tag = normalizarTag(e.tag)
        when {
            tag.isEmpty() -> errores[CAMPO_TAG] = "Ingresa el TAG del equipo"
            !regexTag.matches(tag) -> errores[CAMPO_TAG] = "Usa letras, números y guiones (ej.: TR-01)"
        }

        if (e.anio.isNotBlank()) {
            val anio = e.anio.trim().toIntOrNull()
            val actual = Calendar.getInstance().get(Calendar.YEAR)
            if (anio == null || anio < 1950 || anio > actual) errores[CAMPO_ANIO] = "Año entre 1950 y $actual"
        }

        positivo(e.potenciaKva, e.pidePotencia, "Ingresa la potencia (kVA)")?.let { errores[CAMPO_POTENCIA] = it }
        if (e.pidePotencia && e.tensiones.isBlank()) {
            errores[CAMPO_TENSIONES] = "Ingresa las tensiones (ej.: 12 / 0,4 kV)"
        }
        positivo(e.corriente, false, "")?.let { errores[CAMPO_CORRIENTE] = it }
        // RN-08 necesita el maximo de placa de los contactos
        positivo(e.maxContactos, e.pideContactos, "Ingresa el máximo de contactos (µΩ)")?.let { errores[CAMPO_CONTACTOS] = it }
        return errores
    }

    fun validarInstalacion(i: FormularioInstalacionUiState): Map<String, String> {
        val errores = mutableMapOf<String, String>()
        if (i.cliente == null) errores[CAMPO_CLIENTE] = "Selecciona el cliente"
        if (i.comuna == null) errores[CAMPO_COMUNA] = "Selecciona la comuna"
        when {
            i.nombre.isBlank() -> errores[CAMPO_NOMBRE] = "Ingresa el nombre (ej.: Planta Lampa)"
            i.nombre.trim().length < 3 -> errores[CAMPO_NOMBRE] = "El nombre es muy corto"
        }
        return errores
    }

    // null = valido; obligatorio vacio = mensaje; valor <= 0 o no numerico = mensaje de rango
    private fun positivo(texto: String, obligatorio: Boolean, mensajeVacio: String): String? {
        if (texto.isBlank()) return if (obligatorio) mensajeVacio else null
        val n = ReglasProtocolo.numero(texto) ?: return "Debe ser un número"
        return if (n <= 0) "Debe ser mayor a 0" else null
    }
}
