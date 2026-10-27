package com.example.appprotocolos_eq014.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.appprotocolos_eq014.R

data class CuentaPrueba(
    val nombre: String,
    val correo: String,
    val rol: String
)

const val CONTRASENA_PRUEBA = "123456"

private val gruposCuentasPrueba = listOf(
    "JEFE TÉCNICO" to listOf(
        CuentaPrueba("Ricardo Espinoza Lagos", "respinoza@induztek.ejemplo", "Jefe técnico")
    ),
    "INGENIEROS" to listOf(
        CuentaPrueba("Andrea Contreras Silva", "acontreras@induztek.ejemplo", "Ingeniero"),
        CuentaPrueba("Sebastián Morales Gutiérrez", "smorales@induztek.ejemplo", "Ingeniero")
    ),
    "TÉCNICOS" to listOf(
        CuentaPrueba("Matías Godoy Herrera", "mgodoy@induztek.ejemplo", "Técnico"),
        CuentaPrueba("Javiera Tapia Muñoz", "jtapia@induztek.ejemplo", "Técnico"),
        CuentaPrueba("Carlos Rondón Pérez", "crdon@induztek.ejemplo", "Técnico"),
        CuentaPrueba("Diego Valenzuela Ríos", "dvalenzuela@induztek.ejemplo", "Técnico")
    ),
    "CLIENTES" to listOf(
        CuentaPrueba("Envasadora del Pacífico S.A.", "mfuentes@envpacifico.ejemplo", "Cliente"),
        CuentaPrueba("Sanitaria Valle Central S.A.", "jarancibia@svcentral.ejemplo", "Cliente"),
        CuentaPrueba("Minera Cerro Alto SpA", "pnavarro@cerroalto.ejemplo", "Cliente"),
        CuentaPrueba("Clínica Los Aromos Ltda.", "cmendez@losaromos.ejemplo", "Cliente")
    )
)

@Composable
fun BotonCuentasPrueba(
    expandido: Boolean,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(R.drawable.ic_flecha_abajo),
            contentDescription = "Ver cuentas de prueba",
            modifier = Modifier.rotate(if (expandido) 180f else 0f)
        )
    }
}

@Composable
fun MenuCuentasPrueba(
    expandido: Boolean,
    onCerrar: () -> Unit,
    onSeleccionar: (CuentaPrueba) -> Unit
) {
    DropdownMenu(
        expanded = expandido,
        onDismissRequest = onCerrar,
        modifier = Modifier
            .width(316.dp)
            .heightIn(max = 380.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        gruposCuentasPrueba.forEach { (grupo, cuentas) ->
            Text(
                text = grupo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
            )
            cuentas.forEach { cuenta ->
                DropdownMenuItem(
                    onClick = { onSeleccionar(cuenta) },
                    leadingIcon = { InicialesCuenta(cuenta.nombre) },
                    text = {
                        Column {
                            Text(
                                text = cuenta.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = cuenta.correo,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    trailingIcon = {
                        Text(
                            text = cuenta.rol,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun InicialesCuenta(nombre: String) {
    val iniciales = nombre.split(" ").take(2).joinToString("") { it.take(1) }.uppercase()
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = iniciales,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
