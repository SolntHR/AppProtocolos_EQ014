package com.example.appprotocolos_eq014.ui.screens.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.model.uistate.InicioUiState
import com.example.appprotocolos_eq014.ui.components.*
import com.example.appprotocolos_eq014.ui.screens.visitas.TarjetaVisita
import com.example.appprotocolos_eq014.util.Fechas

@Composable
fun EncabezadoInicio(uiState: InicioUiState, onAbrirPerfil: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(uiState.fechaHoy, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Hola, ${uiState.primerNombre}", style = MaterialTheme.typography.headlineMedium)
                    if (uiState.empresa.isNotBlank()) Text(uiState.empresa, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                AvatarIniciales(uiState.nombreCompleto, tamano = 46.dp,
                    modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        .clickable(role = Role.Button, onClickLabel = "Mi perfil", onClick = onAbrirPerfil))
            }
        }
    }
}

@Composable
fun InicioPersonalTerreno(
    uiState: InicioUiState,
    onAbrirVisita: (Long) -> Unit,
    onNuevoProtocolo: () -> Unit,
    onAbrirPerfil: () -> Unit,
    onVerVisitas: () -> Unit,
    onAsignarVisita: (() -> Unit)? = null,
    onGestionUsuarios: (() -> Unit)? = null,
    onInstrumentos: (() -> Unit)? = null
) {
    val abiertas = uiState.visitas.filter { !it.cerrada }
    val deHoy = abiertas.filter { it.fecha.take(10) == Fechas.hoy() }
    val resumen = (deHoy.ifEmpty { abiertas }).take(3)
    LazyColumn(
        modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { EncabezadoInicio(uiState, onAbrirPerfil) }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Accesos rápidos", style = MaterialTheme.typography.titleLarge)
                TarjetaAcceso("Nuevo protocolo", "Registra las mediciones de un equipo", R.drawable.ic_mas, onNuevoProtocolo)
                if (onAsignarVisita != null) {
                    TarjetaAcceso("Asignar visita", "Programa una OT y asigna a los técnicos", R.drawable.ic_ubicacion, onAsignarVisita)
                }
                if (onGestionUsuarios != null) {
                    val pendientes = uiState.usuariosPendientes
                    TarjetaAcceso(
                        "Usuarios pendientes ($pendientes)",
                        if (pendientes == 0) "No hay cuentas esperando un perfil" else "Asigna el perfil a las cuentas nuevas",
                        R.drawable.ic_alerta, onGestionUsuarios
                    )
                    TarjetaAcceso("Gestión de usuarios", "Administra el personal y los clientes", R.drawable.ic_persona, onGestionUsuarios)
                }
                if (onInstrumentos != null) {
                    TarjetaAcceso("Instrumentos", "Consulta y actualiza las calibraciones", R.drawable.ic_rayo, onInstrumentos)
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(if (deHoy.isNotEmpty()) "Visitas de hoy" else "Visitas pendientes",
                    style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                TextButton(onClick = onVerVisitas) { Text("Ver todas") }
            }
        }
        if (resumen.isEmpty()) item {
            Box(Modifier.padding(horizontal = 20.dp)) {
                EstadoListado("Sin visitas pendientes", "Puedes revisar las visitas anteriores desde Visitas.")
            }
        }
        items(resumen, key = { it.id }) { visita ->
            TarjetaVisita(visita, { onAbrirVisita(visita.id) }, Modifier.padding(horizontal = 20.dp))
        }
    }
}