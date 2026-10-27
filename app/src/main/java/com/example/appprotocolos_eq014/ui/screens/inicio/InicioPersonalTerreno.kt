package com.example.appprotocolos_eq014.ui.screens.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.model.InicioUiState
import com.example.appprotocolos_eq014.ui.components.BotonPrincipal

@Composable
fun InicioPersonalTerreno(
    uiState: InicioUiState,
    onNuevoProtocoloClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "SÁBADO, 26 SEP 2026",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = "Hola, Equipo",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.2f))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painterResource(android.R.drawable.stat_sys_warning), contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Sin conexión · Perfil ${uiState.rolUsuario}", color = Color.White, style = MaterialTheme.typography.labelMedium)
                            Text("Datos guardados en el equipo", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Text("Visitas de hoy", style = MaterialTheme.typography.titleLarge)
                Text("Órdenes programadas", style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        BotonPrincipal(
            texto = "+ Nuevo protocolo",
            onClick = onNuevoProtocoloClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(20.dp)
                .fillMaxWidth()
        )
    }
}