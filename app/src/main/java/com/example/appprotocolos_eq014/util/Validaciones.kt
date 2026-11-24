package com.example.appprotocolos_eq014.util

object Validaciones {

    private val regexCorreo = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    fun rutValido(rut: String): Boolean {
        val limpio = rut.replace(".", "").replace("-", "").trim().uppercase()
        if (limpio.length < 2) return false
        val cuerpo = limpio.dropLast(1)
        val digitoIngresado = limpio.last()
        if (!cuerpo.all { it.isDigit() }) return false

        var suma = 0
        var multiplicador = 2
        for (digito in cuerpo.reversed()) {
            suma += digito.digitToInt() * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }
        val digitoEsperado = when (val resto = 11 - (suma % 11)) {
            11 -> '0'
            10 -> 'K'
            else -> resto.digitToChar()
        }
        return digitoIngresado == digitoEsperado
    }

    fun correoValido(correo: String): Boolean = regexCorreo.matches(correo.trim())


    fun formatearRut(rut: String): String {
        val limpio = rut.replace(".", "").replace("-", "").trim().uppercase()
        if (limpio.length < 2) return rut.trim()
        val cuerpo = limpio.dropLast(1).reversed().chunked(3).joinToString(".").reversed()
        return "$cuerpo-${limpio.last()}"
    }

    fun errorNombre(nombre: String): String? =
        if (nombre.isBlank()) "El nombre es obligatorio" else null

    fun errorRut(rut: String): String? = when {
        rut.isBlank() -> "Ingresa el RUT"
        !rutValido(rut) -> "RUT inválido: el dígito verificador no coincide"
        else -> null
    }

    fun normalizarTelefono(telefono: String): String {
        val digitos = telefono.replace(Regex("[\\s()+.-]"), "")
        return if (digitos.length == 11 && digitos.startsWith("56")) digitos.drop(2) else digitos
    }

    fun telefonoValido(telefono: String): Boolean = Regex("^9\\d{8}$").matches(normalizarTelefono(telefono))

    fun telefonoCompleto(telefono: String): String {
        val numero = normalizarTelefono(telefono)
        return if (telefonoValido(numero)) "+56 ${numero.take(1)} ${numero.substring(1, 5)} ${numero.takeLast(4)}" else telefono.trim()
    }

    fun errorTelefono(telefono: String): String? = when {
    telefono.isBlank() -> "Ingresa tu teléfono"
    !telefonoValido(telefono) -> "Teléfono inválido. Ej: 912345678"
    else -> null
    }

    fun errorCorreo(correo: String): String? = when {
        correo.isBlank() -> "Ingresa tu correo electrónico"
        !correoValido(correo) -> "Correo inválido. Ejemplo: nombre@empresa.cl"
        else -> null
    }

    fun errorContrasena(contrasena: String): String? {
        if (contrasena.isEmpty()) return "Ingresa tu contraseña"
        if (contrasena.any { it.isWhitespace() }) return "La contraseña no puede tener espacios"
        val faltantes = buildList {
            if (contrasena.length < LARGO_MINIMO_CONTRASENA) add("$LARGO_MINIMO_CONTRASENA caracteres")
            if (contrasena.none { it.isUpperCase() }) add("una mayúscula")
            if (contrasena.none { it.isLowerCase() }) add("una minúscula")
            if (contrasena.none { it.isDigit() }) add("un número")
            if (contrasena.none { !it.isLetterOrDigit() }) add("un carácter especial")
        }
        return if (faltantes.isEmpty()) null else "Debe tener al menos: " + faltantes.joinToString(", ")
    }

    private const val LARGO_MINIMO_CONTRASENA = 8

    fun errorObligatorio(valor: String, mensaje: String): String? =
        if (valor.isBlank()) mensaje else null
}
