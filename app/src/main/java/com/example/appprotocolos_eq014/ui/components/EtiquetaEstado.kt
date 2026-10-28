package com.example.appprotocolos_eq014.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appprotocolos_eq014.model.ResultadoPrueba
import com.example.appprotocolos_eq014.ui.theme.AmarilloAviso
import com.example.appprotocolos_eq014.ui.theme.AmarilloSuave
import com.example.appprotocolos_eq014.ui.theme.AzulPrimario
import com.example.appprotocolos_eq014.ui.theme.AzulSuave
import com.example.appprotocolos_eq014.ui.theme.RojoError
import com.example.appprotocolos_eq014.ui.theme.RojoSuave
import com.example.appprotocolos_eq014.ui.theme.SuperficieFuerte
import com.example.appprotocolos_eq014.ui.theme.VerdeExito
import com.example.appprotocolos_eq014.ui.theme.VerdeSuave

enum class TonoEstado(val texto: Color, val fondo: Color) {
    EXITO(VerdeExito, VerdeSuave),
    AVISO(AmarilloAviso, AmarilloSuave),
    ERROR(RojoError, RojoSuave),
    AZUL(AzulPrimario, AzulSuave),
    NEUTRO(Color(0xFF52606C), SuperficieFuerte)
}

fun tonoDe(resultado: ResultadoPrueba?): TonoEstado = when (resultado) {
    ResultadoPrueba.APROBADO -> TonoEstado.EXITO
    ResultadoPrueba.OBSERVADO -> TonoEstado.AVISO
    ResultadoPrueba.RECHAZADO -> TonoEstado.ERROR
    null -> TonoEstado.NEUTRO
}

@Composable
fun EtiquetaEstado(
    texto: String,
    tono: TonoEstado,
    modifier: Modifier = Modifier
) {
    val colorTexto by animateColorAsState(tono.texto, label = "texto_etiqueta")
    val colorFondo by animateColorAsState(tono.fondo, label = "fondo_etiqueta")
    Text(
        text = texto,
        color = colorTexto,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(colorFondo)
            .padding(horizontal = 9.dp, vertical = 5.dp)
    )
}

@Composable
fun EtiquetaResultado(resultado: ResultadoPrueba?, modifier: Modifier = Modifier) {
    EtiquetaEstado(
        texto = resultado?.etiqueta ?: "Sin datos",
        tono = tonoDe(resultado),
        modifier = modifier
    )
}

@Composable
fun TituloSeccion(texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}
