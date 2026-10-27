package com.example.appprotocolos_eq014.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.ui.theme.AppProtocolos_EQ014Theme
import com.example.appprotocolos_eq014.ui.theme.AzulOscuro
import com.example.appprotocolos_eq014.ui.theme.AzulPrimario
import com.example.appprotocolos_eq014.ui.theme.AzulTextoCabecera
import com.example.appprotocolos_eq014.ui.theme.Blanco
import com.example.appprotocolos_eq014.ui.theme.NaranjaAcento

@Composable
fun EncabezadoInduztek(
    titulo: String,
    subtitulo: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp))
            .background(
                Brush.linearGradient(
                    0.15f to AzulPrimario,
                    1f to AzulOscuro
                )
            )
            .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 30.dp)
    ) {
        LogoInduztek()
        Spacer(Modifier.height(58.dp))
        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineLarge,
            color = Blanco
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = subtitulo,
            style = MaterialTheme.typography.bodyMedium,
            color = AzulTextoCabecera
        )
    }
}

@Composable
fun LogoInduztek(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = NaranjaAcento)
                .clip(RoundedCornerShape(16.dp))
                .background(NaranjaAcento),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_rayo),
                contentDescription = stringResource(R.string.cd_logo_induztek),
                tint = Blanco,
                modifier = Modifier.size(29.dp)
            )
        }
        Column {
            Text(
                text = stringResource(R.string.marca_induztek),
                color = Blanco,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = stringResource(R.string.marca_protocolos),
                color = AzulTextoCabecera,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EncabezadoInduztekPreview() {
    AppProtocolos_EQ014Theme {
        EncabezadoInduztek(
            titulo = "Bienvenido",
            subtitulo = "Registra pruebas incluso sin conexión."
        )
    }
}
