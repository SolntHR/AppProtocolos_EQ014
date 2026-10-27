package com.example.appprotocolos_eq014.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FotoPerfilRepository(context: Context) {

    private val contexto = context.applicationContext
    private val carpetaFotos = File(contexto.filesDir, "fotos_perfil").apply { mkdirs() }

    suspend fun leerDesdeGaleria(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        val opcionesTamano = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contexto.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opcionesTamano)
        }
        val lado = maxOf(opcionesTamano.outWidth, opcionesTamano.outHeight)
        var escala = 1
        while (lado / (escala * 2) >= TAMANO_MAXIMO) {
            escala *= 2
        }
        val opciones = BitmapFactory.Options().apply { inSampleSize = escala }
        contexto.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opciones)
        }
    }

    suspend fun guardar(idUsuario: Long, foto: Bitmap): String = withContext(Dispatchers.IO) {
        carpetaFotos.listFiles { archivo -> archivo.name.startsWith("perfil_${idUsuario}_") }
            ?.forEach { it.delete() }
        val archivo = File(carpetaFotos, "perfil_${idUsuario}_${System.currentTimeMillis()}.jpg")
        archivo.outputStream().use { foto.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, it) }
        archivo.absolutePath
    }

    suspend fun cargar(ruta: String?): Bitmap? = withContext(Dispatchers.IO) {
        if (ruta == null) return@withContext null
        val archivo = File(ruta)
        if (archivo.exists()) BitmapFactory.decodeFile(archivo.absolutePath) else null
    }

    suspend fun eliminar(ruta: String?) = withContext(Dispatchers.IO) {
        if (ruta != null) File(ruta).delete()
    }

    private companion object {
        const val TAMANO_MAXIMO = 512
        const val CALIDAD_JPEG = 85
    }
}
