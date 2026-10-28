package com.example.appprotocolos_eq014.permisos

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

// Revision de permisos del dispositivo (camara y notificaciones)
object Permisos {

    fun tiene(context: Context, permiso: String): Boolean =
        ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED

    // Android 13+ exige el permiso POST_NOTIFICATIONS; en versiones anteriores basta con que esten habilitadas
    fun puedeNotificar(context: Context): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                tiene(context, Manifest.permission.POST_NOTIFICATIONS)
    }
}

/**
 * Pide el permiso de notificaciones una sola vez al entrar a la pantalla.
 * Si el usuario lo rechaza la app sigue funcionando: solo no muestra los avisos.
 */
@Composable
fun SolicitarPermisoNotificaciones() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = LocalContext.current
    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (!Permisos.tiene(context, Manifest.permission.POST_NOTIFICATIONS)) {
            lanzador.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
