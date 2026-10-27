package com.example.appprotocolos_eq014.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appprotocolos_eq014.R

data class DestinoInferior(
    val ruta: String,
    @StringRes val etiqueta: Int,
    @DrawableRes val icono: Int
)

fun destinosInferiores(esCliente: Boolean): List<DestinoInferior> =
    if (esCliente) {
        listOf(
            DestinoInferior(Rutas.INICIO, R.string.nav_equipos, R.drawable.ic_equipos),
            DestinoInferior(Rutas.PERFIL, R.string.nav_perfil, R.drawable.ic_perfil)
        )
    } else {
        listOf(
            DestinoInferior(Rutas.INICIO, R.string.nav_inicio, R.drawable.ic_inicio),
            DestinoInferior(Rutas.PERFIL, R.string.nav_perfil, R.drawable.ic_perfil)
        )
    }

@Composable
fun BarraNavegacionInferior(
    destinos: List<DestinoInferior>,
    rutaActual: String?,
    onNavegar: (String) -> Unit
) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0)
        ) {
            destinos.forEach { destino ->
                NavigationBarItem(
                    selected = rutaActual == destino.ruta,
                    onClick = { onNavegar(destino.ruta) },
                    icon = {
                        Icon(
                            painter = painterResource(destino.icono),
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(destino.etiqueta),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
