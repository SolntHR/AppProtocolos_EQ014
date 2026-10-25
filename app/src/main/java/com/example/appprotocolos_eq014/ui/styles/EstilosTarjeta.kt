package com.example.appprotocolos_eq014.ui.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import com.example.appprotocolos_eq014.ui.theme.Dimens

object EstilosTarjeta {
    val forma: Shape = RoundedCornerShape(Dimens.radioTarjeta)

    @Composable
    fun colores(): CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    )

    @Composable
    fun elevacion(): CardElevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevacionTarjeta)
}
