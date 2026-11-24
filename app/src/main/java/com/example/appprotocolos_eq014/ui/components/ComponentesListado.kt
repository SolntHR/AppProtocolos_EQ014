package com.example.appprotocolos_eq014.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.ui.theme.NaranjaAcento
import com.example.appprotocolos_eq014.ui.theme.NaranjaSuave

@Composable
fun RecargarAlVolver(onRecargar: () -> Unit) {
    val propietario = LocalLifecycleOwner.current
    val recargar by rememberUpdatedState(onRecargar)
    DisposableEffect(propietario) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) recargar()
        }
        propietario.lifecycle.addObserver(observador)
        onDispose { propietario.lifecycle.removeObserver(observador) }
    }
}

@Composable
fun CabeceraPagina(titulo: String, subtitulo: String, accion: @Composable (() -> Unit)? = null) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(titulo, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                accion?.invoke()
            }
            Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun TarjetaAcceso(
    titulo: String,
    detalle: String,
    @DrawableRes icono: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    destacada: Boolean = false
) {
    val acento = if (destacada) NaranjaAcento else MaterialTheme.colorScheme.primary
    Surface(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        color = if (destacada) NaranjaSuave else MaterialTheme.colorScheme.surface,
        border = if (destacada) BorderStroke(1.5.dp, NaranjaAcento) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(painterResource(icono), contentDescription = null, tint = if (destacada) Color.White else acento,
                modifier = Modifier.background(if (destacada) NaranjaAcento else MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)).padding(10.dp).size(22.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium)
                Text(detalle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(painterResource(R.drawable.ic_volver), contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp).rotate(180f))
        }
    }
}

@Composable
fun EstadoListado(titulo: String, detalle: String, accion: String? = null, onAccion: () -> Unit = {}) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Text(detalle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (accion != null) TextButton(onClick = onAccion) { Text(accion) }
        }
    }
}

@Composable
fun DatoFicha(etiqueta: String, valor: String?) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(etiqueta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor?.takeIf { it.isNotBlank() } ?: "No informado", style = MaterialTheme.typography.bodyMedium)
    }
}
