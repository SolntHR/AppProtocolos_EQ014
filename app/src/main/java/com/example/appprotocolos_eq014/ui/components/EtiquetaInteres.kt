package com.example.appprotocolos_eq014.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appprotocolos_eq014.ui.theme.AppProtocolos_EQ014Theme
import com.example.appprotocolos_eq014.ui.theme.Dimens

@Composable
fun EtiquetaInteres(
    texto: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
        contentColor = MaterialTheme.colorScheme.secondary
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = Dimens.espacioMedio, vertical = Dimens.espacioChico)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EtiquetaInteresPreview() {
    AppProtocolos_EQ014Theme {
        EtiquetaInteres(texto = "Kotlin", modifier = Modifier.padding(16.dp))
    }
}
