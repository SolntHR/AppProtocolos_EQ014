package com.example.appprotocolos_eq014.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

object Imagenes {
    private const val LADO_MAXIMO = 1280
    private const val CALIDAD_JPEG = 80

    suspend fun jpegDesdeGaleria(contexto: Context, uri: Uri): ByteArray? = withContext(Dispatchers.IO) {
        val tamano = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contexto.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, tamano) }
        var escala = 1
        while (maxOf(tamano.outWidth, tamano.outHeight) / (escala * 2) >= LADO_MAXIMO) escala *= 2
        val opciones = BitmapFactory.Options().apply { inSampleSize = escala }
        contexto.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opciones) }
            ?.let { aJpeg(it) }
    }

    fun aJpeg(foto: Bitmap): ByteArray =
        ByteArrayOutputStream().use { salida ->
            foto.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, salida)
            salida.toByteArray()
        }
}