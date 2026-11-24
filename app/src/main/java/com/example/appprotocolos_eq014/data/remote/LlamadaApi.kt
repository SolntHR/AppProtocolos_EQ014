package com.example.appprotocolos_eq014.data.remote

import com.google.gson.Gson
import com.google.gson.JsonParseException
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

const val SIN_CONEXION = "No se pudo conectar con el servidor. Revisa tu conexión e inténtalo nuevamente."

class ErrorApi(
    mensaje: String,
    val codigo: Int = 0,
    val campos: Map<String, String> = emptyMap()
) : Exception(mensaje)

private class CuerpoError(val mensaje: String?, val campos: Map<String, String>?)

suspend fun <T> llamarApi(peticion: suspend () -> T): T = try {
    peticion()
} catch (e: HttpException) {
    throw errorDesde(e.code(), e.response()?.errorBody()?.string())
} catch (e: IOException) {
    throw ErrorApi(SIN_CONEXION)
} catch (e: JsonParseException) {
    throw ErrorApi("El servidor respondió con datos inesperados")
}

suspend fun <T> listaApi(peticion: suspend () -> Response<List<T>>): List<T> {
    val respuesta = llamarApi(peticion)
    if (!respuesta.isSuccessful) throw errorDesde(respuesta.code(), respuesta.errorBody()?.string())
    return respuesta.body() ?: emptyList()
}

suspend fun sinContenidoApi(peticion: suspend () -> Response<Unit>) {
    val respuesta = llamarApi(peticion)
    if (!respuesta.isSuccessful) throw errorDesde(respuesta.code(), respuesta.errorBody()?.string())
}

fun errorDesde(codigo: Int, cuerpo: String?): ErrorApi {
    val error = try {
        Gson().fromJson(cuerpo, CuerpoError::class.java)
    } catch (_: Exception) {
        null
    }
    val campos = error?.campos.orEmpty()
    val mensaje = when {
        campos.isNotEmpty() -> campos.values.distinct().joinToString("\n")
        !error?.mensaje.isNullOrBlank() -> error?.mensaje.orEmpty()
        else -> "El servicio no está disponible en este momento. Inténtalo más tarde."
    }
    return ErrorApi(mensaje, codigo, campos)
}