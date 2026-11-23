package com.example.appprotocolos_eq014.util

import java.security.MessageDigest

object Seguridad {

    fun cifrarContrasena(correo: String, contrasena: String): String {
        val sal = correo.trim().lowercase()
        val bytes = MessageDigest.getInstance("SHA-256").digest((sal + contrasena).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it.toInt() and 0xff) }
    }

}