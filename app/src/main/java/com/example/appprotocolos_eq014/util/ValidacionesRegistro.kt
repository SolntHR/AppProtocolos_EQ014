package com.example.appprotocolos_eq014.util

import com.example.appprotocolos_eq014.model.registro.DatosRegistro
import com.example.appprotocolos_eq014.model.registro.ErroresRegistro
import com.example.appprotocolos_eq014.model.uistate.PreguntasSeguridad
import java.text.Normalizer
import java.util.Locale

object ValidacionesRegistro {
    fun normalizarRut(valor: String): String = valor.replace(".", "").replace("-", "").trim().uppercase(Locale.ROOT)

    fun normalizarTelefono(valor: String): String = Validaciones.normalizarTelefono(valor)

    fun telefonoCompleto(valor: String): String = Validaciones.telefonoCompleto(valor)
    fun nombreNormalizado(valor: String): String = valor.trim().replace(Regex("\\s+"), " ")

    // Solo una sugerencia visual: el servicio resolverá dominio y colisiones.
    fun correoInternoSugerido(nombre: String): String {
        val partes = nombreNormalizado(nombre).split(" ").filter { it.isNotBlank() }
        if (partes.size < 2) return ""
        val apellido = partes[maxOf(1, partes.size - 2)]
        val usuario = Normalizer.normalize("${partes.first().first()}$apellido", Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "").lowercase(Locale.ROOT).filter { it in 'a'..'z' }
        return if (usuario.length >= 2) "$usuario@induztek.ejemplo" else ""
    }

    fun validar(datos: DatosRegistro): ErroresRegistro {
        val nombre = nombreNormalizado(datos.nombre)
        val rut = normalizarRut(datos.rut)
        val telefono = normalizarTelefono(datos.telefono)
        return ErroresRegistro(
            nombre = when {
                nombre.isBlank() -> "Ingresa tu nombre completo"
                nombre.split(" ").count { parte -> parte.any { it.isLetter() } } < 2 -> "Incluye tu nombre y apellido"
                nombre.any { !it.isLetter() && it !in " '-" } -> "Usa letras para tu nombre y apellido"
                else -> null
            },
            rut = when {
                rut.isBlank() -> "Ingresa tu RUT"
                !Regex("[0-9]{7,8}[0-9K]").matches(rut) || !Validaciones.rutValido(rut) -> "Ingresa un RUT válido, incluido su dígito verificador"
                else -> null
            },
            telefono = when {
                telefono.isBlank() -> "Ingresa tu teléfono"
                !Regex("9[0-9]{8}").matches(telefono) -> "Ingresa un celular chileno: 9 1234 5678"
                else -> null
            },
            correoPersonal = Validaciones.errorCorreo(datos.correoPersonal),
            preguntaSeguridad = if (datos.preguntaSeguridad in PreguntasSeguridad.lista) null else "Selecciona una pregunta de seguridad",
            respuestaSeguridad = if (datos.respuestaSeguridad.isBlank()) "Ingresa tu respuesta de seguridad" else null
        )
    }
}
