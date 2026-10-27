package com.example.appprotocolos_eq014.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appprotocolos_eq014.ui.theme.AppProtocolos_EQ014Theme
import com.example.appprotocolos_eq014.ui.theme.AzulPrimario

val FondoAvatar = Color(0xFFE1EDF7)

fun inicialesDe(nombre: String): String =
    nombre.trim()
        .split(Regex("\\s+"))
        .take(2)
        .joinToString("") { it.take(1).uppercase() }
        .ifEmpty { "?" }

@Composable
fun AvatarIniciales(
    nombre: String,
    modifier: Modifier = Modifier,
    tamano: Dp = 40.dp,
    colorFondo: Color = FondoAvatar,
    colorTexto: Color = AzulPrimario
) {
    Box(
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .background(colorFondo),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = inicialesDe(nombre),
            color = colorTexto,
            fontSize = (tamano.value * 0.36f).sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Preview
@Composable
private fun AvatarInicialesPreview() {
    AppProtocolos_EQ014Theme {
        AvatarIniciales(nombre = "Matías Godoy Herrera", tamano = 96.dp)
    }
}
