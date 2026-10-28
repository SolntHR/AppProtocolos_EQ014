package com.example.appprotocolos_eq014.notificaciones

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.appprotocolos_eq014.MainActivity
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.model.EstadoProtocolo
import com.example.appprotocolos_eq014.permisos.Permisos

/**
 * Notificaciones locales de Induztek Protocolos (no requieren conexion).
 * - Cambios de estado de un protocolo (registrado, observado, formalizado, anulado)
 * - Cierre de visita con entrega del acta al cliente (RF-07, RF-08)
 */
object NotificacionesLocales {

    private const val CANAL_PROTOCOLOS = "estado_protocolos"
    private const val CANAL_VISITAS = "cierre_visitas"

    fun notificarEstadoProtocolo(context: Context, numeroProtocolo: String, estado: EstadoProtocolo) {
        val (titulo, texto) = when (estado) {
            EstadoProtocolo.BORRADOR ->
                "Borrador guardado" to "El protocolo $numeroProtocolo quedó guardado en el dispositivo."
            EstadoProtocolo.REGISTRADO_TERRENO ->
                "Protocolo registrado en terreno" to "El protocolo $numeroProtocolo quedó pendiente de formalización."
            EstadoProtocolo.OBSERVADO ->
                "Protocolo observado" to "El protocolo $numeroProtocolo fue devuelto con observaciones para su corrección."
            EstadoProtocolo.FORMALIZADO ->
                "Protocolo formalizado" to "El protocolo $numeroProtocolo ya es el documento oficial."
            EstadoProtocolo.ANULADO ->
                "Protocolo anulado" to "El protocolo $numeroProtocolo fue anulado."
        }
        mostrar(context, CANAL_PROTOCOLOS, "Estado de protocolos", numeroProtocolo.hashCode(), titulo, texto)
    }

    fun notificarVisitaCerrada(context: Context, numeroOt: String, receptor: String) {
        mostrar(
            context, CANAL_VISITAS, "Cierre de visitas", numeroOt.hashCode(),
            "Visita $numeroOt cerrada",
            "El acta de prueba en terreno fue entregada a $receptor (pendiente de formalización)."
        )
    }

    @SuppressLint("MissingPermission") // se revisa con Permisos.puedeNotificar
    private fun mostrar(
        context: Context,
        idCanal: String,
        nombreCanal: String,
        idNotificacion: Int,
        titulo: String,
        texto: String
    ) {
        if (!Permisos.puedeNotificar(context)) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(
                    NotificationChannel(idCanal, nombreCanal, NotificationManager.IMPORTANCE_DEFAULT)
                )
        }

        // Al tocar la notificacion se abre la app
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            context, idNotificacion, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(context, idCanal)
            .setSmallIcon(R.drawable.ic_rayo)
            .setColor(0xFF0B3C6F.toInt()) // azul Induztek
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(idNotificacion, notificacion)
    }
}
