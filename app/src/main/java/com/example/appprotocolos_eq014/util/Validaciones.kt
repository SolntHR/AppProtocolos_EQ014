package com.example.appprotocolos_eq014.util

object Validaciones {

    private val regexTelefono = Regex("^\\+56 9 \\d{4} \\d{4}$")
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

    fun telefonoValido(telefono: String): Boolean = regexTelefono.matches(telefono.trim())

    fun correoValido(correo: String): Boolean = regexCorreo.matches(correo.trim())

    fun errorNombre(nombre: String): String? =
        if (nombre.isBlank()) "El nombre es obligatorio" else null

    fun errorRut(rut: String): String? = when {
        rut.isBlank() -> "Ingresa el RUT"
        !rutValido(rut) -> "RUT inválido: el dígito verificador no coincide"
        else -> null
    }

    fun errorTelefono(telefono: String): String? = when {
        telefono.isBlank() -> "Ingresa el teléfono"
        !telefonoValido(telefono) -> "Formato: +56 9 XXXX XXXX"
        else -> null
    }

    fun errorCorreo(correo: String): String? = when {
        correo.isBlank() -> "Ingresa tu correo electrónico"
        !correoValido(correo) -> "Correo inválido. Ejemplo: nombre@empresa.cl"
        else -> null
    }

    fun errorContrasena(contrasena: String): String? = when {
        contrasena.isEmpty() -> "Ingresa tu contraseña"
        contrasena.length < LARGO_MINIMO_CONTRASENA -> "La contraseña debe tener al menos $LARGO_MINIMO_CONTRASENA caracteres"
        else -> null
    }

    private const val LARGO_MINIMO_CONTRASENA = 6

    fun errorObligatorio(valor: String, mensaje: String): String? =
        if (valor.isBlank()) mensaje else null
}
