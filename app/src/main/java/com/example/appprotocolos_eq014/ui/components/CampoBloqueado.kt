package com.example.appprotocolos_eq014.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.ui.styles.EstilosCampo
import com.example.appprotocolos_eq014.ui.theme.AppProtocolos_EQ014Theme
import com.example.appprotocolos_eq014.ui.theme.Dimens

@Composable
fun CampoBloqueado(
    etiqueta: String,
    valor: String,
    ayuda: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(etiqueta) },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_candado),
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.tamanoIcono)
                )
            },
            singleLine = true,
            shape = EstilosCampo.forma,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.background
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = ayuda,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 12.dp, top = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CampoBloqueadoPreview() {
    AppProtocolos_EQ014Theme {
        CampoBloqueado(
            etiqueta = "RUT",
            valor = "17.284.519-3",
            ayuda = "Solo lo puede cambiar el Jefe técnico",
            modifier = Modifier.padding(16.dp)
        )
    }
}
